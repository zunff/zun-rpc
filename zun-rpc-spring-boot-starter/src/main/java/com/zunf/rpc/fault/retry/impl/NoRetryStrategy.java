package com.zunf.rpc.fault.retry.impl;

import com.zunf.rpc.fault.retry.RetryStrategy;
import com.zunf.rpc.model.RpcResponse;

import java.util.concurrent.Callable;

/**
 * 不重试
 *
 * @author zunf
 * @date 2024/5/22 15:25
 */
public class NoRetryStrategy implements RetryStrategy {

    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        return callable.call();
    }
}
