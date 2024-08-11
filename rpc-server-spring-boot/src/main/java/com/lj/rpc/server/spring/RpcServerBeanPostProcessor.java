package com.lj.rpc.server.spring;

import com.lj.rpc.core.entity.ServiceMessage;
import com.lj.rpc.core.registry.ServiceRegistry;
import com.lj.rpc.core.util.ServiceUtils;
import com.lj.rpc.server.annotation.RpcService;
import com.lj.rpc.server.cache.LocalServiceCache;
import com.lj.rpc.server.config.RpcServerProperties;
import com.lj.rpc.server.transport.RpcServer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.CommandLineRunner;

/**
 * 自定义的 RPC 服务端的后置处理器，
 * 实现将自定义的注解扫描{@link com.lj.rpc.server.annotation.RpcService}，并进行服务注册并缓存
 *
 * @author liujue
 */
@Slf4j
public class RpcServerBeanPostProcessor implements BeanPostProcessor, CommandLineRunner {

    private final ServiceRegistry serviceRegistry;

    private final RpcServer rpcServer;

    private final RpcServerProperties properties;

    public RpcServerBeanPostProcessor(ServiceRegistry serviceRegistry, RpcServer rpcServer, RpcServerProperties properties) {
        this.serviceRegistry = serviceRegistry;
        this.rpcServer = rpcServer;
        this.properties = properties;
    }

    /**
     * 开机自启动 - 此方法实现于 {@link CommandLineRunner} 接口，基于 Springboot
     *
     * @param args incoming main method arguments 命令行参数
     */
    @Override
    public void run(String... args) {
        new Thread(() -> rpcServer.start(properties.getPort())).start();
        log.info("Rpc server [{}] start, the appName is {}, the port is {}",
                rpcServer, properties.getAppName(), properties.getPort());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                // 当服务关闭之后，将服务从注册中心上清除（关闭连接）
                serviceRegistry.destroy();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }));
    }

    /**
     * 在 bean 实例化后，初始化后，检测标注有 @RpcService 注解的类，将对应的服务类进行注册，对外暴露服务，同时进行本地服务注册
     *
     * @param bean     bean
     * @param beanName beanName
     * @return 返回增强后的 bean
     * @throws BeansException Bean 异常
     */
    @SneakyThrows
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 判断当前 Bean 是否被 @RpcService 注解标注
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            log.info("[{}] is annotated with [{}].", bean.getClass().getName(), RpcService.class.getCanonicalName());
            // 获取到该类的 @RpcService 注解
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            String interfaceName;
            if ("".equals(rpcService.interfaceName())) {
                interfaceName = rpcService.interfaceClass().getName();
            } else {
                interfaceName = rpcService.interfaceName();
            }
            String version = rpcService.version();
            String serviceName = ServiceUtils.getServiceName(interfaceName, version);
            // 构建 ServiceMessage 对象
            ServiceMessage serviceMessage = ServiceMessage.builder()
                    .appName(properties.getAppName())
                    .serviceName(serviceName)
                    .version(version)
                    .inetAddress(properties.getAddress())
                    .port(properties.getPort())
                    // NOTE: 是否幂等的属性值默认为 true, 幂等性本身由服务本身的业务逻辑保证
                    .isIdempotent(true)
                    .build();
            // 进行远程服务注册
            serviceRegistry.register(serviceMessage);
            // 进行本地服务缓存注册
            LocalServiceCache.addService(serviceName, bean);
        }
        return bean;
    }
}
