package com.lj.rpc.core.registry.zookeeper;

import com.lj.rpc.core.entity.ServiceMessage;
import com.lj.rpc.core.exception.RpcException;
import com.lj.rpc.core.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

/**
 * 基于 Zookeeper 实现服务注册功能
 *
 * @author liujue
 * @version 1.0
 * @date 2024/2/24 13:31
 */
@Slf4j
public class ZookeeperServiceRegistry implements ServiceRegistry {

    private static final int SESSION_TIMEOUT = 60 * 1000;

    private static final int CONNECT_TIMEOUT = 15 * 1000;

    private static final int BASE_SLEEP_TIME = 3 * 1000;

    private static final int MAX_RETRY = 10;

    private static final String BASE_PATH = "/lj_rpc";

    private ServiceDiscovery<ServiceMessage> serviceDiscovery;

    /**
     * 可以进行失败重试的服务白名单的根目录
     */
    private static final String WHITE_LIST_PATH = "/retry";

    private ServiceDiscovery<ServiceMessage> serviceRetryDiscovery;

    private CuratorFramework client;


    public ZookeeperServiceRegistry() {

    }

    /**
     * 构造方法，传入 zookeeper 的连接地址，如：127.0.0.1:2181
     *
     * @param registryAddress Zookeeper 的连接地址
     */
    public ZookeeperServiceRegistry(String registryAddress) {
        try {
            // 创建 zookeeper 客户端
            client = CuratorFrameworkFactory
                    .newClient(registryAddress, SESSION_TIMEOUT, CONNECT_TIMEOUT,
                            new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRY));
            // 开启客户端通信
            client.start();

            // 构建 ServiceDiscovery 服务注册中心
            serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceMessage.class)
                    .client(client)
                    .serializer(new JsonInstanceSerializer<>(ServiceMessage.class))
                    .basePath(BASE_PATH)
                    .build();

            serviceDiscovery.start();

            // 构建处理可重试服务的服务注册中心
            serviceRetryDiscovery = ServiceDiscoveryBuilder.builder(ServiceMessage.class)
                    .client(client)
                    .serializer(new JsonInstanceSerializer<>(ServiceMessage.class))
                    .basePath(BASE_PATH + WHITE_LIST_PATH)
                    .build();

            serviceRetryDiscovery.start();
        } catch (Exception e) {
            log.error("An error occurred while starting the zookeeper registry: ", e);
        }
    }

    /**
     * 注册服务
     *
     * @param serviceMessage 服务信息
     */
    @Override
    public void register(ServiceMessage serviceMessage) {
        try {
            ServiceInstance<ServiceMessage> serviceInstance = ServiceInstance.<ServiceMessage>builder()
                    .name(serviceMessage.getServiceName())
                    .address(serviceMessage.getInetAddress())
                    .port(serviceMessage.getPort())
                    .payload(serviceMessage)
                    .build();
            // 符合幂等性的服务还会额外保存到白名单下面
            if (serviceMessage.isIdempotent()) {
                serviceRetryDiscovery.registerService(serviceInstance);
            }
            serviceDiscovery.registerService(serviceInstance);
            log.info("Successfully registered [{}] service.", serviceInstance.getName());
        } catch (Exception e) {
            throw new RpcException(String.format("An error occurred when rpc server registering [%s] service.",
                    serviceMessage.getServiceName()), e);
        }
    }

    /**
     * 移除服务
     *
     * @param serviceMessage 服务信息
     */
    @Override
    public void unregister(ServiceMessage serviceMessage) throws Exception {
        ServiceInstance<ServiceMessage> serviceInstance = ServiceInstance.<ServiceMessage>builder()
                .name(serviceMessage.getServiceName())
                .address(serviceMessage.getInetAddress())
                .port(serviceMessage.getPort())
                .payload(serviceMessage)
                .build();
        try {
            serviceDiscovery.unregisterService(serviceInstance);
            if (serviceMessage.isIdempotent()) {
                serviceRetryDiscovery.unregisterService(serviceInstance);
            }
        } catch (Exception e) {
            throw new RpcException(String.format("An error occurred when rpc server unregistering [%s] service.",
                    serviceMessage.getServiceName()), e);
        }
        log.warn("Successfully unregistered {} service.", serviceInstance.getName());
    }

    /**
     * 断开连接
     */
    @Override
    public void destroy() throws Exception {
        serviceDiscovery.close();
        serviceRetryDiscovery.close();
        client.close();
        log.info("Destroy zookeeper registry completed.");
    }
}
