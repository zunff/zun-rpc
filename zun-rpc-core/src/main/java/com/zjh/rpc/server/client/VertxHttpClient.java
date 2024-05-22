package com.zjh.rpc.server.client;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.zjh.rpc.config.RpcConfig;
import com.zjh.rpc.model.RpcRequest;
import com.zjh.rpc.model.RpcResponse;
import com.zjh.rpc.model.ServiceMetaInfo;
import com.zjh.rpc.serializer.Serializer;
import com.zjh.rpc.serializer.SerializerFactory;

public class VertxHttpClient {

    public static RpcResponse doRequest(ServiceMetaInfo serviceMetaInfo, RpcRequest request, RpcConfig rpcConfig) throws Exception{
        //序列化body
        Serializer serializer = SerializerFactory.getInstance(rpcConfig.getSerializer());
        byte[] bytes = serializer.serialize(request);

        try (HttpResponse httpResponse = HttpRequest
                .post(serviceMetaInfo.getServiceAddress())
                .body(bytes).execute()) {

            //处理接口执行结果并返回
            byte[] result = httpResponse.bodyBytes();
            //反序列化
            return serializer.deserialize(result, RpcResponse.class);
        }
    }
}
