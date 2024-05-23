package com.zunf.rpc.protocol;

import com.zunf.rpc.serializer.Serializer;
import com.zunf.rpc.serializer.SerializerFactory;
import com.zunf.rpc.constants.ProtocolConstants;
import com.zunf.rpc.enums.MessageTypeEnums;
import com.zunf.rpc.enums.SerializerEnums;
import com.zunf.rpc.model.RpcRequest;
import com.zunf.rpc.model.RpcResponse;
import io.vertx.core.buffer.Buffer;
import lombok.SneakyThrows;

/**
 * 自定义协议解码器
 *
 * @author zunf
 * @date 2024/5/15 10:13
 */
public class ProtocolMessageDecoder {

    @SneakyThrows
    public static ProtocolMessage<?> decode(Buffer buffer) {

        byte magic = buffer.getByte(0);

        if (magic != ProtocolConstants.MAGIC) {
            throw new RuntimeException("消息 magic 非法");
        }

        byte version = buffer.getByte(1);
        if (version != ProtocolConstants.VERSION) {
            throw new RuntimeException("解码器与编码器版本不一致");
        }

        ProtocolMessage.Header header = new ProtocolMessage.Header();

        //传入的数字是从第几个字节开始读
        header.setMagic(magic);
        header.setVersion(version);
        header.setSerializer(buffer.getByte(2));
        header.setType(buffer.getByte(3));
        header.setStatus(buffer.getByte(4));
        header.setRequestId(buffer.getLong(5));
        header.setBodyLength(buffer.getInt(13));

        int bodyBegin = ProtocolConstants.MESSAGE_HEAD_LENGTH;
        byte[] body = buffer.getBytes(bodyBegin, bodyBegin + header.getBodyLength());

        MessageTypeEnums messageTypeEnum = MessageTypeEnums.of(header.getType());
        if (messageTypeEnum == null) {
            throw new RuntimeException("消息类型不存在");
        }

        SerializerEnums serializerEnum = SerializerEnums.of(header.getSerializer());
        if (serializerEnum == null) {
            throw new RuntimeException("序列化器类型不存在");
        }
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getValue());

        switch (messageTypeEnum) {
            case REQUEST:
                RpcRequest rpcRequest = serializer.deserialize(body, RpcRequest.class);
                return new ProtocolMessage<>(header, rpcRequest);
            case RESPONSE:
                RpcResponse rpcResponse = serializer.deserialize(body, RpcResponse.class);
                return new ProtocolMessage<>(header, rpcResponse);
            case HEARTBEAT:
            default:
                throw new RuntimeException("暂不支持该类型");
        }
    }

}
