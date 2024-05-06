package com.zjh.provider;

import com.zjh.common.service.UserService;
import com.zjh.provider.service.UserServiceImpl;
import com.zjh.rpc.RpcApplication;
import com.zjh.rpc.registry.LocalRegistry;
import com.zjh.rpc.server.impl.VertxHttpServer;

public class ProviderMain {

    public static void main(String[] args) {

        //服务启动时将服务注册到注册器中
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);

        VertxHttpServer vertxHttpServer = new VertxHttpServer();

        vertxHttpServer.doStart(RpcApplication.getRpcProviderConfig().getServerPort());

    }

}
