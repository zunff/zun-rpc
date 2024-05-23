package com.zunf.rpc.fault.tolerance.impl;

import com.zunf.rpc.fault.tolerance.ToleranceStrategy;
import com.zunf.rpc.model.RpcResponse;

import java.util.Map;

/**
 * 容错策略-服务降级
 *
 * @author zunf
 * @date 2024/5/22 18:49
 */
public class FailBackToleranceStrategy implements ToleranceStrategy {

    @Override
    public RpcResponse doTolerance(Map<String, Object> context, Exception e) {
        //搁置一下，还不知道降级成什么服务
        return null;
    }
}
