package com.lj.rpc.client.proxy;

import com.lj.rpc.client.config.RpcClientProperties;
import com.lj.rpc.client.transport.RpcClient;
import com.lj.rpc.core.discovery.ServiceDiscovery;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 基于 JDK 动态代理机制实现的客户端方法调用类
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/30 9:54
 */
public class ClientInvocationHandler implements InvocationHandler {

    /**
     * 服务发现中心
     */
    private final ServiceDiscovery serviceDiscovery;

    /**
     * 发起调用的 RPC 客户端
     */
    private final RpcClient rpcClient;

    /**
     * 服务名称(服务名 + 版本号)
     */
    private final String serviceName;

    /**
     * 客户端相关配置属性
     */
    private final RpcClientProperties properties;

    public ClientInvocationHandler(ServiceDiscovery serviceDiscovery,
                                   RpcClient rpcClient,
                                   String serviceName,
                                   RpcClientProperties properties) {
        this.serviceDiscovery = serviceDiscovery;
        this.rpcClient = rpcClient;
        this.serviceName = serviceName;
        this.properties = properties;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        // 当我们的动态代理对象调用原生方法的时候，最终实际上调用到的是 invoke() 方法，然后 invoke() 方法代替我们去调用了被代理对象的原生方法
        return RemoteMethodCall.remoteCall(serviceName, method, args, serviceDiscovery, rpcClient, properties);
    }
}
