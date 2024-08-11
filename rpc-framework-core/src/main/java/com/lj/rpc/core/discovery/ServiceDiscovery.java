package com.lj.rpc.core.discovery;

import com.lj.rpc.core.entity.RpcRequest;
import com.lj.rpc.core.entity.ServiceMessage;
import com.lj.rpc.core.extension.SPI;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务发现接口
 * NameServer -> getAllServer();
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/30 10:29
 */
@SPI
public interface ServiceDiscovery {

    /**
     * 服务发现
     *
     * @param request RPC 请求, 封装有服务名称 serviceName
     * @return 服务方提供的信息
     */
    ServiceMessage discover(RpcRequest request);

    /**
     * 得到当前服务的所有提供方, 如果未实现, 默认返回空集合
     *
     * @param serviceName 服务名
     * @return 所有服务提供方的信息
     */
    default List<ServiceMessage> getAllServices(String serviceName) throws Exception {
        return new ArrayList<>();
    }

    /**
     * 服务关闭
     */
    void destroy() throws Exception;

    /**
     * 检查当前请求的服务是否可以进行重试
     *
     * @param serviceName 服务名
     * @return true / false
     */
    boolean checkRetry(String serviceName);
}
