package com.zjh.rpc.serializer;

import com.zjh.rpc.spi.SpiLoader;

/**
 * 序列化器工厂
 *
 * @author zunf
 * @date 2024/5/7 10:46
 */
public class SerializerFactory {

    static {
        SpiLoader.load(Serializer.class);
    }

    public static Serializer getInstance(String key) {
        return SpiLoader.getSpiObject(Serializer.class, key);
    }

}
