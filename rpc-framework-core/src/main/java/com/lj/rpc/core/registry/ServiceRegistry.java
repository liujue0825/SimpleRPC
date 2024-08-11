package com.lj.rpc.core.registry;

import com.lj.rpc.core.entity.ServiceMessage;
import com.lj.rpc.core.extension.SPI;

/**
 * 服务注册接口
 * NameServer -> register(newServer);
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/30 11:00
 */
@SPI
public interface ServiceRegistry {

    /**
     * 注册服务
     *
     * @param serviceMessage 服务信息
     */
    void register(ServiceMessage serviceMessage) throws Exception;

    /**
     * 移除服务
     *
     * @param serviceMessage 服务信息
     */
    void unregister(ServiceMessage serviceMessage) throws Exception;

    /**
     * 断开连接
     */
    void destroy() throws Exception;
}
