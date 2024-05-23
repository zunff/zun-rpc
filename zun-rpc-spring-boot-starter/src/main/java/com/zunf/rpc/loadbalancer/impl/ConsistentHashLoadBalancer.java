package com.zunf.rpc.loadbalancer.impl;

import com.zunf.rpc.loadbalancer.LoadBalancer;
import com.zunf.rpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 一致性Hash负载均衡器
 *
 * @author zunf
 * @date 2024/5/21 10:32
 */
public class ConsistentHashLoadBalancer implements LoadBalancer {

    /**
     * 一致性 Hash 环，存放虚拟节点
     */
    private final TreeMap<Integer, ServiceMetaInfo> virtualNodes = new TreeMap<>();

    /**
     * 虚拟节点数
     */
    private static final int VIRTUAL_NODE_NUM = 100;

    @Override
    public ServiceMetaInfo select(Map<String, Object> params, List<ServiceMetaInfo> serviceMetaInfoList) {
        if (serviceMetaInfoList.isEmpty()) {
            return null;
        }
        if (serviceMetaInfoList.size() == 1) {
            return serviceMetaInfoList.get(0);
        }
        //构建一致性Hash环
        for (ServiceMetaInfo serviceMetaInfo : serviceMetaInfoList) {
            //每一个节点都建立虚拟节点
            for (int i = 0; i < VIRTUAL_NODE_NUM; i++) {
                int hash = getHash(serviceMetaInfo.getServiceNodeKey() + "#" + i);
                virtualNodes.put(hash, serviceMetaInfo);
            }
        }
        //获取调用请求的Hash值
        int hash = getHash(params);
        //选择一个最接近且大于请求hash值的虚拟节点
        Map.Entry<Integer, ServiceMetaInfo> entry = virtualNodes.ceilingEntry(hash);
        if (entry == null) {
            //如果没有大于等于当前请求的hash值的虚拟节点，则返回环首部的节点
            entry = virtualNodes.firstEntry();
        }
        return entry.getValue();
    }

    /**
     * Hash算法
     * @param key
     * @return hash值
     */
    private int getHash(Object key) {
        return key.hashCode();
    }
}
