package com.zjh.rpc.constants;

/**
 * 重试策略常量
 *
 * @author zunf
 * @date 2024/5/22 15:49
 */
public interface RetryStrategyKeys {

    /**
     * 不重试
     */
    String NO = "no";

    /**
     * 固定间隔重试
     */
    String FIXED_INTERVAL = "fixedInterval";
}
