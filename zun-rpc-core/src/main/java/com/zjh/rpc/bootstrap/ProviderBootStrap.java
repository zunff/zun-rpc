package com.zjh.rpc.bootstrap;

import com.zjh.rpc.RpcApplication;
import com.zjh.rpc.config.RpcConfig;
import com.zjh.rpc.model.ServiceMetaInfo;
import com.zjh.rpc.registry.LocalRegistry;
import com.zjh.rpc.registry.Registry;
import com.zjh.rpc.registry.RegistryFactory;
import com.zjh.rpc.server.WebServer;
import com.zjh.rpc.server.impl.VertxTcpServer;

import java.util.List;
import java.util.Map;

/**
 * 服务提供者启动类
 *
 * @author zunf
 * @date 2024/5/23 09:49
 */
public class ProviderBootStrap {

    /**
     * 初始化服务提供者
     *
     * @param serviceMap key为接口类、value为接口实现类
     */
    public static void init(Map<Class<?>, Class<?>> serviceMap) {
        //初始化RPC框架，读取配置文件
        RpcApplication.init();

        //服务启动时将服务对象注册到注册器中
        for (Map.Entry<Class<?>, Class<?>> entry : serviceMap.entrySet()) {
            String serviceName = entry.getKey().getName();
            LocalRegistry.register(serviceName, entry.getValue());

            //将服务信息注册到注册中心
            RpcConfig rpcConfig = RpcApplication.getRpcConfig();

            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setHost(rpcConfig.getServerHost());
            serviceMetaInfo.setPort(rpcConfig.getServerPort());

            Registry registry = RegistryFactory.getInstance(RpcApplication.getRpcConfig().getRegistryConfig().getType());
            try {
                registry.register(serviceMetaInfo);
            } catch (Exception e) {
                throw new RuntimeException(serviceName + "注册到服务中心时失败：" + e);
            }
        }

        // 启动服务器
        WebServer webServer = new VertxTcpServer();

        webServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }
}
