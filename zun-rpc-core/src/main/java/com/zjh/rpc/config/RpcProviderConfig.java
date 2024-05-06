package com.zjh.rpc.config;

import lombok.Data;

/**
 * RPC配置类
 *
 * @author zunf
 * @date 2024/5/6 00:57
 */
@Data
public class RpcProviderConfig {

    /**
     * 服务提供者服务端口
     */
    private Integer serverPort = 8088;
}
