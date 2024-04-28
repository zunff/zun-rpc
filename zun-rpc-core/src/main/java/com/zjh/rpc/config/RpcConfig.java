package com.zjh.rpc.config;

import lombok.Data;

/**
 * RPC框架配置
 */
@Data
public class RpcConfig {

    /**
     * 服务名
     */
    private String name = "zun-rpc-example";

    /**
     * 版本号
     */
    private String version = "1.0";

    /**
     * Web服务器地址
     */
    private String serverHost = "http://localhost";

    /**
     * Web服务器端口
     */
    private Integer serverPort = 8088;
}
