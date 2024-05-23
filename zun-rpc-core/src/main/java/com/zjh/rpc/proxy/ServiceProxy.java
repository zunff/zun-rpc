package com.zjh.rpc.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.zjh.rpc.RpcApplication;
import com.zjh.rpc.config.RpcConfig;
import com.zjh.rpc.enums.MessageStatusEnums;
import com.zjh.rpc.enums.MessageTypeEnums;
import com.zjh.rpc.enums.SerializerEnums;
import com.zjh.rpc.fault.retry.RetryStrategy;
import com.zjh.rpc.fault.retry.RetryStrategyFactory;
import com.zjh.rpc.fault.tolerance.ToleranceStrategy;
import com.zjh.rpc.fault.tolerance.ToleranceStrategyFactory;
import com.zjh.rpc.loadbalancer.LoadBalancer;
import com.zjh.rpc.loadbalancer.LoadBalancerFactory;
import com.zjh.rpc.model.RpcRequest;
import com.zjh.rpc.model.RpcResponse;
import com.zjh.rpc.model.ServiceMetaInfo;
import com.zjh.rpc.protocol.ProtocolMessage;
import com.zjh.rpc.protocol.ProtocolMessageDecoder;
import com.zjh.rpc.protocol.ProtocolMessageEncoder;
import com.zjh.rpc.protocol.wrapper.TcpBufferHandlerWrapper;
import com.zjh.rpc.registry.Registry;
import com.zjh.rpc.registry.RegistryFactory;
import com.zjh.rpc.serializer.Serializer;
import com.zjh.rpc.serializer.SerializerFactory;
import com.zjh.rpc.server.client.VertxTcpClient;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
        String registryType = RpcApplication.getRpcConfig().getRegistryConfig().getType();
        Registry registry = RegistryFactory.getInstance(registryType);
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        List<ServiceMetaInfo> serviceList = registry.discover(serviceMetaInfo.getServiceKey());

        RpcConfig rpcConfig = RpcApplication.getRpcConfig();

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
