package com.zunf.rpc.client;

import cn.hutool.core.util.IdUtil;
import com.zunf.rpc.enums.MessageStatusEnums;
import com.zunf.rpc.enums.MessageTypeEnums;
import com.zunf.rpc.model.RpcRequest;
import com.zunf.rpc.model.RpcResponse;
import com.zunf.rpc.model.ServiceMetaInfo;
import com.zunf.rpc.protocol.ProtocolMessage;
import com.zunf.rpc.serializer.Serializer;
import com.zunf.rpc.utils.SpringContextUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyTcpClient {

    private static final int REQUEST_TIMEOUT_SECONDS = 30;

    public static RpcResponse doRequest(ServiceMetaInfo serviceMetaInfo, RpcRequest request) throws Exception {
        Serializer serializer = SpringContextUtil.getBean(Serializer.class);

        long requestId = IdUtil.getSnowflakeNextId();

        ProtocolMessage.Header header = new ProtocolMessage.Header();
        header.setSerializer((byte) serializer.getType().getType());
        header.setType((byte) MessageTypeEnums.REQUEST.getType());
        header.setStatus((byte) MessageStatusEnums.OK.getType());
        header.setRequestId(requestId);

        ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>(header, request);

        CompletableFuture<ProtocolMessage<RpcResponse>> future = new CompletableFuture<>();
        NettyTcpClientHandler.PENDING_REQUESTS.put(requestId, future);

        Channel channel = null;
        try {
            channel = ChannelPoolManager.acquire(serviceMetaInfo.getHost(), serviceMetaInfo.getPort());
            channel.writeAndFlush(protocolMessage);

            ProtocolMessage<RpcResponse> response = future.get(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            handleError(response);
            return response.getBody();
        } catch (Exception e) {
            log.warn("RPC call failed for {}:{}, removing connection pool",
                    serviceMetaInfo.getHost(), serviceMetaInfo.getPort(), e);
            ChannelPoolManager.remove(serviceMetaInfo.getHost(), serviceMetaInfo.getPort());
            throw e;
        } finally {
            NettyTcpClientHandler.PENDING_REQUESTS.remove(requestId);
            if (channel != null) {
                ChannelPoolManager.release(serviceMetaInfo.getHost(), serviceMetaInfo.getPort(), channel);
            }
        }
    }

    private static void handleError(ProtocolMessage<RpcResponse> protocolMessage) {
        int status = protocolMessage.getHeader().getStatus();
        RpcResponse rpcResponse = protocolMessage.getBody();
        if (MessageStatusEnums.OK.getType() != status) {
            throw new RuntimeException(String.format("远程调用服务抛出异常：%s，异常信息：%s",
                    rpcResponse.getExceptionClassName(), rpcResponse.getMessage()));
        }
    }
}
