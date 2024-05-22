package com.zjh.rpc.fault.tolerance;

import com.zjh.rpc.model.RpcResponse;

import java.util.Map;

/**
 * 容错策略
 *
 * @author zunf
 * @date 2024/5/22 18:40
 */
public interface ToleranceStrategy {


    /**
     * 容错
     *
     * @param context 上下文，用于传递参数
     * @param e 报错的异常
     * @return
     */
    RpcResponse doTolerance(Map<String, Object> context, Exception e);
}
