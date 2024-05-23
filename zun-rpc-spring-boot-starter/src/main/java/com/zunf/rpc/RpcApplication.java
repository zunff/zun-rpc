package com.zunf.rpc;

import com.zunf.rpc.config.RegistryConfig;
import com.zunf.rpc.config.RpcConfig;
import com.zunf.rpc.constants.RpcConstants;
import com.zunf.rpc.registry.Registry;
import com.zunf.rpc.registry.RegistryFactory;
import com.zunf.rpc.utils.ConfigUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * RPC应用程序，用于存储全局对象
 *
 * @author zunf
 * @date 2024/5/6 10:20
 */
@Slf4j
public class RpcApplication {

    /**
     * RPC配置对象，使用单例模式
     */
    private static volatile RpcConfig rpcConfig;

    /**
     * 无参初始化RpcConfig
     */
    public static void init() {

        RpcConfig newConfig;
        //读取配置对象
        try {
            newConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstants.CONFIG_PREFIX);
            newConfig.setRegistryConfig(ConfigUtils.loadConfig(RegistryConfig.class, RpcConstants.REGISTRY_CONFIG_PREFIX));
        } catch (Exception e) {
            //配置初始化失败，使用默认值
            newConfig = new RpcConfig();
            log.error("rpc init config error, using default config");
        }
        //存储配置对象
        init(newConfig);
    }

    /**
     * 有参初始化RpcConfig
     *
     * @param newRpcConfig 需要存储的RpcConfig对象
     */
    public static void init(RpcConfig newRpcConfig) {
        rpcConfig = newRpcConfig;
        //注册中心初始化
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getType());
        registry.init(registryConfig);
        log.info("rpc init config = {}", newRpcConfig.toString());
        //初始化结束后，创建 ShutdownHook ,JVM退出时执行
        Runtime.getRuntime().addShutdownHook(new Thread(registry::destroy));
    }

    /**
     * 获取RpcConfig，使用双检锁机制，实现单例模式
     *
     * @return 从.properties文件中读取到的RpcConfig对象
     */
    public static RpcConfig getRpcConfig() {
        if (rpcConfig == null) {
            synchronized (RpcApplication.class) {
                if (rpcConfig == null) {
                    init();
                }
            }
        }
        return rpcConfig;
    }
}

