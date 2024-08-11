package com.lj.rpc.core.fallback;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 服务熔断器
 *
 * @author liujue
 * @version 1.0
 * @since 2024/7/25
 */
public class CircuitBreaker {

    /**
     * 熔断状态
     */
    private enum State {

        CLOSED,

        OPEN,

        HALF_OPEN
    }

    private State state = State.CLOSED;

    private final AtomicInteger failureCount = new AtomicInteger(0);

    private final AtomicInteger successCount = new AtomicInteger(0);

    private final AtomicInteger requestCount = new AtomicInteger(0);

    /**
     * 失败次数阈值
     */
    private final int failureThreshold;

    /**
     * 半开始状态向关闭状态转换的成功率
     */
    private final double halfOpenSuccessRate;

    /**
     * 恢复时间, 单位为毫秒
     */
    private final long retryTimePeriod;

    /**
     * 上一次失败时间, 单位为毫秒
     */
    private long lastFailureTime = 0;

    public CircuitBreaker(int failureThreshold, double halfOpenSuccessRate, long retryTimePeriod) {
        this.failureThreshold = failureThreshold;
        this.halfOpenSuccessRate = halfOpenSuccessRate;
        this.retryTimePeriod = retryTimePeriod;
    }

    /**
     * 判断当前熔断器是否允许请求通过
     */
    public synchronized boolean allowRequest() {
        long currentTime = System.currentTimeMillis();
        switch (state) {
            case OPEN:
                if (currentTime - lastFailureTime > retryTimePeriod) {
                    state = State.HALF_OPEN;
                    resetCounts();
                    return true;
                }
                return false;
            case HALF_OPEN:
                requestCount.incrementAndGet();
                return true;
            case CLOSED:
            default:
                return true;
        }
    }

    /**
     * 记录成功
     */
    public synchronized void recordSuccess() {
        if (state == State.HALF_OPEN) {
            successCount.incrementAndGet();
            if (successCount.get() >= halfOpenSuccessRate * requestCount.get()) {
                state = State.CLOSED;
                resetCounts();
            }
        } else {
            resetCounts();
        }
    }

    /**
     * 记录失败
     */
    public synchronized void recordFailure() {
        failureCount.incrementAndGet();
        lastFailureTime = System.currentTimeMillis();
        if (state == State.HALF_OPEN) {
            state = State.OPEN;
            lastFailureTime = System.currentTimeMillis();
        } else if (failureCount.get() >= failureThreshold) {
            state = State.OPEN;
        }
    }

    /**
     * 重置次数
     */
    private void resetCounts() {
        failureCount.set(0);
        successCount.set(0);
        requestCount.set(0);
    }

}
