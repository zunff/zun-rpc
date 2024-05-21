package com.zjh.rpc.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.zjh.rpc.RpcApplication;
import com.zjh.rpc.config.RpcConfig;
import com.zjh.rpc.enums.MessageTypeEnums;
import com.zjh.rpc.enums.SerializerEnums;
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
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
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

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //构造请求参数
        String serviceName = method.getDeclaringClass().getName();
        RpcRequest request = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .paramTypes(method.getParameterTypes())
                .params(args)
                .build();

        try {
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
            Map<String, Object> params = new HashMap<>();
            params.put("methodName", request.getMethodName());
            //获取负载均衡算法选出的服务
            serviceMetaInfo = loadBalancer.select(params, serviceList);
            //发送请求
            return invokeByTcp(serviceMetaInfo, request, rpcConfig);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private Object invokeByTcp(ServiceMetaInfo serviceMetaInfo, RpcRequest request, RpcConfig rpcConfig) throws Exception {
        RpcResponse rpcResponse = null;
        Vertx vertx = Vertx.vertx();
        NetClient netClient = vertx.createNetClient();
        CompletableFuture<RpcResponse> completableFuture = new CompletableFuture<>();
        //建立链接
        netClient.connect(serviceMetaInfo.getPort(), serviceMetaInfo.getHost(), result -> {
            if (result.succeeded()) {
                NetSocket netSocket = result.result();
                ProtocolMessage.Header header = new ProtocolMessage.Header();

                SerializerEnums serializerEnums = SerializerEnums.of(rpcConfig.getSerializer());
                if (serializerEnums == null) {
                    throw new RuntimeException("序列化器类型不存在");
                }

                header.setSerializer((byte) serializerEnums.getType());
                header.setType((byte) MessageTypeEnums.REQUEST.getType());

                //发送请求
                ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>(header, request);
                try {
                    Buffer encode = ProtocolMessageEncoder.encode(protocolMessage);
                    netSocket.write(encode);
                } catch (IOException e) {
                    throw new RuntimeException("协议消息编码错误" + e);
                }
                //接收响应，使用自己封装的装饰者模式加强过的能够处理半包、粘包的Handler
                netSocket.handler(new TcpBufferHandlerWrapper(buffer -> {
                    try {
                        ProtocolMessage<?> decode = ProtocolMessageDecoder.decode(buffer);
                        completableFuture.complete((RpcResponse) decode.getBody());
                    } catch (IOException e) {
                        throw new RuntimeException("协议消息解码错误" + e);
                    }
                }));
            } else {
                log.error("Fail to connect to server", result.cause());
            }
        });
        //阻塞，等到拿到类结构再往下执行
        rpcResponse = completableFuture.get();
        //关闭链接
        netClient.close();
        return rpcResponse.getData();
    }

    private Object invokeByHttp(ServiceMetaInfo serviceMetaInfo, RpcRequest request, RpcConfig rpcConfig) throws IOException {
        //序列化body
        Serializer serializer = SerializerFactory.getInstance(rpcConfig.getSerializer());
        byte[] bytes = serializer.serialize(request);

        try (HttpResponse httpResponse = HttpRequest
                .post(serviceMetaInfo.getServiceAddress())
                .body(bytes).execute()) {

            //处理接口执行结果并返回
            byte[] result = httpResponse.bodyBytes();
            //反序列化
            RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
            return rpcResponse.getData();
        }
    }
}
