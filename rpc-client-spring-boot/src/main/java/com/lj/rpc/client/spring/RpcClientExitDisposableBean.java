package com.lj.rpc.client.spring;

import com.lj.rpc.core.discovery.ServiceDiscovery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;

/**
 * 客户端退出时执行额外操作类
 *
 * @author liujue
 */
@Slf4j
public class RpcClientExitDisposableBean implements DisposableBean {

    private final ServiceDiscovery serviceDiscovery;

    public RpcClientExitDisposableBean(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    @Override
    public void destroy() {
        try {
            if (serviceDiscovery != null) {
                serviceDiscovery.destroy();
            }
            log.info("Rpc client resource release completed and exited successfully.");
        } catch (Exception e) {
            log.warn("An exception occurred while executing the destroy operation when the rpc client exited, {}.",
                    e.getMessage());
        }
    }
}
