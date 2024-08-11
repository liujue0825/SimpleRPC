package com.lj.rpc.client.retry;


import com.github.rholder.retry.*;
import com.lj.rpc.client.entity.RequestMetaData;
import com.lj.rpc.client.transport.RpcClient;
import com.lj.rpc.core.protocol.RpcMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 失败重试机制类, 实现在发送请求后出现问题时框架自动进行请求重试
 *
 * @author liujue
 * @version 1.0
 * @since 2024/7/25
 */
@Slf4j
public class FailureRetry {

    /**
     * 重试任务
     *
     * @param request   重试任务需要的参数
     * @param rpcClient 重试任务的调用方
     * @return 重试任务的结果
     */
    public RpcMessage retry(RequestMetaData request, RpcClient rpcClient) {
        Retryer<RpcMessage> retryer = RetryerBuilder.<RpcMessage>newBuilder()
                // 重试所有异常
                .retryIfException()
                // 如果返回结果为 null, 则重试
                .retryIfResult(Objects::isNull)
                // 设置重试间隔时间为 1 秒
                .withWaitStrategy(WaitStrategies.fixedWait(1, TimeUnit.SECONDS))
                // 设置最大重试次数为 3 次
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                .build();
        try {
            RpcMessage result = retryer.call(() -> rpcClient.sendRequest(request));
            log.info("Retry succeeded with result: {}", result);
            return result;
        } catch (ExecutionException | RetryException e) {
            log.error("Retry failed !", e);
        }
        return null;
    }
}

