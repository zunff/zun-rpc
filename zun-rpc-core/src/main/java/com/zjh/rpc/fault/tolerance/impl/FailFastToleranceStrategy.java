package com.zjh.rpc.fault.tolerance.impl;

import com.zjh.rpc.fault.tolerance.ToleranceStrategy;
import com.zjh.rpc.model.RpcResponse;

import java.util.Map;

/**
 * 容错策略-快速失败
 *
 * @author zunf
 * @date 2024/5/22 18:43
 */
public class FailFastToleranceStrategy implements ToleranceStrategy {

    @Override
    public RpcResponse doTolerance(Map<String, Object> context, Exception e) {
        throw new RuntimeException("调用远程服务报错", e);
    }
}
