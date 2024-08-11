package com.lj.rpc.client.config;

import com.lj.rpc.client.proxy.ClientProxyFactory;
import com.lj.rpc.client.spring.RpcClientBeanPostProcessor;
import com.lj.rpc.client.spring.RpcClientExitDisposableBean;
import com.lj.rpc.client.transport.RpcClient;
import com.lj.rpc.client.transport.netty.NettyRpcClient;
import com.lj.rpc.core.discovery.ServiceDiscovery;
import com.lj.rpc.core.discovery.nacos.NacosServiceDiscovery;
import com.lj.rpc.core.discovery.zookeeper.ZookeeperServiceDiscovery;
import com.lj.rpc.core.loadbalance.LoadBalance;
import com.lj.rpc.core.loadbalance.impl.ConsistentHashLoadBalance;
import com.lj.rpc.core.loadbalance.impl.RandomLoadBalance;
import com.lj.rpc.core.loadbalance.impl.RoundRobinLoadBalance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * RPC 客户端自动配置加载类
 *
 * @author liujue
 */
@Configuration
@EnableConfigurationProperties(RpcClientProperties.class)
public class RpcClientAutoConfiguration {

    private final RpcClientProperties rpcClientProperties;

    @Autowired
    public RpcClientAutoConfiguration(RpcClientProperties rpcClientProperties) {
        this.rpcClientProperties = rpcClientProperties;
    }

    /**
     * 创建 LoadBalance 的实体类 bean，没有显式配置时默认使用 roundRobin
     */
    @Bean(name = "loadBalance")
    @Primary    // 标记为默认配置
    @ConditionalOnMissingBean // 不指定 value 则值默认为当前创建的类
    @ConditionalOnProperty(prefix = "rpc.client", name = "loadbalance", havingValue = "random", matchIfMissing = true)
    public LoadBalance randomLoadBalance() {
        return new RandomLoadBalance();
    }

    @Bean(name = "loadBalance")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rpc.client", name = "loadbalance", havingValue = "roundRobin")
    public LoadBalance roundRobinLoadBalance() {
        return new RoundRobinLoadBalance();
    }

    @Bean(name = "loadBalance")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rpc.client", name = "loadbalance", havingValue = "consistentHash")
    public LoadBalance consistentHashLoadBalance() {
        return new ConsistentHashLoadBalance();
    }

    /**
     * 创建 ServiceDiscovery 的实体类 bean, 没有显式配置时默认使用 zookeeper
     */
    @Bean(name = "serviceDiscovery")
    @Primary
    @ConditionalOnMissingBean
    @ConditionalOnBean(LoadBalance.class)
    @ConditionalOnProperty(prefix = "rpc.client", name = "registry", havingValue = "zookeeper", matchIfMissing = true)
    public ServiceDiscovery zookeeperServiceDiscovery(@Autowired LoadBalance loadBalance) {
        return new ZookeeperServiceDiscovery(rpcClientProperties.getRegistryAddr(), loadBalance);
    }

    @Bean(name = "serviceDiscovery")
    @ConditionalOnMissingBean
    @ConditionalOnBean(LoadBalance.class)
    @ConditionalOnProperty(prefix = "rpc.client", name = "registry", havingValue = "nacos")
    public ServiceDiscovery nacosServiceDiscovery(@Autowired LoadBalance loadBalance) {
        return new NacosServiceDiscovery(rpcClientProperties.getRegistryAddr(), loadBalance);
    }

    /**
     * 创建 RpcClient 的实体类 bean, 没有显式配置时默认使用 netty
     */
    @Bean(name = "rpcClient")
    @Primary
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rpc.client", name = "transport", havingValue = "netty", matchIfMissing = true)
    public RpcClient nettyRpcClient() {
        return new NettyRpcClient();
    }

    /**
     * 创建 ClientProxyFactory 的实体类 Bean
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ServiceDiscovery.class, RpcClient.class})
    public ClientProxyFactory clientProxyFactory(@Autowired ServiceDiscovery serviceDiscovery,
                                                 @Autowired RpcClient rpcClient,
                                                 @Autowired RpcClientProperties rpcClientProperties) {
        return new ClientProxyFactory(serviceDiscovery, rpcClient, rpcClientProperties);
    }

    /**
     * 注册自定义的后置处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public RpcClientBeanPostProcessor rpcClientBeanPostProcessor(@Autowired ClientProxyFactory clientProxyFactory) {
        return new RpcClientBeanPostProcessor(clientProxyFactory);
    }

    /**
     * 注册自定义的 DisposableBean
     */
    @Bean
    @ConditionalOnMissingBean
    public RpcClientExitDisposableBean rpcClientExitDisposableBean(@Autowired ServiceDiscovery serviceDiscovery) {
        return new RpcClientExitDisposableBean(serviceDiscovery);
    }

}
