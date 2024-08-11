package com.lj.rpc.spi;

import com.lj.rpc.core.entity.RpcRequest;
import com.lj.rpc.core.entity.ServiceMessage;
import com.lj.rpc.core.discovery.ServiceDiscovery;
import com.lj.rpc.core.extension.ExtensionLoader;
import com.lj.rpc.core.loadbalance.LoadBalance;
import com.lj.rpc.core.registry.ServiceRegistry;
import com.lj.rpc.core.serialization.Serialization;
import org.junit.Test;

import java.util.Arrays;

/**
 * SPI 机制测试类
 *
 * @author liujue
 */
public class SpiTest {
    @Test
    public void testServiceRegistry() {
        ExtensionLoader<ServiceRegistry> registryExtensionLoader = ExtensionLoader.getExtensionLoader(ServiceRegistry.class);
        // 下方需要依赖注入，解决（实现 IOC 或提供 空参构造方法），
        // 否则会出现 java.lang.InstantiationException: com.lj.rpc.core.registry.zookeeper.ZookeeperServiceRegistry
        // Caused by: java.lang.NoSuchMethodException: com.lj.rpc.core.registry.zookeeper.ZookeeperServiceRegistry.<init>()
        System.out.println(registryExtensionLoader.getExtension("zookeeper"));
        System.out.println(registryExtensionLoader.getExtension("nacos"));
    }

    @Test
    public void testServiceDiscovery() {
        ExtensionLoader<ServiceDiscovery> discoveryExtensionLoader = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class);
        System.out.println(discoveryExtensionLoader.getExtension("zookeeper"));
        System.out.println(discoveryExtensionLoader.getExtension("nacos"));
    }

    @Test
    public void testSerialization() {
        ExtensionLoader<Serialization> serializationExtensionLoader = ExtensionLoader.getExtensionLoader(Serialization.class);
        System.out.println(serializationExtensionLoader.getExtension("protostuff"));
    }

    @Test
    public void testLoadBalance() {
        ExtensionLoader<LoadBalance> loadBalanceExtensionLoader = ExtensionLoader.getExtensionLoader(LoadBalance.class);
        LoadBalance random = loadBalanceExtensionLoader.getExtension("random");
        System.out.println(random.select(Arrays.asList(
                ServiceMessage.builder().port(1).build(),
                ServiceMessage.builder().port(2).build(),
                ServiceMessage.builder().port(3).build()), new RpcRequest()));
        System.out.println(random);
    }
}
