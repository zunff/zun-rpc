package com.zunf.rpc.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 服务注册中心配置
 *
 * @author zunf
 * @date 2024/5/9 10:47
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Configuration
@ConfigurationProperties("zun.rpc.registry")
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
