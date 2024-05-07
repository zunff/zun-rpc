package com.zjh.rpc.config;

import lombok.Data;

/**
 * 服务提供者配置
 *
 * @author zunf
 * @date 2024/5/6 10:48
 */
@Data
public class RpcConfig {

    //consumer、provider 通用配置 begin

    /**
     * 服务提供者监听端口
     */
    private int serverPort = 8088;

    /**
     * 序列化器
     */
    private String serializer = "jdk";

    //end


    //consumer begin

    /**
     * 服务提供者地址
     */
    private String serverHost = "http://localhost";

    /**
     * 是否使用模拟数据
     */
    private boolean isMock = false;

    //end


    //provider begin

    //end
}
