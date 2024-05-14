package com.zjh.rpc.proxy;

import com.zjh.rpc.RpcApplication;

import java.lang.reflect.Proxy;

public class ServiceProxyFactory {

    public static <T> T getProxy(Class<T> clazz) {
        if (RpcApplication.getRpcConfig().isMock()) {
            return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new MockServiceProxy());
        }

        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new ServiceProxy());
    }

}
