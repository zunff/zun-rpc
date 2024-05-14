package com.zjh.rpc.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.zjh.rpc.RpcApplication;
import com.zjh.rpc.config.RpcConfig;
import com.zjh.rpc.model.RpcRequest;
import com.zjh.rpc.model.RpcResponse;
import com.zjh.rpc.model.ServiceMetaInfo;
import com.zjh.rpc.registry.Registry;
import com.zjh.rpc.registry.RegistryFactory;
import com.zjh.rpc.registry.RegistryServiceCache;
import com.zjh.rpc.serializer.Serializer;
import com.zjh.rpc.serializer.SerializerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 服务代理（JDK 动态代理）
 *
 * @author zunf
 * @date 2024/5/6 09:25
 */
public class ServiceProxy implements InvocationHandler {


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        Serializer serializer = SerializerFactory.getInstance(rpcConfig.getSerializer());

        //构造请求参数
        String serviceName = method.getDeclaringClass().getName();
        RpcRequest request = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .paramTypes(method.getParameterTypes())
                .params(args)
                .build();

        try {
            byte[] bytes = serializer.serialize(request);
            //获取服务提供者地址
            String registryType = RpcApplication.getRpcConfig().getRegistryConfig().getType();
            Registry registry = RegistryFactory.getInstance(registryType);
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            List<ServiceMetaInfo> serviceList =  registry.discover(serviceMetaInfo.getServiceKey());

            //todo 暂时只获取第一个
            serviceMetaInfo = serviceList.get(0);
            //发送请求
            try (HttpResponse httpResponse = HttpRequest
                    .post(serviceMetaInfo.getServiceAddress())
                    .body(bytes).execute()){

                //处理接口执行结果并返回
                byte[] result = httpResponse.bodyBytes();
                //反序列化
                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
                return rpcResponse.getData();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
