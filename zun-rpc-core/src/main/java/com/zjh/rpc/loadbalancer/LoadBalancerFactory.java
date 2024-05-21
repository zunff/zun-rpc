package com.zjh.rpc.loadbalancer;

import com.zjh.rpc.spi.SpiLoader;

/**
 * 负载均衡器工厂
 *
 * @author zunf
 * @date 2024/5/21 11:11
 */
public class LoadBalancerFactory {

    static {
        SpiLoader.load(LoadBalancer.class);
    }

    public static LoadBalancer getInstance(String key) {
        return SpiLoader.getSpiObject(LoadBalancer.class, key);
    }

}
