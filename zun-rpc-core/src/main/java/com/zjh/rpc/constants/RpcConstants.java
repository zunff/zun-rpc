package com.zjh.rpc.constants;

/**
 * RPC常量
 *
 * @author zunf
 * @date 2024/5/7 11:15
 */
public interface RpcConstants {

    /**
     * 配置文件加载前缀
     */
    String CONFIG_PREFIX = "rpc";

    /**
     * 注册中心配置文件加载前缀
     */
    String REGISTRY_CONFIG_PREFIX = CONFIG_PREFIX + ".registry";

    /**
     * 心跳检测执行频率，每10s执行一次
     */
    String HEAT_BEAT_SCHEDULE_STR = "*/10 * * * * *";
}
