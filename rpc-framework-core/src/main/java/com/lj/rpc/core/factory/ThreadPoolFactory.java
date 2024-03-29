package com.lj.rpc.core.factory;

import com.lj.rpc.core.config.ThreadPoolConfig;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 获取默认线程池工厂类
 * <p>
 *     注意由此类获取的线程池可以配置
 * </p>
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/27 16:59
 */
public class ThreadPoolFactory {
    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    private static ThreadPoolConfig threadPoolConfig;

    public ThreadPoolFactory() {
        threadPoolConfig = new ThreadPoolConfig();
    }

    public static ThreadPoolExecutor getDefaultThreadPool() {
        return new ThreadPoolExecutor(threadPoolConfig.getCorePoolSize(),
                threadPoolConfig.getMaximumPoolSize(),
                threadPoolConfig.getKeepAliveTime(),
                threadPoolConfig.getTimeUnit(),
                threadPoolConfig.getWorkQueue());
    }
}
