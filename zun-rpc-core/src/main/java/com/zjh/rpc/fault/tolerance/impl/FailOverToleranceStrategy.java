package com.zjh.rpc.fault.tolerance.impl;

import com.zjh.rpc.RpcApplication;
import com.zjh.rpc.config.RpcConfig;
import com.zjh.rpc.fault.retry.RetryStrategy;
import com.zjh.rpc.fault.retry.RetryStrategyFactory;
import com.zjh.rpc.fault.tolerance.ToleranceStrategy;
import com.zjh.rpc.loadbalancer.LoadBalancer;
import com.zjh.rpc.loadbalancer.impl.RandomLoadBalancer;
import com.zjh.rpc.model.RpcRequest;
import com.zjh.rpc.model.RpcResponse;
import com.zjh.rpc.model.ServiceMetaInfo;
import com.zjh.rpc.registry.RegistryServiceCache;
import com.zjh.rpc.server.client.VertxTcpClient;

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
