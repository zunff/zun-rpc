package com.zjh.rpc.server.handler;

import com.zjh.rpc.model.RpcRequest;
import com.zjh.rpc.model.RpcResponse;
import com.zjh.rpc.registry.LocalRegistry;
import com.zjh.rpc.serializer.Serializer;
import com.zjh.rpc.serializer.impl.JdkSerializer;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Vertx 请求处理器
 *
 * @author zunf
 * @date 2024/5/6 09:29
 */
public class VertxHttpServerHandler implements Handler<HttpServerRequest> {


    @Override
    public void handle(HttpServerRequest request) {
        //记录日志
        System.out.println("Received request: " + request.method() + " " + request.uri());

        //创建序列化器
        Serializer serializer = new JdkSerializer();

        //异步处理 Http 请求
        request.bodyHandler(body -> {
            RpcResponse rpcResponse = new RpcResponse();
            //反序列化数据
            RpcRequest rpcRequest = null;
            try {
                byte[] bytes = body.getBytes();
                rpcRequest = serializer.deserialize(bytes, RpcRequest.class);
            } catch (IOException e) {
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }


            if (rpcRequest == null) {
                rpcResponse.setMessage("RpcRequest为空");
                doResponse(request, rpcResponse, serializer);
                return;
            }

            //调用接口并响应
            try {
                Class<?> clazz = LocalRegistry.get(rpcRequest.getServiceName());
                Method method = clazz.getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
                Object result = method.invoke(clazz.newInstance(), rpcRequest.getParams());
                //封装返回结果
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("ok");
            } catch (Exception e) {
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }

            //响应
            doResponse(request, rpcResponse ,serializer);
        });


    }

    public void doResponse(HttpServerRequest request, RpcResponse rpcResponse, Serializer serializer) {
        HttpServerResponse httpServerResponse = request.response()
                .putHeader("content-type", "application/json");

        try {
            byte[] bytes = serializer.serialize(rpcResponse);
            httpServerResponse.end(Buffer.buffer(bytes));
        } catch (IOException e) {
            e.printStackTrace();
            httpServerResponse.end(Buffer.buffer());
        }
    }
}
