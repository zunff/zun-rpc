package com.zjh.rpc.protocol;

import cn.hutool.core.util.IdUtil;
import com.zjh.rpc.constants.ProtocolConstants;
import com.zjh.rpc.constants.RpcConstants;
import com.zjh.rpc.enums.SerializerEnums;
import com.zjh.rpc.serializer.Serializer;
import com.zjh.rpc.serializer.SerializerFactory;
import io.vertx.core.buffer.Buffer;
import lombok.SneakyThrows;

import java.io.IOException;

/**
 * 自定义协议的消息编码器
 *
 * @author zunf
 * @date 2024/5/15 09:37
 */
public class ProtocolMessageEncoder {

    /**
     * 对 RpcRequest 或 RpcResponse 编码成 Buffer 对象
     * @param message RpcRequest 或 RpcResponse对象
     * @return 编码结果
     */
    @SneakyThrows
    public static Buffer encode(ProtocolMessage<?> message) {
        ProtocolMessage.Header header = message.getHeader();
        Buffer buffer = Buffer.buffer();
        buffer.appendByte(ProtocolConstants.MAGIC);
        buffer.appendByte(ProtocolConstants.VERSION);
        buffer.appendByte(header.getSerializer());
        buffer.appendByte(header.getType());
        buffer.appendByte(header.getStatus());
        buffer.appendLong(IdUtil.getSnowflakeNextId());

        SerializerEnums serializerEnum = SerializerEnums.of(header.getSerializer());
        if (serializerEnum == null) {
            throw new RuntimeException("序列化协议不存在");
        }
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getValue());
        byte[] body = serializer.serialize(message.getBody());
        //写入body长度
        buffer.appendInt(body.length);
        //写入body
        buffer.appendBytes(body);
        return buffer;
    }
}
