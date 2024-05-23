package com.zjh.provider;

import com.zjh.common.service.UserService;
import com.zjh.provider.service.UserServiceImpl;
import com.zjh.rpc.RpcApplication;
import com.zjh.rpc.bootstrap.ProviderBootStrap;
import com.zjh.rpc.config.RpcConfig;
import com.zjh.rpc.model.ServiceMetaInfo;
import com.zjh.rpc.registry.LocalRegistry;
import com.zjh.rpc.registry.Registry;
import com.zjh.rpc.registry.RegistryFactory;
import com.zjh.rpc.server.WebServer;
import com.zjh.rpc.server.impl.VertxHttpServer;
import com.zjh.rpc.server.impl.VertxTcpServer;

import java.util.HashMap;
import java.util.Map;

/**
 * 提供者
 *
 * @author zunf
 * @date 2024/5/7 11:28
 */
public class ProviderMain {

    public static void main(String[] args) {
        Map<Class<?>, Class<?>> serviceMap = new HashMap<>();
        serviceMap.put(UserService.class, UserServiceImpl.class);
        ProviderBootStrap.init(serviceMap);
    }

}
