package com.zjh.rpc.registry;

import com.zjh.rpc.config.RegistryConfig;
import com.zjh.rpc.model.ServiceMetaInfo;

import java.util.List;

/**
 * 注册中心实现接口
 *
 * @author zunf
 * @date 2024/5/9 10:41
 */
public interface Registry {

    /**
     * 注册中心初始化
     *
     * @param registryConfig 注册中心配置
     */
    void init(RegistryConfig registryConfig);

    /**
     * 注册服务到注册中心
     *
     * @param serviceMetaInfo 服务元信息
     */
    void register(ServiceMetaInfo serviceMetaInfo) throws Exception;


    /**
     * 从注册中心注销服务
     *
     * @param serviceMetaInfo
     */
    void unregister(ServiceMetaInfo serviceMetaInfo) throws Exception;

    /**
     * 服务发现，根据服务名称，获取节点列表
     *
     * @param serviceKey 服务名称，需根据具体实现决定是否支持前缀搜索
     * @return 符合要求的服务节点
     */
    List<ServiceMetaInfo> discover(String serviceKey) throws Exception;

    /**
     * 心跳检测
     */
    void heartbeat();

    /**
     * 监控（消费端）
     *
     * @param serviceKey 服务节点目录
     */
    void watch(String serviceKey);

    /**
     * 销毁注册中心
     */
    void destroy();
}
