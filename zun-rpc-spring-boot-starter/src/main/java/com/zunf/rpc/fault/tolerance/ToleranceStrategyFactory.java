package com.zunf.rpc.fault.tolerance;

import com.zunf.rpc.spi.SpiLoader;

/**
 * 容错机制对象工厂
 *
 * @author zunf
 * @date 2024/5/22 20:03
 */
public class ToleranceStrategyFactory {

    static {
        SpiLoader.load(ToleranceStrategy.class);
    }

    public static ToleranceStrategy getInstance(String key) {
        return SpiLoader.getSpiObject(ToleranceStrategy.class, key);
    }
}
