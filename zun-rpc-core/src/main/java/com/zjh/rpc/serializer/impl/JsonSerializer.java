package com.zjh.rpc.serializer.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjh.rpc.model.RpcRequest;
import com.zjh.rpc.model.RpcResponse;
import com.zjh.rpc.serializer.Serializer;

import java.io.IOException;

/**
 * Json序列化器
 *
 * @author zunf
 * @date 2024/5/6 15:16
 */
public class JsonSerializer implements Serializer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public <T> byte[] serialize(T object) throws IOException {
        return OBJECT_MAPPER.writeValueAsBytes(object);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> type) throws IOException {
        T t = OBJECT_MAPPER.readValue(bytes, type);
        //RpcResponse的data属性为Object类型，RpcRequest的params为Object类型
        //在反序列化时，Object类型和泛型的类型都会被擦除，也就导致jackson不知道把它反序列化成什么类型的变量
        //导致最后反序列化的结果是LinkedHashMap类型
        //所以如果想要正常得显示Object类型，需要保存原来的类型，并在反序列化时，反序列化成原来所希望的类型，而不是LinkedHashMap
        handleTypeError(t);
        return t;
    }

    /**
     * 处理RpcResponse的data、RpcRequest的params属性，被反序列化为LinkedHashMap的错误
     *
     * @param t   RpcResponse或RpcRequest对象
     * @param <T> RpcResponse或RpcRequest类
     * @throws IOException IO异常
     */
    private <T> void handleTypeError(T t) throws IOException {
        if (t instanceof RpcRequest) {
            RpcRequest rpcRequest = (RpcRequest) t;
            Class<?>[] paramTypes = rpcRequest.getParamTypes();
            Object[] params = rpcRequest.getParams();
            for (int i = 0; i < params.length; i++) {
                //如果类型不同，重新处理类型
                if (!params[i].getClass().equals(paramTypes[i])) {
                    byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(params[i]);
                    params[i] = OBJECT_MAPPER.readValue(bytes, paramTypes[i]);
                }
            }
        } else if (t instanceof RpcResponse) {
            RpcResponse rpcResponse = (RpcResponse) t;
            Object data = rpcResponse.getData();
            Class<?> dataType = rpcResponse.getDataType();
            //如果类型不同，重新处理类型
            if (!data.getClass().equals(dataType)) {
                byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(data);
                rpcResponse.setData(OBJECT_MAPPER.readValue(bytes, dataType));
            }
        }
    }
}
