package com.zunf.rpc.server.client;

import com.zunf.rpc.config.RpcConfig;
import com.zunf.rpc.enums.MessageStatusEnums;
import com.zunf.rpc.enums.MessageTypeEnums;
import com.zunf.rpc.enums.SerializerEnums;
import com.zunf.rpc.model.RpcRequest;
import com.zunf.rpc.model.RpcResponse;
import com.zunf.rpc.model.ServiceMetaInfo;
import com.zunf.rpc.protocol.ProtocolMessage;
import com.zunf.rpc.protocol.ProtocolMessageDecoder;
import com.zunf.rpc.protocol.ProtocolMessageEncoder;
import com.zunf.rpc.protocol.wrapper.TcpBufferHandlerWrapper;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * Tcp客户端
 *
 * @author zunf
 * @date 2024/5/22 19:48
 */
@Slf4j
public class VertxTcpClient {


    public static RpcResponse doRequest(ServiceMetaInfo serviceMetaInfo, RpcRequest request, RpcConfig rpcConfig) throws Exception {

        Vertx vertx = Vertx.vertx();
        NetClient netClient = vertx.createNetClient();
        CompletableFuture<ProtocolMessage<RpcResponse>> completableFuture = new CompletableFuture<>();
        //建立链接
        netClient.connect(serviceMetaInfo.getPort(), serviceMetaInfo.getHost(), result -> {
            if (result.succeeded()) {
                NetSocket netSocket = result.result();
                ProtocolMessage.Header header = new ProtocolMessage.Header();

                SerializerEnums serializerEnums = SerializerEnums.of(rpcConfig.getSerializer());
                if (serializerEnums == null) {
                    throw new RuntimeException("序列化器类型不存在");
                }

                header.setSerializer((byte) serializerEnums.getType());
                header.setType((byte) MessageTypeEnums.REQUEST.getType());

                //发送请求
                ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>(header, request);
                Buffer encode = ProtocolMessageEncoder.encode(protocolMessage);
                netSocket.write(encode);
                //接收响应，使用自己封装的装饰者模式加强过的能够处理半包、粘包的Handler
                netSocket.handler(new TcpBufferHandlerWrapper(buffer -> {
                    ProtocolMessage<RpcResponse> decode = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
                    completableFuture.complete(decode);
                }));
            } else {
                log.error("Fail to connect to server", result.cause());
            }
        });
        //阻塞，等到拿到了结果再往下执行
        ProtocolMessage<RpcResponse> protocolMessage = completableFuture.get();
        //关闭链接
        netClient.close();
        //处理响应结果中的异常信息，抛出异常
        handleError(protocolMessage);
        return protocolMessage.getBody();
    }

    private static void handleError(ProtocolMessage<RpcResponse> protocolMessage) {
        int status = protocolMessage.getHeader().getStatus();
        RpcResponse rpcResponse = protocolMessage.getBody();
        if (MessageStatusEnums.OK.getType() != status) {
            throw new RuntimeException(String.format("远程调用服务抛出异常：%s，异常信息：%s"
                    , rpcResponse.getExceptionClassName(), rpcResponse.getMessage()));
        }
    }

}
