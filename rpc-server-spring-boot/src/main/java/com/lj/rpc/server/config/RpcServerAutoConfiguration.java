package com.lj.rpc.server.config;

import com.lj.rpc.core.registry.ServiceRegistry;
import com.lj.rpc.core.registry.nacos.NacosServiceRegistry;
import com.lj.rpc.core.registry.zookeeper.ZookeeperServiceRegistry;
import com.lj.rpc.server.spring.RpcServerBeanPostProcessor;
import com.lj.rpc.server.transport.RpcServer;
import com.lj.rpc.server.transport.netty.NettyRpcServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * RPC 服务端自动配置加载类
 * <pre>
 * ConditionalOnBean：是否存在某个某类或某个名字的 Bean
 * ConditionalOnMissingBean：是否缺失某个某类或某个名字的 Bean
 * ConditionalOnProperty：Environment 中是否存在某个属性
 * </pre>
 *
 * @author liujue
 */
@Configuration
@EnableConfigurationProperties(RpcServerProperties.class)
public class RpcServerAutoConfiguration {

    private final RpcServerProperties rpcServerProperties;

    @Autowired
    public RpcServerAutoConfiguration(RpcServerProperties rpcServerProperties) {
        this.rpcServerProperties = rpcServerProperties;
    }

    /**
     * 创建 ServiceRegistry 的实体类 bean, 没有显式配置时默认使用 zookeeper
     */
    @Bean(name = "serviceRegistry")
    @Primary
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rpc.server", name = "registry", havingValue = "zookeeper", matchIfMissing = true)
    public ServiceRegistry zookeeperServiceRegistry() {
        return new ZookeeperServiceRegistry(rpcServerProperties.getRegistryAddr());
    }

    @Bean(name = "serviceRegistry")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rpc.server", name = "registry", havingValue = "nacos")
    public ServiceRegistry nacosServiceRegistry() {
        return new NacosServiceRegistry(rpcServerProperties.getRegistryAddr());
    }

    /**
     * 创建 RpcServer 的实体类 bean, 没有显式配置时默认使用 netty
     */
    @Bean(name = "rpcServer")
    @Primary
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rpc.server", name = "transport", havingValue = "netty", matchIfMissing = true)
    public RpcServer nettyRpcClient() {
        return new NettyRpcServer();
    }

    /**
     * 注册自定义的后置处理器
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ServiceRegistry.class, RpcServer.class})
    public RpcServerBeanPostProcessor rpcServerBeanPostProcessor(@Autowired ServiceRegistry serviceRegistry,
                                                                 @Autowired RpcServer rpcServer,
                                                                 @Autowired RpcServerProperties properties) {
        return new RpcServerBeanPostProcessor(serviceRegistry, rpcServer, properties);
    }
}
