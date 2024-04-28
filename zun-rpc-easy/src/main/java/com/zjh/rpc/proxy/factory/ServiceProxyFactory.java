package com.zjh.rpc.proxy.factory;

import com.zjh.rpc.proxy.ServiceProxy;

import java.lang.reflect.Proxy;
import java.util.Arrays;

public class ServiceProxyFactory {


    public static <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new ServiceProxy());
    }

}
