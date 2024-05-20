package com.zjh.rpc.registry.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.zjh.rpc.config.RegistryConfig;
import com.zjh.rpc.constants.RpcConstants;
import com.zjh.rpc.model.ServiceMetaInfo;
import com.zjh.rpc.registry.Registry;
import com.zjh.rpc.registry.RegistryServiceCache;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Etcd注册中心
 *
 * @author zunf
 * @date 2024/5/9 11:43
 */
@Slf4j
public class EtcdRegistry implements Registry {

    private Client client = null;

    private KV kvClient = null;

    /**
     * Etcd存储根路径
     */
    public static final String ETCD_ROOT_PATH = "/rpc/";

    private final Set<String> watchingKeySet = new HashSet<>();

    /**
     * 本机注册的节点key集合，用于续期
     */
    private final Set<String> localRegistryNodeKeySet = new HashSet<>();

    @Override
    public void init(RegistryConfig registryConfig) {

        client = Client.builder()
                .endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout())).build();

        kvClient = client.getKVClient();

        //注册中心初始化时，开启定时任务，定时续期
        this.heartbeat();
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        //创建租借客户端，用来设置k-v时限
        Lease leaseClient = client.getLeaseClient();
        // 30s 的租约
        long leaseId = leaseClient.grant(30).get().getID();
        //设置键值对
        String registryKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        ByteSequence key = ByteSequence.from(registryKey, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);

        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        kvClient.put(key, value, putOption).get();

        //注册节点时本地缓存一份key
        localRegistryNodeKeySet.add(registryKey);

        //监控这个服务的节点目录，并且不会重复监控
        watchingKeySet.add(serviceMetaInfo.getServiceKey());
    }

    @Override
    public void unregister(ServiceMetaInfo serviceMetaInfo) throws Exception {
        String registryKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        ByteSequence key = ByteSequence.from(registryKey, StandardCharsets.UTF_8);
        kvClient.delete(key).get();
    }

    @Override
    public List<ServiceMetaInfo> discover(String serviceKey) throws Exception {

        List<ServiceMetaInfo> serviceList = null;

        //先从服务缓存中获取
        serviceList = RegistryServiceCache.getCacheByServiceKey(serviceKey);

        if (CollUtil.isEmpty(serviceList)) {
            //缓存中没有再去注册中心找
            GetOption getOption = GetOption.builder().isPrefix(true).build();
            List<KeyValue> keyValueList = kvClient.get(
                    ByteSequence.from(ETCD_ROOT_PATH + serviceKey, StandardCharsets.UTF_8), getOption
            ).get().getKvs();

            serviceList = keyValueList.stream().map(keyValue -> {
                String jsonStr = keyValue.getValue().toString(StandardCharsets.UTF_8);
                return JSONUtil.toBean(jsonStr, ServiceMetaInfo.class);
            }).collect(Collectors.toList());
            //把服务信息添加到缓存里
            RegistryServiceCache.writeCache(serviceKey, serviceList);
            //监听这个节点的目录，serviceKey
            watch(ETCD_ROOT_PATH + serviceKey);
        }

        if (serviceList.isEmpty()) {
            throw new RuntimeException("没有启动符合的节点：" + serviceKey);
        }

        return serviceList;
    }

    @Override
    public void heartbeat() {
        CronUtil.schedule(RpcConstants.HEAT_BEAT_SCHEDULE_STR, new Task() {
            @Override
            public void execute() {
                for (String key : localRegistryNodeKeySet) {
                    try {
                        List<KeyValue> keyValueList = kvClient.get(ByteSequence.from(key, StandardCharsets.UTF_8)).get().getKvs();
                        if (CollUtil.isEmpty(keyValueList)) {
                            //Etcd中找不到该数据，需要重新启动才能注册
                            continue;
                        }
                        //找到了数据，进行续期操作
                        String json = keyValueList.get(0).getValue().toString(StandardCharsets.UTF_8);
                        ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(json, ServiceMetaInfo.class);
                        register(serviceMetaInfo);
                    } catch (Exception e) {
                        throw new RuntimeException(key + "续签失败" + e);
                    }
                }
            }
        });

        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }

    @Override
    public void watch(String serviceKey) {
        boolean isNew = watchingKeySet.add(serviceKey);
        if (isNew) {
            Watch watchClient = client.getWatchClient();
            WatchOption watchOption = WatchOption.builder().isPrefix(true).build();
            watchClient.watch(ByteSequence.from(serviceKey, StandardCharsets.UTF_8), watchOption, watchResponse -> {

                for (WatchEvent event : watchResponse.getEvents()) {
                    //删除操作，只能从WatchEvent中拿到key，value为null
                    String serviceNodeKey = event.getKeyValue().getKey().toString(StandardCharsets.UTF_8);

                    switch (event.getEventType()) {
                        case DELETE:
                            log.info("------------服务：{}, 已下线，删除缓存------------", serviceKey);
                            RegistryServiceCache.removeCacheByServiceNodeKey(serviceNodeKey);
                            break;
                        case PUT:
                            String json = event.getKeyValue().getValue().toString(StandardCharsets.UTF_8);
                            ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(json, ServiceMetaInfo.class);
                            List<ServiceMetaInfo> serviceMetaInfoList = RegistryServiceCache.getCacheByServiceKey(serviceKey);

                            if (serviceMetaInfoList == null) {
                                //如果还不存在该 serviceKey 对应的 List，创建一个
                                serviceMetaInfoList = new ArrayList<>();
                                serviceMetaInfoList.add(serviceMetaInfo);

                                RegistryServiceCache.writeCache(serviceKey, serviceMetaInfoList);
                            } else {
                                //如果是续签操作，不添加缓存
                                if (!serviceMetaInfoList.contains(serviceMetaInfo)) {
                                    log.info("------------服务：{}, 已上线，添加缓存------------", serviceKey);
                                    serviceMetaInfoList.add(serviceMetaInfo);
                                }
                            }
                            break;
                        default:
                            log.error("非法的Etcd操作类型");
                    }
                    //记录日志--更新后的缓存结果
                    log.info(RegistryServiceCache.getCacheByServiceKey(serviceKey).toString());
                }
            });
        }
    }

    @Override
    public void destroy() {
        log.info("当前节点下线");

        //便利删除所有注册的服务
        for (String key : localRegistryNodeKeySet) {
            try {
                kvClient.delete(ByteSequence.from(key, StandardCharsets.UTF_8)).get();
            } catch (Exception e) {
                throw new RuntimeException("节点下线失败：" + e);
            }
        }

        //释放资源
        if (client != null) {
            client.close();
        }
        if (kvClient != null) {
            kvClient.close();

        }
    }
}
