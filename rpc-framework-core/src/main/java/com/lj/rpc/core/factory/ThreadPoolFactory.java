package com.lj.rpc.core.factory;

import com.lj.rpc.core.config.ThreadPoolConfig;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 获取线程池的工厂类
 *
 * <p>
 * 由此类获取的线程池可以配置
 * </p>
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/27 16:59
 */
public final class ThreadPoolFactory {

    private ThreadPoolFactory() {

    }

    /**
     * 创建一个默认配置的线程池
     *
     * @return 线程池
     */
    public static ThreadPoolExecutor createDefaultThreadPool() {
        ThreadPoolConfig defaultConfig = new ThreadPoolConfig();
        return createThreadPool(defaultConfig);
    }

    /**
     * 创建一个自定义配置的线程池
     *
     * @param threadPoolConfig 自定义的线程池配置
     * @return 线程池
     */
    public static ThreadPoolExecutor createCustomThreadPool(ThreadPoolConfig threadPoolConfig) {
        return createThreadPool(threadPoolConfig);
    }

    /**
     * 根据配置创建线程池
     *
     * @param config 线程池配置
     * @return 线程池
     */
    private static ThreadPoolExecutor createThreadPool(ThreadPoolConfig config) {
        return new ThreadPoolExecutor(
                config.getCorePoolSize(),
                config.getMaximumPoolSize(),
                config.getKeepAliveTime(),
                config.getTimeUnit(),
                config.getWorkQueue()
        );
    }

}
