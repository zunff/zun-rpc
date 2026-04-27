package com.zunf.rpc.proxy;

import com.zunf.rpc.config.RpcConfig;
import com.zunf.rpc.fault.retry.RetryStrategy;
import com.zunf.rpc.fault.tolerance.ToleranceStrategy;
import com.zunf.rpc.loadbalancer.LoadBalancer;
import com.zunf.rpc.model.RpcRequest;
import com.zunf.rpc.model.RpcResponse;
import com.zunf.rpc.model.ServiceMetaInfo;
import com.zunf.rpc.registry.Registry;
import com.zunf.rpc.client.NettyTcpClient;
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
        // 拦截 Object 方法，不走 RPC
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }

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
        Registry registry = SpringContextUtil.getBean(Registry.class);
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        List<ServiceMetaInfo> serviceList = registry.discover(serviceMetaInfo.getServiceKey());

        //负载均衡
        LoadBalancer loadBalancer = SpringContextUtil.getBean(LoadBalancer.class);
        Map<String, Object> params = new HashMap<>(5);
        params.put("methodName", request.getMethodName());
        ServiceMetaInfo selectedService = loadBalancer.select(params, serviceList);
        //发送请求，使用重试策略
        RpcResponse rpcResponse = null;
        try {
            RetryStrategy retryStrategy = SpringContextUtil.getBean(RetryStrategy.class);
            rpcResponse = retryStrategy.doRetry(() ->
                    NettyTcpClient.doRequest(selectedService, request, rpcConfig));
            return rpcResponse.getData();
        } catch (Exception e) {
            log.warn("重试结束，启动容错机制...");
            //容错机制
            ToleranceStrategy toleranceStrategy = SpringContextUtil.getBean(ToleranceStrategy.class);
            Map<String, Object> context = new HashMap<>();
            context.put("serviceKey", selectedService.getServiceKey());
            context.put("serviceNodeKey", selectedService.getServiceNodeKey());
            context.put("request", request);
            rpcResponse = toleranceStrategy.doTolerance(context, e);
        }

        return rpcResponse;
    }
}
