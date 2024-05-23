package com.zunf.rpc.proxy;

import com.zunf.rpc.config.RpcConfig;
import com.zunf.rpc.utils.SpringContextUtil;

import java.lang.reflect.Proxy;

/**
 * 代理对象工厂
 *
 * @author zunf
 * @date 2024/5/23 18:27
 */
public class ServiceProxyFactory {

    public static <T> T getProxy(Class<T> clazz, RpcConfig rpcConfig) {
        if ( rpcConfig.isMock()) {
            return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new MockServiceProxy());
        }

        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new ServiceProxy());
    }

}
