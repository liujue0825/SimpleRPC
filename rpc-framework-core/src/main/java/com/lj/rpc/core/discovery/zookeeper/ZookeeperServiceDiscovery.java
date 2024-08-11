package com.lj.rpc.core.discovery.zookeeper;

import com.lj.rpc.core.entity.RpcRequest;
import com.lj.rpc.core.entity.ServiceMessage;
import com.lj.rpc.core.discovery.ServiceDiscovery;
import com.lj.rpc.core.exception.RpcException;
import com.lj.rpc.core.loadbalance.LoadBalance;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceCache;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.details.ServiceCacheListener;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 基于 Zookeeper 实现服务发现功能
 *
 * @author liujue
 */
@Slf4j
public class ZookeeperServiceDiscovery implements ServiceDiscovery {

    private static final int SESSION_TIMEOUT = 60 * 1000;

    private static final int CONNECT_TIMEOUT = 15 * 1000;

    private static final int BASE_SLEEP_TIME = 3 * 1000;

    private static final int MAX_RETRY = 10;

    private static final String BASE_PATH = "/lj_rpc";

    private org.apache.curator.x.discovery.ServiceDiscovery<ServiceMessage> serviceDiscovery;

    /**
     * 可以进行失败重试的服务白名单的根目录
     */
    private static final String WHITE_LIST_PATH = "/retry";

    private org.apache.curator.x.discovery.ServiceDiscovery<ServiceMessage> serviceRetryDiscovery;

    private LoadBalance loadBalance;

    private CuratorFramework client;

    /**
     * ServiceCache: 将在 zookeeper 中的服务数据缓存至本地，并监听服务变化，实时更新缓存
     * <p>
     * 服务本地缓存，将服务缓存到本地并增加 watch 事件，当远程服务发生改变时自动更新服务缓存
     */
    private final Map<String, ServiceCache<ServiceMessage>> serviceCacheMap = new ConcurrentHashMap<>();

    /**
     * 用来将服务列表缓存到本地内存
     * 当服务发生变化时，由 serviceCache 进行服务列表更新操作；
     * 而当 zookeeper 挂掉时，将保存当前服务列表以便继续提供服务
     */
    private final Map<String, List<ServiceMessage>> serviceMap = new ConcurrentHashMap<>();


    /**
     * 构造方法，传入 zookeeper 的连接地址，如：127.0.0.1:2181
     *
     * @param registryAddress zookeeper 的连接地址
     */
    public ZookeeperServiceDiscovery(String registryAddress, LoadBalance loadBalance) {
        try {
            this.loadBalance = loadBalance;

            // 创建 zookeeper 客户端
            client = CuratorFrameworkFactory
                    .newClient(registryAddress, SESSION_TIMEOUT, CONNECT_TIMEOUT,
                            new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRY));
            // 开启客户端通信
            client.start();

            // 构建 ServiceDiscovery 服务发现中心
            serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceMessage.class)
                    .client(client)
                    .serializer(new JsonInstanceSerializer<>(ServiceMessage.class))
                    .basePath(BASE_PATH)
                    .build();
            // 开启服务发现
            serviceDiscovery.start();

            // 构建处理可重试服务的服务注册中心
            serviceRetryDiscovery = ServiceDiscoveryBuilder.builder(ServiceMessage.class)
                    .client(client)
                    .serializer(new JsonInstanceSerializer<>(ServiceMessage.class))
                    .basePath(BASE_PATH + WHITE_LIST_PATH)
                    .build();
            serviceRetryDiscovery.start();
        } catch (Exception e) {
            log.error("An error occurred while starting the zookeeper discover: ", e);
        }
    }

    @Override
    public ServiceMessage discover(RpcRequest request) {
        try {
            return loadBalance.select(getAllServices(request.getServiceName()), request);
        } catch (Exception e) {
            throw new RpcException(String.format("Remote service discover did not find service %s.",
                    request.getServiceName()), e);
        }
    }

    @Override
    public List<ServiceMessage> getAllServices(String serviceName) throws Exception {
        if (!serviceCacheMap.containsKey(serviceName)) {
            // 构建本地服务缓存
            ServiceCache<ServiceMessage> serviceCache = serviceDiscovery.serviceCacheBuilder()
                    .name(serviceName)
                    .build();
            // 添加服务监听，当服务发生变化时主动更新本地缓存并通知
            serviceCache.addListener(new ServiceCacheListener() {
                @Override
                public void cacheChanged() {
                    log.info("The service [{}] cache has changed. The current number of service samples is {}."
                            , serviceName, serviceCache.getInstances().size());
                    // 更新本地缓存的服务列表
                    serviceMap.put(serviceName, serviceCacheMap.get(serviceName).getInstances()
                            .stream()
                            .map(ServiceInstance::getPayload)
                            .collect(Collectors.toList()));
                }

                @Override
                public void stateChanged(CuratorFramework client, ConnectionState newState) {
                    // 当连接状态发生改变时，只打印提示信息，保留本地缓存的服务列表
                    log.info("The client {} connection status has changed. The current status is: {}."
                            , client, newState);
                }
            });
            // 开启服务缓存监听
            serviceCache.start();
            // 将服务缓存对象存入本地
            serviceCacheMap.put(serviceName, serviceCache);
            // 将服务列表缓存到本地
            serviceMap.put(serviceName, serviceCacheMap.get(serviceName).getInstances()
                    .stream()
                    .map(ServiceInstance::getPayload)
                    .collect(Collectors.toList()));
        }
        return serviceMap.get(serviceName);
    }

    @Override
    public void destroy() throws Exception {
        for (ServiceCache<ServiceMessage> serviceCache : serviceCacheMap.values()) {
            if (serviceCache != null) {
                serviceCache.close();
            }
        }
        if (serviceDiscovery != null) {
            serviceDiscovery.close();
        }
        if (serviceRetryDiscovery != null) {
            serviceRetryDiscovery.close();
        }
        if (client != null) {
            client.close();
        }
    }

    @Override
    public boolean checkRetry(String serviceName) {
        boolean canRetry = false;
        try {
            Collection<ServiceInstance<ServiceMessage>> serviceInstances
                    = serviceRetryDiscovery.queryForInstances(serviceName);
            if (serviceInstances != null && !serviceInstances.isEmpty()) {
                log.info("当前 {} 服务存在于白名单之中, 可以重试", serviceName);
                canRetry = true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return canRetry;
    }
}
