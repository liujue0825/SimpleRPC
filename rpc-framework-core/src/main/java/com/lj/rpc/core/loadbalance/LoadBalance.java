package com.lj.rpc.core.loadbalance;

import com.lj.rpc.core.common.RpcRequest;
import com.lj.rpc.core.common.ServiceMessage;
import com.lj.rpc.core.extension.SPI;

import java.util.List;

/**
 * 负载均衡算法实现接口
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/30 12:25
 */
@SPI
public interface LoadBalance {

    /**
     * 从服务列表中基于负载均衡策略选择一个服务返回
     *
     * @param services 服务列表
     * @param request  RPC 请求
     * @return 服务信息对象
     */
    ServiceMessage select(List<ServiceMessage> services, RpcRequest request);
}
