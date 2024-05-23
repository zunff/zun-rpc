package com.zunf.rpc.fault.retry;

import com.zunf.rpc.spi.SpiLoader;

/**
 * 重试策略工厂
 *
 * @author zunf
 * @date 2024/5/22 15:45
 */
public class RetryStrategyFactory {

    static {
        SpiLoader.load(RetryStrategy.class);
    }


    public static RetryStrategy getInstance(String key) {
        return SpiLoader.getSpiObject(RetryStrategy.class, key);
    }
}
