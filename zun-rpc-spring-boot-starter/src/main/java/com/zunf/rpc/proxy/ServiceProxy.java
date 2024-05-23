package com.zunf.rpc.proxy;

import com.zunf.rpc.config.RpcConfig;
import com.zunf.rpc.fault.retry.RetryStrategy;
import com.zunf.rpc.fault.retry.RetryStrategyFactory;
import com.zunf.rpc.fault.tolerance.ToleranceStrategy;
import com.zunf.rpc.fault.tolerance.ToleranceStrategyFactory;
import com.zunf.rpc.loadbalancer.LoadBalancer;
import com.zunf.rpc.loadbalancer.LoadBalancerFactory;
import com.zunf.rpc.model.RpcRequest;
import com.zunf.rpc.model.RpcResponse;
import com.zunf.rpc.model.ServiceMetaInfo;
import com.zunf.rpc.registry.Registry;
import com.zunf.rpc.registry.RegistryFactory;
import com.zunf.rpc.server.client.VertxTcpClient;
import com.zunf.rpc.utils.SpringContextUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务代理（JDK 动态代理）
 *
 * @author zunf
 * @date 2024/5/6 09:25
 */
@Slf4j
public class ServiceProxy implements InvocationHandler {

    @SneakyThrows
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        //构造请求参数
        String serviceName = method.getDeclaringClass().getName();
        RpcRequest request = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .paramTypes(method.getParameterTypes())
                .params(args)
                .build();

        //获取服务提供者地址
        RpcConfig rpcConfig = SpringContextUtil.getBean(RpcConfig.class);
        String registryType = rpcConfig.getRegistryConfig().getType();
        Registry registry = RegistryFactory.getInstance(registryType);
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        List<ServiceMetaInfo> serviceList = registry.discover(serviceMetaInfo.getServiceKey());

        //负载均衡
        LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
        //将调用方法名作为负载均衡参数
        Map<String, Object> params = new HashMap<>(5);
        params.put("methodName", request.getMethodName());
        //获取负载均衡算法选出的服务
        ServiceMetaInfo selectedService = loadBalancer.select(params, serviceList);
        //发送请求，使用重试策略
        RpcResponse rpcResponse = null;
        try {
            RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());
            rpcResponse = retryStrategy.doRetry(() ->
                    VertxTcpClient.doRequest(selectedService, request, rpcConfig));
            return rpcResponse.getData();
        } catch (Exception e) {
            log.warn("重试结束，启动容错机制...");
            //容错机制
            ToleranceStrategy toleranceStrategy = ToleranceStrategyFactory.getInstance(rpcConfig.getToleranceStrategy());
            Map<String, Object> context = new HashMap<>();
            context.put("serviceKey", selectedService.getServiceKey());
            context.put("serviceNodeKey", selectedService.getServiceNodeKey());
            context.put("request", request);
            rpcResponse = toleranceStrategy.doTolerance(context, e);
        }

        return rpcResponse;
    }
}
