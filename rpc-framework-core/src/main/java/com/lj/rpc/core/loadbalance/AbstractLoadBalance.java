package com.lj.rpc.core.loadbalance;

import com.lj.rpc.core.entity.RpcRequest;
import com.lj.rpc.core.entity.ServiceMessage;

import java.util.List;

/**
 * 负载均衡策略抽象类, 支持用户自定义负载均衡策略
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/30 12:38
 */
public abstract class AbstractLoadBalance implements LoadBalance {

    @Override
    public ServiceMessage select(List<ServiceMessage> services, RpcRequest request) {
        // 未注册服务信息, 返回 null
        if (services == null || services.isEmpty()) {
            return null;
        }
        // 只有一组服务, 直接返回
        if (services.size() == 1) {
            return services.get(0);
        }
        return doSelect(services, request);
    }

    protected abstract ServiceMessage doSelect(List<ServiceMessage> services, RpcRequest request);
}
