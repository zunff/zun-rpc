package com.zjh.rpc.proxy.factory;

import com.zjh.rpc.RpcApplication;
import com.zjh.rpc.proxy.MockServiceProxy;
import com.zjh.rpc.proxy.ServiceProxy;

import java.lang.reflect.Proxy;
import java.util.Arrays;

public class ServiceProxyFactory {

    public static <T> T getProxy(Class<T> clazz) {
        if (RpcApplication.getRpcConsumerConfig().isMock()) {
            return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new MockServiceProxy());
        }

        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new ServiceProxy());
    }

}
