package com.lj.rpc.core.config;

import lombok.Data;

import java.util.concurrent.*;

/**
 * 线程池相关参数的配置类
 *
 * <p> 被 final 修饰的为线程池默认参数
 * <p> 未被 final 修饰的为线程池可配置参数
 * <p> 线程池用于处理 RPC 网络通信过程中的请求处理, 作为 CPU 密集型任务设置最大线程数为 N + 1
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/27 17:02
 */
@Data
public class ThreadPoolConfig {

    /**
     * CPU 核心数
     */
    private static final int N = Runtime.getRuntime().availableProcessors();

    /**
     * 默认核心线程数
     */
    private static final int DEFAULT_CORE_POOL_SIZE = N;

    /**
     * 默认最大线程数
     */
    private static final int DEFAULT_MAX_POOL_SIZE = N + 1;

    /**
     * 默认保持活跃时间
     */
    private static final long DEFAULT_KEEP_ALIVE_TIME = 60L;

    /**
     * 默认时间单位，秒
     */
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;

    /**
     * 默认阻塞队列大小
     */
    private static final int DEFAULT_BLOCKING_QUEUE_SIZE = 100;

    /**
     * 核心线程数
     */
    private int corePoolSize;

    /**
     * 最大线程数
     */
    private int maximumPoolSize;

    /**
     * 保持活跃时间
     */
    private long keepAliveTime;

    /**
     * 时间单位
     */
    private TimeUnit timeUnit;

    /**
     * 有界阻塞队列
     */
    private BlockingQueue<Runnable> workQueue;

    /**
     * 默认配置
     */
    public ThreadPoolConfig() {
        this.corePoolSize = DEFAULT_CORE_POOL_SIZE;
        this.maximumPoolSize = DEFAULT_MAX_POOL_SIZE;
        this.keepAliveTime = DEFAULT_KEEP_ALIVE_TIME;
        this.timeUnit = TimeUnit.SECONDS;
        this.workQueue = new ArrayBlockingQueue<>(DEFAULT_BLOCKING_QUEUE_SIZE);
    }

    /**
     * 自定义配置
     */
    public ThreadPoolConfig(long keepAliveTime,
                            int corePoolSize,
                            int maximumPoolSize,
                            TimeUnit timeUnit,
                            BlockingQueue<Runnable> workQueue) {
        this.keepAliveTime = keepAliveTime;
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.timeUnit = timeUnit;
        this.workQueue = workQueue;
    }
}
