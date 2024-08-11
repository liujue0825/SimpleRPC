package com.lj.rpc.core.ratelimit;

/**
 * 服务端限流
 *
 * @author liujue
 * @version 1.0
 * @since 2024/7/25
 */
public interface RateLimit {

    /**
     * 是否放行本次请求
     */
    boolean isRelease();
}
