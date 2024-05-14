package com.zjh.rpc.registry;


import com.zjh.rpc.model.ServiceMetaInfo;
import org.checkerframework.checker.units.qual.C;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 服务注册信息缓存
 *
 * @author zunf
 * @date 2024/5/11 17:06
 */
public class RegistryServiceCache {

    /**
     * 线程安全的Map缓存，serviceKey(不带host:port的) ->服务列表（多个用于负载均衡）
     */
    public static final Map<String, List<ServiceMetaInfo>> CACHE = new ConcurrentHashMap<>();

    /**
     * 写入数据到缓存中
     * @param serviceKey ServiceKey是服务目录的唯一标识符，但是可以对应多个程序（负载均衡）
     * @param serviceMetaInfos 服务元信息列表
     */
    public static void writeCache(String serviceKey, List<ServiceMetaInfo> serviceMetaInfos) {
        CACHE.put(serviceKey, serviceMetaInfos);
    }

    /**
     * 根据ServiceKey获取服务元信息列表
     * @param serviceKey ServiceKey是服务目录的唯一标识符，但是可以对应多个程序（负载均衡）
     * @return
     */
    public static List<ServiceMetaInfo> getCacheByServiceKey(String serviceKey) {
        return CACHE.get(serviceKey);
    }

    /**
     * 根据 ServiceNodeKey 删除服务元信息
     *
     * @param serviceNodeKey 服务程序的唯一标识符
     */
    public static void removeCacheByServiceNodeKey(String serviceNodeKey) {
        CACHE.keySet().stream().filter(serviceKey -> serviceNodeKey.startsWith(serviceKey)).forEach(serviceKey -> {
            List<ServiceMetaInfo> serviceMetaInfos = CACHE.get(serviceKey);
            //传进来的值为：/rpc/com.zjh.common.service.UserService:1.0/localhost:8099
            //serviceMetaInfo.getServiceNodeKey()的值为：com.zjh.common.service.UserService:1.0/localhost:8099
            //所以用endWith
            serviceMetaInfos.removeIf(serviceMetaInfo -> serviceNodeKey.endsWith(serviceMetaInfo.getServiceNodeKey()));
        });
    }
}
