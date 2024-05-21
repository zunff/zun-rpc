package com.zjh.provider;

import com.zjh.common.service.UserService;
import com.zjh.provider.service.UserServiceImpl;
import com.zjh.rpc.RpcApplication;
import com.zjh.rpc.config.RpcConfig;
import com.zjh.rpc.model.ServiceMetaInfo;
import com.zjh.rpc.registry.LocalRegistry;
import com.zjh.rpc.registry.Registry;
import com.zjh.rpc.registry.RegistryFactory;
import com.zjh.rpc.server.WebServer;
import com.zjh.rpc.server.impl.VertxTcpServer;

/**
 * 提供者
 *
 * @author zunf
 * @date 2024/5/7 11:28
 */
public class ProviderMain2 {

    public static void main(String[] args) {

        //初始化RPC框架，读取配置文件
        RpcApplication.init();

        //服务启动时将服务对象注册到注册器中
        String serviceName = UserService.class.getName();
        LocalRegistry.register(serviceName, UserServiceImpl.class);

        //将服务信息注册到注册中心
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setHost(rpcConfig.getServerHost());
        serviceMetaInfo.setPort(rpcConfig.getServerPort() + 2);

        Registry registry = RegistryFactory.getInstance(RpcApplication.getRpcConfig().getRegistryConfig().getType());
        try {
            registry.register(serviceMetaInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 启动服务器
        WebServer webServer = new VertxTcpServer();

        webServer.doStart(RpcApplication.getRpcConfig().getServerPort() + 2);

    }

}
