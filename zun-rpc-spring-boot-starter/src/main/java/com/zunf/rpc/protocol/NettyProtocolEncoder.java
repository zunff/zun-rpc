package com.zunf.rpc.protocol;

import com.zunf.rpc.constants.ProtocolConstants;
import com.zunf.rpc.enums.SerializerEnums;
import com.zunf.rpc.serializer.Serializer;
import com.zunf.rpc.serializer.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.SneakyThrows;

@io.netty.channel.ChannelHandler.Sharable
public class NettyProtocolEncoder extends MessageToByteEncoder<ProtocolMessage<?>> {

    public static final NettyProtocolEncoder INSTANCE = new NettyProtocolEncoder();

    private NettyProtocolEncoder() {
    }

    @SneakyThrows
    @Override
    protected void encode(ChannelHandlerContext ctx, ProtocolMessage<?> message, ByteBuf out) {
        ProtocolMessage.Header header = message.getHeader();

        SerializerEnums serializerEnum = SerializerEnums.of(header.getSerializer());
        if (serializerEnum == null) {
            throw new RuntimeException("序列化协议不存在");
        }
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getValue());
        byte[] body = serializer.serialize(message.getBody());

        out.writeByte(ProtocolConstants.MAGIC);
        out.writeByte(ProtocolConstants.VERSION);
        out.writeByte(header.getSerializer());
        out.writeByte(header.getType());
        out.writeByte(header.getStatus());
        out.writeLong(header.getRequestId());
        out.writeInt(body.length);
        out.writeBytes(body);
    }
}
