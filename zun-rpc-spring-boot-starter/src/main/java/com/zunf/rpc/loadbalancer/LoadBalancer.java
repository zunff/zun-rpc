package com.zunf.rpc.loadbalancer;

import com.zunf.rpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;

/**
 * 负载均衡器
 *
 * @author zunf
 * @date 2024/5/20 14:00
 */
public interface LoadBalancer {

    /**
     * 根据不同的负载均衡策略，获取一个服务，
     * @param params 负载均衡参数，只有使用一致性Hash时才需要传入，其他的负载均衡策略传什么都可以，你甚至可以传入null
     * @param serviceMetaInfoList 服务列表
     * @return 选择的服务
     */
    ServiceMetaInfo select(Map<String, Object> params, List<ServiceMetaInfo> serviceMetaInfoList);
}
