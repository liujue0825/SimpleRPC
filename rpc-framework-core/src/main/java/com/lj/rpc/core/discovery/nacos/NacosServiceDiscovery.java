package com.lj.rpc.core.discovery.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.lj.rpc.core.entity.RpcRequest;
import com.lj.rpc.core.entity.ServiceMessage;
import com.lj.rpc.core.discovery.ServiceDiscovery;
import com.lj.rpc.core.exception.RpcException;
import com.lj.rpc.core.loadbalance.LoadBalance;
import com.lj.rpc.core.util.ServiceUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 基于 Nacos 实现服务发现功能
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/30 10:39
 */
@Slf4j
public class NacosServiceDiscovery implements ServiceDiscovery {

    /**
     * 名字服务
     */
    private NamingService namingService;

    /**
     * 负载均衡算法
     */
    private LoadBalance loadBalance;

    /**
     * 服务列表的本地缓存, key: 服务名称; value: 服务列表
     */
    private final Map<String, List<ServiceMessage>> serviceCache = new ConcurrentHashMap<>();

    public NacosServiceDiscovery() {
    }

    public NacosServiceDiscovery(String serverAddr, LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
        try {
            namingService = NamingFactory.createNamingService(serverAddr);
        } catch (NacosException e) {
            throw new RpcException(e);
        }
    }

    @Override
    public ServiceMessage discover(RpcRequest request) {
        return loadBalance.select(getAllServices(request.getServiceName()), request);
    }

    @Override
    public List<ServiceMessage> getAllServices(String serviceName) {
        try {
            // 先查本地缓存中有没有
            if (!serviceCache.containsKey(serviceName)) {
                // 构造 service 列表, 然后更新本地缓存
                // instance 列表 -> service 列表
                List<Instance> instances = namingService.getAllInstances(serviceName);
                List<ServiceMessage> services = instances.stream().map(ServiceUtils::toServiceMessage).collect(Collectors.toList());
                serviceCache.put(serviceName, services);

                // 注册指定服务名称下的监听事件，用来实时更新本地服务缓存列表
                namingService.subscribe(serviceName, event -> {
                    NamingEvent namingEvent = (NamingEvent) event;
                    log.info("The service [{}] cache has changed. The current number of service samples is {}."
                            , serviceName, namingEvent.getInstances().size());
                });
            }
            // 本地缓存中存在, 直接返回对应的 value
            return serviceCache.get(serviceName);
        } catch (NacosException e) {
            throw new RpcException(e);
        }
    }

    @Override
    public void destroy() throws Exception {
        namingService.shutDown();
    }

    /**
     * 检查当前请求的服务是否可以进行重试
     *
     * @param serviceName 服务名
     * @return true / false
     */
    @Override
    public boolean checkRetry(String serviceName) {
        return false;
    }
}
