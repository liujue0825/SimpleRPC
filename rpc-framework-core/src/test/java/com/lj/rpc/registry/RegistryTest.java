package com.lj.rpc.registry;

import com.lj.rpc.core.entity.ServiceMessage;
import com.lj.rpc.core.registry.ServiceRegistry;
import com.lj.rpc.core.registry.zookeeper.ZookeeperServiceRegistry;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author liujue
 * @version 1.0
 * @description 注册中心测试
 * @since 2024/7/17
 */
public class RegistryTest {

    public static void main(String[] args) throws Exception {
        ServiceRegistry serviceRegistry = new ZookeeperServiceRegistry("121.40.251.235:2181");

        ServiceMessage serviceInfo = ServiceMessage.builder()
                .appName("rpc")
                .serviceName("com.lj.rpc.api.service.HelloService")
                .version("1.0")
                .inetAddress(InetAddress.getLocalHost().getHostAddress())
                .port(8081)
                .build();

        serviceRegistry.register(serviceInfo);

        TimeUnit.SECONDS.sleep(3);

        serviceRegistry.unregister(serviceInfo);

        TimeUnit.SECONDS.sleep(3);

        serviceRegistry.destroy();
    }
}
