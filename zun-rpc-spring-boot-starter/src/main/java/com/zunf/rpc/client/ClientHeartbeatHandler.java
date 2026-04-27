package com.zunf.rpc.client;

import com.zunf.rpc.enums.MessageStatusEnums;
import com.zunf.rpc.enums.MessageTypeEnums;
import com.zunf.rpc.protocol.ProtocolMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class ClientHeartbeatHandler extends ChannelInboundHandlerAdapter {

    public static final ClientHeartbeatHandler INSTANCE = new ClientHeartbeatHandler();

    private ClientHeartbeatHandler() {
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                ProtocolMessage.Header header = new ProtocolMessage.Header();
                header.setType((byte) MessageTypeEnums.HEARTBEAT.getType());
                header.setStatus((byte) MessageStatusEnums.OK.getType());
                header.setRequestId(0L);
                ctx.writeAndFlush(new ProtocolMessage<>(header, null));
                log.debug("Sent heartbeat to {}", ctx.channel().remoteAddress());
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
