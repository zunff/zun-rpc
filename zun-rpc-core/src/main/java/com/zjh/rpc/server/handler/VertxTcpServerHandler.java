package com.zjh.rpc.server.handler;


import com.zjh.rpc.RpcApplication;
import com.zjh.rpc.enums.MessageStatusEnums;
import com.zjh.rpc.enums.MessageTypeEnums;
import com.zjh.rpc.enums.SerializerEnums;
import com.zjh.rpc.model.RpcRequest;
import com.zjh.rpc.model.RpcResponse;
import com.zjh.rpc.protocol.ProtocolMessage;
import com.zjh.rpc.protocol.ProtocolMessageDecoder;
import com.zjh.rpc.protocol.ProtocolMessageEncoder;
import com.zjh.rpc.protocol.wrapper.TcpBufferHandlerWrapper;
import com.zjh.rpc.registry.LocalRegistry;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * Vertx Tcp 协议服务处理器
 *
 * @author zunf
 * @date 2024/5/14 18:16
 */
@Slf4j
public class VertxTcpServerHandler implements Handler<NetSocket> {

    @Override
    public void handle(NetSocket netSocket) {
        //使用自己封装的装饰者模式加强过的能够处理半包、粘包的Handler
        netSocket.handler(new TcpBufferHandlerWrapper(buffer -> {

            ProtocolMessage.Header header = new ProtocolMessage.Header();

            //处理请求，调用服务，获得返回体
            try {
                RpcResponse rpcResponse = handleRequest(buffer, header);

                SerializerEnums serializerEnums = SerializerEnums.of(RpcApplication.getRpcConfig().getSerializer());
                if (serializerEnums == null) {
                    throw new RuntimeException("序列化器不存在");
                }

                header.setSerializer((byte) serializerEnums.getType());
                header.setType((byte) MessageTypeEnums.RESPONSE.getType());

                ProtocolMessage<RpcResponse> protocolMessage = new ProtocolMessage<>(header, rpcResponse);

                //编码器编码，并对请求体进行序列化
                Buffer encode = ProtocolMessageEncoder.encode(protocolMessage);

                //响应请求
                netSocket.write(encode);
            } catch (Exception e) {
                log.error("调用服务失败:{}", e.getMessage());
            }
        }));
    }

    private RpcResponse handleRequest(Buffer buffer, ProtocolMessage.Header header) throws Exception {
        RpcResponse rpcResponse = new RpcResponse();
        //解码并序列化
        ProtocolMessage<?> protocolMessage = ProtocolMessageDecoder.decode(buffer);
        //调用服务，不需要再进行反序列化了，因为我们的自定义解码器中已经反序列化过了
        RpcRequest rpcRequest = (RpcRequest) protocolMessage.getBody();

        log.info("Received request: {}  {}", rpcRequest.getServiceName(), rpcRequest.getMethodName());

        Class<?> serviceClass = LocalRegistry.get(rpcRequest.getServiceName());
        if (serviceClass == null) {
            throw new RuntimeException("不存在此服务" + rpcRequest.getServiceName());
        }
        Method method = serviceClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
        Object result = null;
        try {
            //class.newInstance()在java9已经标为废弃，因其只能创建有无参构造器的对象的局限性
            result = method.invoke(serviceClass.getConstructor().newInstance(), rpcRequest.getParams());
        } catch (Exception e) {
            rpcResponse.setMessage(e.getMessage());
            rpcResponse.setException(e);
            header.setStatus((byte) MessageStatusEnums.RESPONSE_FAILED.getType());
            log.error("Web服务器，调用方法时报错：{}", e.getMessage());
        }
        //构造返回对象
        rpcResponse.setData(result);
        rpcResponse.setDataType(method.getReturnType());
        rpcResponse.setMessage("ok");
        header.setStatus((byte) MessageStatusEnums.OK.getType());

        return rpcResponse;
    }
}
