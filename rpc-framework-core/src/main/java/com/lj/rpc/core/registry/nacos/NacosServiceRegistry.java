package com.lj.rpc.core.registry.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.lj.rpc.core.entity.ServiceMessage;
import com.lj.rpc.core.exception.RpcException;
import com.lj.rpc.core.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * 基于 Nacos 实现服务注册功能
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/30 11:00
 */
@Slf4j
public class NacosServiceRegistry implements ServiceRegistry {

    /**
     * 名字服务
     */
    private NamingService namingService;

    public NacosServiceRegistry() {

    }

    public NacosServiceRegistry(String serveAddr) {
        try {
            namingService = NamingFactory.createNamingService(serveAddr);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * void registerInstance(String, Instance) throws NacosException;
     *
     * @param serviceMessage 服务信息
     */
    @Override
    public void register(ServiceMessage serviceMessage) {
        try {
            Instance instance = getInstance(serviceMessage);
            namingService.registerInstance(instance.getServiceName(), instance);
            log.info("Successfully registered [{}] service.", instance.getServiceName());
        } catch (Exception e) {
            throw new RpcException(String.format("An error occurred when rpc server registering [%s] service.",
                    serviceMessage.getServiceName()), e);
        }
    }

    /**
     * void deregisterInstance(String var1, Instance var2) throws NacosException;
     *
     * @param serviceMessage 服务信息
     */
    @Override
    public void unregister(ServiceMessage serviceMessage) {
        try {
            Instance instance = getInstance(serviceMessage);
            namingService.deregisterInstance(instance.getServiceName(), instance);
            log.info("Successfully unregistered {} service.", instance.getServiceName());
        } catch (Exception e) {
            throw new RpcException(e);
        }

    }

    @Override
    public void destroy() throws Exception {
        namingService.shutDown();
    }

    /**
     * 根据服务信息构造服务实例对象
     *
     * @param serviceMessage 服务信息
     * @return 实例对象
     */
    private Instance getInstance(ServiceMessage serviceMessage) {
        Instance instance = new Instance();
        instance.setServiceName(serviceMessage.getServiceName());
        instance.setIp(serviceMessage.getInetAddress());
        instance.setPort(serviceMessage.getPort());
        // 服务是否健康，和服务发现有关，默认为 true
        instance.setHealthy(true);
        return instance;
    }
}
