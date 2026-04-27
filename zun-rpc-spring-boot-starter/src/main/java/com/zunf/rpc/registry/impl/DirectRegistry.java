package com.zunf.rpc.registry.impl;

import com.zunf.rpc.config.RegistryConfig;
import com.zunf.rpc.model.ServiceMetaInfo;
import com.zunf.rpc.registry.Registry;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

@Slf4j
public class DirectRegistry implements Registry {

    private String host;
    private int port;

    @Override
    public void init(RegistryConfig registryConfig) {
        String address = registryConfig.getAddress();
        if (address != null && !address.isEmpty()) {
            String[] parts = address.split(":");
            this.host = parts[0];
            this.port = parts.length > 1 ? Integer.parseInt(parts[1]) : 10880;
        } else {
            this.host = "localhost";
            this.port = 10880;
        }
        log.info("DirectRegistry initialized: {}:{}", host, port);
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) {
        // 直连模式不需要注册
    }

    @Override
    public void unregister(ServiceMetaInfo serviceMetaInfo) {
        // 直连模式不需要注销
    }

    @Override
    public List<ServiceMetaInfo> discover(String serviceKey) {
        // serviceKey 格式: "serviceName:version"，提取 serviceName
        String serviceName = serviceKey.contains(":")
                ? serviceKey.substring(0, serviceKey.indexOf(":"))
                : serviceKey;

        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setHost(host);
        serviceMetaInfo.setPort(port);

        return Collections.singletonList(serviceMetaInfo);
    }

    @Override
    public void heartbeat() {
        // 直连模式不需要心跳
    }

    @Override
    public void watch(String serviceKey) {
        // 直连模式不需要监听
    }

    @Override
    public void destroy() {
        // 直连模式不需要清理
    }
}
