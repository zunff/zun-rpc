package com.zjh.rpc.config;

import lombok.Data;

/**
 * 服务注册中心配置
 *
 * @author zunf
 * @date 2024/5/9 10:47
 */
@Data
public class RegistryConfig {

    /**
     * 注册中心类型
     */
    private String type;

    /**
     * 注册中心地址
     */
    private String address;

    /**
     * 注册中心用户名
     */
    private String username;

    /**
     * 注册中心密码
     */
    private String password;

    /**
     * 超时时间（单位毫秒）
     */
    private Long timeout = 10000L;
}
