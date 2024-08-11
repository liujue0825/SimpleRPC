package com.lj.rpc.core.loadbalance.impl;

import com.lj.rpc.core.entity.RpcRequest;
import com.lj.rpc.core.entity.ServiceMessage;
import com.lj.rpc.core.loadbalance.AbstractLoadBalance;

import java.util.List;
import java.util.Random;

/**
 * 随机选择负载均衡策略
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/30 12:42
 */
public class RandomLoadBalance extends AbstractLoadBalance {
    final Random random = new Random();

    @Override
    protected ServiceMessage doSelect(List<ServiceMessage> services, RpcRequest request) {
        return services.get(random.nextInt(services.size()));
    }
}
