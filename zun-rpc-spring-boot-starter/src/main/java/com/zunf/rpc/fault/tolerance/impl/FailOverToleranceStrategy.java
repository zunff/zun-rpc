package com.zunf.rpc.fault.tolerance.impl;

import com.zunf.rpc.RpcApplication;
import com.zunf.rpc.registry.RegistryServiceCache;
import com.zunf.rpc.server.client.VertxTcpClient;
import com.zunf.rpc.config.RpcConfig;
import com.zunf.rpc.fault.retry.RetryStrategy;
import com.zunf.rpc.fault.retry.RetryStrategyFactory;
import com.zunf.rpc.fault.tolerance.ToleranceStrategy;
import com.zunf.rpc.loadbalancer.LoadBalancer;
import com.zunf.rpc.loadbalancer.impl.RandomLoadBalancer;
import com.zunf.rpc.model.RpcRequest;
import com.zunf.rpc.model.RpcResponse;
import com.zunf.rpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 容错策略-故障转移
 *
 * @author zunf
 * @date 2024/5/22 18:52
 */
public class FailOverToleranceStrategy implements ToleranceStrategy {
    @Override
    public RpcResponse doTolerance(Map<String, Object> context, Exception e) {
        String serviceKey = (String) context.get("serviceKey");
        String serviceNodeKey = (String) context.get("serviceNodeKey");
        RpcRequest request = (RpcRequest) context.get("request");

        //获取其他的节点并调用
        List<ServiceMetaInfo> serviceMetaInfoList = RegistryServiceCache.getCacheByServiceKey(serviceKey);
        List<ServiceMetaInfo> filterList = serviceMetaInfoList.stream()
                .filter(serviceMetaInfo ->
                        !serviceNodeKey.equals(serviceMetaInfo.getServiceNodeKey())).collect(Collectors.toList());
        if (filterList.isEmpty()) {
            //如果只有一个节点，直接抛出异常，走快速失败容错策略
            throw new RuntimeException("调用远程服务报错", e);
        }

        RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        //直接随便拿一个服务
        LoadBalancer loadBalancer = new RandomLoadBalancer();
        ServiceMetaInfo selectedService = loadBalancer.select(null, filterList);
        //发送请求，重试，报错直接抛出
        RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());
        RpcResponse rpcResponse;
        try {
            rpcResponse = retryStrategy.doRetry(() ->
                    VertxTcpClient.doRequest(selectedService, request, rpcConfig));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return rpcResponse;
    }
}
