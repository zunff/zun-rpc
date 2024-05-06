package com.zjh.rpc;

import com.zjh.rpc.config.RpcConsumerConfig;
import com.zjh.rpc.config.RpcProviderConfig;
import com.zjh.rpc.constants.RpcConstants;
import com.zjh.rpc.utils.ConfigUtils;
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
     * RPC服务端配置对象，使用单例模式
     */
    private static volatile RpcProviderConfig rpcProviderConfig;

    /**
     * RPC消费者配置对象，使用单例模式
     */
    private static volatile RpcConsumerConfig rpcConsumerConfig;

    /**
     * 无参初始化RpcProviderConfig
     */
    public static void init() {

        RpcProviderConfig newRpcProviderConfig;
        //读取配置对象
        try {
            newRpcProviderConfig = ConfigUtils.loadConfig(RpcProviderConfig.class, RpcConstants.PROVIDER_CONFIG_PREFIX);
        } catch (Exception e) {
            //配置初始化失败，使用默认值
            newRpcProviderConfig = new RpcProviderConfig();
            log.error("rpc provider init config error, using default config");
        }
        //存储配置对象
        init(newRpcProviderConfig);
    }

    /**
     * 有参初始化RpcProviderConfig
     *
     * @param newRpcProviderConfig 需要存储的RpcProviderConfig对象
     */
    public static void init(RpcProviderConfig newRpcProviderConfig) {
        rpcProviderConfig = newRpcProviderConfig;
        log.info("rpc provider init config = {}", newRpcProviderConfig.toString());
    }

    /**
     * 获取RpcProviderConfig，使用双检锁机制，实现单例模式
     *
     * @return 从.properties文件中读取到的RpcProviderConfig对象
     */
    public static RpcProviderConfig getRpcProviderConfig() {
        if (rpcProviderConfig == null) {
            synchronized (RpcApplication.class) {
                if (rpcProviderConfig == null) {
                    init();
                }
            }
        }
        return rpcProviderConfig;
    }

    /**
     * 获取RpcConsumerConfig，使用双检锁机制，实现单例模式
     *
     * @return 从.properties文件中读取到的RpcConsumerConfig对象
     */
    public static RpcConsumerConfig getRpcConsumerConfig() {
        if (rpcConsumerConfig == null) {
            synchronized (RpcApplication.class) {
                if (rpcConsumerConfig == null) {
                    RpcConsumerConfig newRpcConsumerConfig;
                    //读取配置对象
                    try {
                        newRpcConsumerConfig = ConfigUtils.loadConfig(RpcConsumerConfig.class, RpcConstants.CONSUMER_CONFIG_PREFIX);
                    } catch (Exception e) {
                        //配置初始化失败，使用默认值
                        newRpcConsumerConfig = new RpcConsumerConfig();
                        log.error("rpc consumer init config error, using default config");
                    }
                    rpcConsumerConfig = newRpcConsumerConfig;
                    log.info("rpc consumer init config = {}", newRpcConsumerConfig.toString());
                }
            }
        }
        return rpcConsumerConfig;
    } 
}

