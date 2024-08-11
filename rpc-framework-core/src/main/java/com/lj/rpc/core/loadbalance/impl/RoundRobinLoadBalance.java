package com.lj.rpc.core.loadbalance.impl;

import com.lj.rpc.core.entity.RpcRequest;
import com.lj.rpc.core.entity.ServiceMessage;
import com.lj.rpc.core.loadbalance.AbstractLoadBalance;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询选择负载均衡策略
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/30 12:41
 */
public class RoundRobinLoadBalance extends AbstractLoadBalance {

    private static final AtomicInteger COUNT = new AtomicInteger();

    @Override
    protected ServiceMessage doSelect(List<ServiceMessage> services, RpcRequest request) {
        return services.get(roundCount() % services.size());
    }

    /**
     * 防止类型溢出
     */
    public final int roundCount() {
        int prev, next;
        do {
            prev = COUNT.get();
            next = prev == Integer.MAX_VALUE - 1 ? 0 : prev + 1;
        } while (!COUNT.compareAndSet(prev, next));
        return prev;
    }
}
