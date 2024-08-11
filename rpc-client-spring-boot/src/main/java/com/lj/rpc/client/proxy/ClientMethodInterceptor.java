package com.lj.rpc.client.proxy;

import com.lj.rpc.client.config.RpcClientProperties;
import com.lj.rpc.client.transport.RpcClient;
import com.lj.rpc.core.discovery.ServiceDiscovery;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * 基于 CGLIB 实现动态代理机制
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/30 9:56
 */
public class ClientMethodInterceptor implements MethodInterceptor {

    /**
     * 服务发现中心
     */
    private final ServiceDiscovery serviceDiscovery;

    /**
     * 发起调用的 RPC 客户端
     */
    private final RpcClient rpcClient;

    /**
     * 服务名
     */
    private final String serviceName;

    /**
     * 客户端相关配置属性
     */
    private final RpcClientProperties properties;

    public ClientMethodInterceptor(ServiceDiscovery serviceDiscovery,
                                   RpcClient rpcClient,
                                   String serviceName,
                                   RpcClientProperties properties) {
        this.serviceDiscovery = serviceDiscovery;
        this.rpcClient = rpcClient;
        this.serviceName = serviceName;
        this.properties = properties;
    }

    /**
     * 拦截增强被代理类的方法, 作用和 invoke() 方法类似
     */
    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) {
        return RemoteMethodCall.remoteCall(serviceName, method, objects, serviceDiscovery, rpcClient, properties);
    }
}
