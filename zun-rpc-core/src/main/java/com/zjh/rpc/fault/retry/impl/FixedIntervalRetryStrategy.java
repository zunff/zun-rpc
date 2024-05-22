package com.zjh.rpc.fault.retry.impl;

import com.github.rholder.retry.*;
import com.zjh.rpc.fault.retry.RetryStrategy;
import com.zjh.rpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 固定间隔时间重试
 *
 * @author zunf
 * @date 2024/5/22 15:32
 */
@Slf4j
public class FixedIntervalRetryStrategy implements RetryStrategy {

    /**
     * 每隔三秒重试一次
     */
    private static final long RETRY_INTERVAL = 3L;

    /**
     * 最大尝试次数
     */
    private static final int MAX_RETRY_TIMES = 3;

    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                //当出现Exception异常时重试
                .retryIfExceptionOfType(Exception.class)
                //使用固定时间重试策略，每3秒重试一次
                .withWaitStrategy(WaitStrategies.fixedWait(RETRY_INTERVAL, TimeUnit.SECONDS))
                //使用最大重试次数策略，达到3次后停止重试
                .withStopStrategy(StopStrategies.stopAfterAttempt(MAX_RETRY_TIMES))
                //添加一个监听器，每次重试都打印日志
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        long attemptNumber = attempt.getAttemptNumber();
                        if (attemptNumber > 1) {
                            log.warn("远程调用服务失败，正在重试，第{}次...", attemptNumber - 1);
                        }
                    }
                })
                .build();
        return retryer.call(callable);
    }
}
