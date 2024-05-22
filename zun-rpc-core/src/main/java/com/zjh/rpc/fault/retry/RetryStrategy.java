package com.zjh.rpc.fault.retry;

import com.zjh.rpc.model.RpcResponse;

import java.util.concurrent.Callable;

/**
 * 重试策略接口
 *
 * @author zunf
 * @date 2024/5/22 15:12
 */
public interface RetryStrategy {


    /**
     * 重试
     *
     * @param callable 通过一个匿名内部类来传入需要重试的事
     * @throws Exception
     */
    RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception;
}
