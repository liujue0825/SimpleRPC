package com.lj.rpc.core.factory;

import com.lj.rpc.core.fallback.CircuitBreaker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author liujue
 * @version 1.0
 * @since 2024/7/25
 */
public final class FallbackFactory {

    private FallbackFactory() {}

    private static final Map<String, CircuitBreaker> circuitBreakerMap = new ConcurrentHashMap<>();

    public static CircuitBreaker getCircuitBreaker(String serviceName) {
        circuitBreakerMap.computeIfAbsent(serviceName,
                key -> new CircuitBreaker(3, 0.5, 10000) );
        return circuitBreakerMap.get(serviceName);
    }
}
