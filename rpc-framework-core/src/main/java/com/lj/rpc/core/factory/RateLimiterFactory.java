package com.lj.rpc.core.factory;

import com.lj.rpc.core.ratelimit.RateLimit;
import com.lj.rpc.core.ratelimit.impl.TokenBucketRateLimit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于根据服务名生成对应的 TokenBucketRateLimit 实例
 *
 * @author liujue
 * @version 1.0
 * @since 2024/7/25
 */
public final class RateLimiterFactory {

    private final Map<String, RateLimit> rateLimiters = new ConcurrentHashMap<>();

    private final long capacity;

    private final long refillTokens;

    private final long refillInterval;

    public RateLimiterFactory(long capacity, long refillTokens, long refillInterval) {
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillInterval = refillInterval;
    }

    public RateLimit getRateLimiter(String serviceName) {
        rateLimiters.computeIfAbsent(serviceName,
                key -> new TokenBucketRateLimit(capacity, refillTokens, refillInterval));
        return rateLimiters.get(serviceName);
    }
}
