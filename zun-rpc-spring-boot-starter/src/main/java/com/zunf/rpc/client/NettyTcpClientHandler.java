package com.zunf.rpc.client;

import com.zunf.rpc.enums.MessageStatusEnums;
import com.zunf.rpc.enums.MessageTypeEnums;
import com.zunf.rpc.model.RpcResponse;
import com.zunf.rpc.protocol.ProtocolMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ChannelHandler.Sharable
public class NettyTcpClientHandler extends SimpleChannelInboundHandler<ProtocolMessage<RpcResponse>> {

    public static final NettyTcpClientHandler INSTANCE = new NettyTcpClientHandler();

    private NettyTcpClientHandler() {
    }

    static final ConcurrentHashMap<Long, CompletableFuture<ProtocolMessage<RpcResponse>>> PENDING_REQUESTS = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtocolMessage<RpcResponse> msg) {
        long requestId = msg.getHeader().getRequestId();
        CompletableFuture<ProtocolMessage<RpcResponse>> future = PENDING_REQUESTS.remove(requestId);
        if (future != null) {
            future.complete(msg);
        } else {
            log.warn("Received response for unknown requestId: {}", requestId);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.warn("Channel inactive: {}", ctx.channel().remoteAddress());
        // 清理该 channel 上所有未完成的请求是不精确的（多 channel 共享 map），
        // 但超时机制会兜底，无需在此遍历
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Client handler exception", cause);
        ctx.close();
    }

    @io.netty.channel.ChannelHandler.Sharable
    public static class HeartbeatHandler extends io.netty.channel.ChannelInboundHandlerAdapter {
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;
                if (event.state() == IdleState.WRITER_IDLE) {
                    ProtocolMessage.Header header = new ProtocolMessage.Header();
                    header.setType((byte) MessageTypeEnums.HEARTBEAT.getType());
                    header.setStatus((byte) MessageStatusEnums.OK.getType());
                    header.setRequestId(0L);
                    ProtocolMessage<Void> heartbeat = new ProtocolMessage<>(header, null);
                    ctx.writeAndFlush(heartbeat);
                    log.debug("Sent heartbeat to {}", ctx.channel().remoteAddress());
                }
            } else {
                super.userEventTriggered(ctx, evt);
            }
        }
    }
}
