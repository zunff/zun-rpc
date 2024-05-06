package com.zjh.rpc.config;

import lombok.Data;

/**
 * 服务提供者配置
 *
 * @author zunf
 * @date 2024/5/6 10:48
 */
@Data
public class RpcConsumerConfig {

    /**
     * 服务提供者地址
     */
    private String serverHost = "http://localhost";

    /**
     * 服务提供者端口
     */
    private String serverPort = "8088";

    /**
     * 是否使用模拟数据
     */
    private boolean isMock = false;
}
