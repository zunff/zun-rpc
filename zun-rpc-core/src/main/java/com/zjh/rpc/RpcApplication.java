package com.zjh.rpc;

import com.zjh.rpc.config.RpcConfig;
import com.zjh.rpc.constants.RpcConstants;
import com.zjh.rpc.utils.ConfigUtils;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class RpcApplication {

    private static volatile RpcConfig rpcConfig;

    public static void init() {
        RpcConfig newRpcConfig;
        try {
            newRpcConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstants.CONFIG_PREFIX);
        } catch (Exception e) {
            //配置初始化失败，使用默认值
            newRpcConfig = new RpcConfig();
            log.error("rpc init config error, using default config");
        }
        init(newRpcConfig);
    }

    public static void init(RpcConfig newRpcConfig) {
        rpcConfig = newRpcConfig;
        log.info("rpc init config = {}", newRpcConfig.toString());
    }

    public static RpcConfig getRpcConfig() {
        //双检锁机制，实现单例模式
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

