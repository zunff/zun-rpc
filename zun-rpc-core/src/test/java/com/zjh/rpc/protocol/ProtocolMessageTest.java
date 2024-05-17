package com.zjh.rpc.protocol;


import com.zjh.rpc.enums.MessageTypeEnums;
import com.zjh.rpc.enums.SerializerEnums;
import com.zjh.rpc.model.RpcRequest;
import io.vertx.core.buffer.Buffer;
import org.junit.Test;

public class ProtocolMessageTest {

    @Test
    public void testEncodeDecode() throws Exception {
        ProtocolMessage.Header header = new ProtocolMessage.Header();
        header.setSerializer((byte)SerializerEnums.JDK.getType());
        header.setType((byte) MessageTypeEnums.REQUEST.getType());

        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setServiceName("test");
        rpcRequest.setMethodName("test");
        rpcRequest.setParamTypes(new Class[]{Integer.class});
        rpcRequest.setParams(new Object[]{1});

        ProtocolMessage<RpcRequest> requestProtocolMessage = new ProtocolMessage<>(header, rpcRequest);

        Buffer encode = ProtocolMessageEncoder.encode(requestProtocolMessage);

        ProtocolMessage<?> decode = ProtocolMessageDecoder.decode(encode);

        System.out.println(decode);


    }
}