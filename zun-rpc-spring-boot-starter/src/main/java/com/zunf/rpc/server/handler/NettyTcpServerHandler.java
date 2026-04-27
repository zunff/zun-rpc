package com.zunf.rpc.server.handler;

import com.zunf.rpc.config.RpcConfig;
import com.zunf.rpc.enums.MessageStatusEnums;
import com.zunf.rpc.enums.MessageTypeEnums;
import com.zunf.rpc.enums.SerializerEnums;
import com.zunf.rpc.model.RpcRequest;
import com.zunf.rpc.model.RpcResponse;
import com.zunf.rpc.protocol.ProtocolMessage;
import com.zunf.rpc.server.LocalRegistry;
import com.zunf.rpc.utils.SpringContextUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Slf4j
@io.netty.channel.ChannelHandler.Sharable
public class NettyTcpServerHandler extends SimpleChannelInboundHandler<ProtocolMessage<RpcRequest>> {

    public static final NettyTcpServerHandler INSTANCE = new NettyTcpServerHandler();

    private NettyTcpServerHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtocolMessage<RpcRequest> msg) {
        ProtocolMessage.Header requestHeader = msg.getHeader();
        RpcRequest rpcRequest = msg.getBody();

        ProtocolMessage.Header responseHeader = new ProtocolMessage.Header();
        responseHeader.setRequestId(requestHeader.getRequestId());

        SerializerEnums serializerEnums = SerializerEnums.of(
                SpringContextUtil.getBean(RpcConfig.class).getSerializer());
        if (serializerEnums == null) {
            throw new RuntimeException("序列化器不存在");
        }
        responseHeader.setSerializer((byte) serializerEnums.getType());
        responseHeader.setType((byte) MessageTypeEnums.RESPONSE.getType());

        ProtocolMessage<RpcResponse> protocolMessage;

        try {
            RpcResponse rpcResponse = handleRequest(rpcRequest);
            responseHeader.setStatus((byte) MessageStatusEnums.OK.getType());
            protocolMessage = new ProtocolMessage<>(responseHeader, rpcResponse);
        } catch (Exception e) {
            log.error("调用服务失败: {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            RpcResponse rpcResponse = new RpcResponse();
            rpcResponse.setMessage(String.format("调用服务失败：%s",
                    e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
            rpcResponse.setExceptionClassName(e.getClass().getName());
            responseHeader.setStatus((byte) MessageStatusEnums.RESPONSE_FAILED.getType());
            protocolMessage = new ProtocolMessage<>(responseHeader, rpcResponse);
        }

        ctx.writeAndFlush(protocolMessage);
    }

    private RpcResponse handleRequest(RpcRequest rpcRequest) throws Exception {
        log.info("Received request: {} {}", rpcRequest.getServiceName(), rpcRequest.getMethodName());

        Class<?> serviceClass = LocalRegistry.get(rpcRequest.getServiceName());
        if (serviceClass == null) {
            throw new RuntimeException("不存在此服务" + rpcRequest.getServiceName());
        }
        Method method = serviceClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
        Object bean = SpringContextUtil.getBean(serviceClass);
        Object result = method.invoke(bean, rpcRequest.getParams());

        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setData(result);
        rpcResponse.setDataType(method.getReturnType());
        rpcResponse.setMessage("ok");
        return rpcResponse;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Server handler exception", cause);
        ctx.close();
    }
}
