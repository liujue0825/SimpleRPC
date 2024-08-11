package com.lj.rpc.core.ratelimit.impl;

import com.lj.rpc.core.ratelimit.RateLimit;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 通过令牌桶算法实现服务端限流
 *
 * @author liujue
 * @version 1.0
 * @since 2024/7/25
 */
public class TokenBucketRateLimit implements RateLimit {

    /**
     * 桶的容量
     */
    private final long capacity;

    /**
     * 每次添加的令牌数量
     */
    private final long refillTokens;

    /**
     * 添加令牌的时间间隔（单位：毫秒）
     */
    private final long refillInterval;

    /**
     * 当前令牌数量
     */
    private final AtomicLong tokens;

    /**
     * 上次添加令牌的时间
     */
    private final AtomicLong lastRefillTime;

    public TokenBucketRateLimit(long capacity,
                                long refillTokens,
                                long refillInterval) {
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillInterval = refillInterval;
        this.tokens = new AtomicLong(0);
        this.lastRefillTime = new AtomicLong(0);
    }

    @Override
    public boolean isRelease() {
        refill();
        // 如果桶中有令牌则放行请求，并减少一个令牌，否则拒绝请求
        return tokens.getAndDecrement() > 0;
    }

    /**
     * 根据时间间隔和添加数量计算并添加新令牌，确保令牌数量不超过桶的容量
     */
    private void refill() {
        long lastTime = lastRefillTime.get();
        long now = System.currentTimeMillis();
        if (now - lastTime > refillInterval) {
            long newTokens = ((now - lastTime) / refillInterval) * refillTokens;
            long newTokenCount = Math.min(tokens.get() + newTokens, capacity);
            tokens.set(newTokenCount);
            lastRefillTime.set(now);
        }
    }
}
