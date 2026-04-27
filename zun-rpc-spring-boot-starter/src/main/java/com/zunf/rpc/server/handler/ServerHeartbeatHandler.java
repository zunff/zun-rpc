package com.zunf.rpc.server.handler;

import com.zunf.rpc.enums.MessageStatusEnums;
import com.zunf.rpc.enums.MessageTypeEnums;
import com.zunf.rpc.protocol.ProtocolMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class ServerHeartbeatHandler extends ChannelInboundHandlerAdapter {

    public static final ServerHeartbeatHandler INSTANCE = new ServerHeartbeatHandler();

    private ServerHeartbeatHandler() {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ProtocolMessage) {
            ProtocolMessage<?> protocolMessage = (ProtocolMessage<?>) msg;
            if (protocolMessage.getHeader().getType() == MessageTypeEnums.HEARTBEAT.getType()) {
                ProtocolMessage.Header header = new ProtocolMessage.Header();
                header.setType((byte) MessageTypeEnums.HEARTBEAT.getType());
                header.setStatus((byte) MessageStatusEnums.OK.getType());
                header.setRequestId(protocolMessage.getHeader().getRequestId());
                ctx.writeAndFlush(new ProtocolMessage<>(header, null));
                return;
            }
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == io.netty.handler.timeout.IdleState.READER_IDLE) {
                log.warn("Reader idle, closing channel: {}", ctx.channel().remoteAddress());
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
