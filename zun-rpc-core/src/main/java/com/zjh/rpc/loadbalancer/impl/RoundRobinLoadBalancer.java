package com.zjh.rpc.loadbalancer.impl;

import com.zjh.rpc.loadbalancer.LoadBalancer;
import com.zjh.rpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮训负载均衡器
 *
 * @author zunf
 * @date 2024/5/20 14:16
 */
public class RoundRobinLoadBalancer implements LoadBalancer {

    /**
     * JUC包中线程安全的原子计数器
     */
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    @Override
    public ServiceMetaInfo select(Map<String, Object> params, List<ServiceMetaInfo> serviceMetaInfoList) {
        if (serviceMetaInfoList.isEmpty()) {
            return null;
        }
        int size = serviceMetaInfoList.size();
        if (size == 1) {
            return serviceMetaInfoList.get(0);
        }
        //取模轮训
        int index = currentIndex.getAndIncrement() % size;
        return serviceMetaInfoList.get(index);
    }
}
