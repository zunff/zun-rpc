package com.zunf.rpc.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 服务注册元信息
 *
 * @author zunf
 * @date 2024/5/9 10:49
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ServiceMetaInfo {

    /**
     * Service全类名
     */
    private String serviceName;

    /**
     * 服务版本
     */
    private String serviceVersion = "1.0";

    /**
     * 服务分组
     */
    private String serviceGroup = "default";

    /**
     * 服务地址
     */
    private String host;

    /**
     * 服务端口
     */
    private int port;

    /**
     *
     * @return 服务的唯一标识符
     */
    public String getServiceKey() {
        return String.format("%s:%s", serviceName, serviceVersion);
    }

    /**
     *
     * @return 服务节点的唯一标识符（一个服务可能有多个节点来负载均衡）
     */
    public String getServiceNodeKey() {
        return String.format("%s/%s:%s", getServiceKey(), host, port);
    }

    /**
     *
     * @return 服务访问地址:端口
     */
    public String getServiceAddress() {
        return String.format("http://%s:%s", host, port);
    }

}
