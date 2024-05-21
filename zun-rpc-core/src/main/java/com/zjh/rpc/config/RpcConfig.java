package com.zjh.rpc.config;

import com.zjh.rpc.loadbalancer.LoaderBalancerKeys;
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

    /**
     * 注册中心配置
     */
    private RegistryConfig registryConfig;

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
    private String loadBalancer = LoaderBalancerKeys.ROUND_ROBIN;
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
