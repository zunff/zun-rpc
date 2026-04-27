package com.zunf.rpc.protocol;

import com.zunf.rpc.constants.ProtocolConstants;
import com.zunf.rpc.enums.MessageTypeEnums;
import com.zunf.rpc.enums.SerializerEnums;
import com.zunf.rpc.model.RpcRequest;
import com.zunf.rpc.model.RpcResponse;
import com.zunf.rpc.serializer.Serializer;
import com.zunf.rpc.serializer.SerializerRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.SneakyThrows;

import java.util.List;

public class NettyProtocolDecoder extends ByteToMessageDecoder {

    @SneakyThrows
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < ProtocolConstants.MESSAGE_HEAD_LENGTH) {
            return;
        }

        in.markReaderIndex();

        byte magic = in.readByte();
        if (magic != ProtocolConstants.MAGIC) {
            throw new RuntimeException("消息 magic 非法");
        }
        byte version = in.readByte();
        if (version != ProtocolConstants.VERSION) {
            throw new RuntimeException("解码器与编码器版本不一致");
        }

        ProtocolMessage.Header header = new ProtocolMessage.Header();
        header.setMagic(magic);
        header.setVersion(version);
        header.setSerializer(in.readByte());
        header.setType(in.readByte());
        header.setStatus(in.readByte());
        header.setRequestId(in.readLong());
        header.setBodyLength(in.readInt());

        if (in.readableBytes() < header.getBodyLength()) {
            in.resetReaderIndex();
            return;
        }

        byte[] body = new byte[header.getBodyLength()];
        in.readBytes(body);

        MessageTypeEnums messageTypeEnum = MessageTypeEnums.of(header.getType());
        if (messageTypeEnum == null) {
            throw new RuntimeException("消息类型不存在");
        }

        SerializerEnums serializerEnum = SerializerEnums.of(header.getSerializer());
        if (serializerEnum == null) {
            throw new RuntimeException("序列化器类型不存在");
        }
        Serializer serializer = SerializerRegistry.get(serializerEnum.getValue());

        switch (messageTypeEnum) {
            case REQUEST:
                RpcRequest rpcRequest = serializer.deserialize(body, RpcRequest.class);
                out.add(new ProtocolMessage<>(header, rpcRequest));
                break;
            case RESPONSE:
                RpcResponse rpcResponse = serializer.deserialize(body, RpcResponse.class);
                out.add(new ProtocolMessage<>(header, rpcResponse));
                break;
            case HEARTBEAT:
                out.add(new ProtocolMessage<>(header, null));
                break;
            default:
                throw new RuntimeException("不支持的消息类型: " + header.getType());
        }
    }
}
