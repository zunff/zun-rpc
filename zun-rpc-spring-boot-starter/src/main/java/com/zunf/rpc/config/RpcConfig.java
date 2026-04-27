package com.zunf.rpc.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 服务提供者配置
 *
 * @author zunf
 * @date 2024/5/6 10:48
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Configuration
@ConfigurationProperties("zun.rpc")
public class RpcConfig {

    //consumer、provider 通用配置 begin

    /**
     * 服务提供者监听端口
     */
    private int serverPort = 10880;

    /**
     * 序列化器
     */
    private String serializer = "jdk";

    //end


    //consumer begin

    /**
     * 服务提供者地址
     */
    private String serverHost = "localhost";

    /**
     * 是否使用模拟数据
     */
    private boolean isMock = false;


    /**
     * 负载均衡器，默认轮询
     */
    private String loadBalancer = "roundRobin";

    /**
     * 重试策略，默认不重试
     */
    private String retryStrategy = "no";

    /**
     * 容错策略，默认快速失败
     */
    private String toleranceStrategy = "failFast";
    //end


    //provider begin

    /**
     * 服务提供者应用名
     */
    private String application;

    /**
     * 服务提供者版本号
     */
    private String version = "1.0";

    //end
}
