package com.zjh.rpc.constants;

/**
 * 容错机制常量
 *
 * @author zunf
 * @date 2024/5/22 20:05
 */
public interface ToleranceStrategyKeys {

    /**
     * 快速失败
     */
    String FAIL_FAST = "failFast";

    /**
     * 静默处理
     */
    String FAIL_SAFE = "failSafe";

    /**
     * 服务降级
     */
    String FAIL_BACK = "failBack";

    /**
     * 故障转移
     */
    String FAIL_OVER = "failOver";
}
