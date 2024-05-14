package com.zjh.rpc.registry;

import com.zjh.rpc.spi.SpiLoader;

/**
 * 注册中心工厂
 *
 * @author zunf
 * @date 2024/5/9 11:47
 */
public class RegistryFactory {
    static {
        SpiLoader.load(Registry.class);
    }

    public static Registry getInstance(String key) {
        return SpiLoader.getSpiObject(Registry.class, key);
    }
}
