package com.zjh.rpc.constants;

/**
 * 负载均衡器键名常量
 *
 * @author zunf
 * @date 2024/5/21 11:16
 */
public interface LoaderBalancerKeys {

    /**
     * 轮询
     */
    String ROUND_ROBIN = "roundRobin";

    /**
     * 随机
     */
    String RANDOM = "random";

    /**
     * 一致性Hash
     */
    String CONSISTENT_HASH = "consistentHash";
}
