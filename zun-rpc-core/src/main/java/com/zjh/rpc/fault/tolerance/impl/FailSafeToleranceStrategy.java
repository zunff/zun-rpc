package com.zjh.rpc.fault.tolerance.impl;

import com.zjh.rpc.fault.tolerance.ToleranceStrategy;
import com.zjh.rpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 容错策略-静默处理
 *
 * @author zunf
 * @date 2024/5/22 18:44
 */
@Slf4j
public class FailSafeToleranceStrategy implements ToleranceStrategy {

    @Override
    public RpcResponse doTolerance(Map<String, Object> context, Exception e) {
        //记录一下日志
        log.error("静默处理异常", e);
        //返回一个正常的返回值
        return new RpcResponse();
    }
}
