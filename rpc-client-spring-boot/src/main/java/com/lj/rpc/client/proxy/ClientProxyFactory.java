package com.lj.rpc.client.proxy;

import com.lj.rpc.client.config.RpcClientProperties;
import com.lj.rpc.client.transport.RpcClient;
import com.lj.rpc.core.discovery.ServiceDiscovery;
import com.lj.rpc.core.util.ServiceUtils;
import net.sf.cglib.proxy.Enhancer;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端代理工厂类, 返回服务代理对象
 *
 * <p>
 * 为了提高通用性(JDK 不支持代理未实现接口的类), 根据目标类的类型来选择不同的代理模式来得到代理对象
 * </p>
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/27 17:12
 */
public class ClientProxyFactory {

    /**
     * 服务发现中心
     */
    private final ServiceDiscovery serviceDiscovery;

    /**
     * 发起调用的 RPC 客户端
     */
    private final RpcClient rpcClient;

    /**
     * 客户端相关配置属性
     */
    private final RpcClientProperties rpcClientProperties;

    /**
     * 本地缓存代理对象, key: 服务名; value: 代理对象
     */
    private static final Map<String, Object> CLIENT_PROXY_CACHE = new ConcurrentHashMap<>();

    public ClientProxyFactory(ServiceDiscovery serviceDiscovery,
                              RpcClient rpcClient,
                              RpcClientProperties rpcClientProperties) {
        this.serviceDiscovery = serviceDiscovery;
        this.rpcClient = rpcClient;
        this.rpcClientProperties = rpcClientProperties;
    }

    /**
     * 根据目标类的类型来选择不同的代理模式
     *
     * @param clazz   服务接口类型
     * @param version 版本号
     * @param <T>     代理对象的参数类型
     * @return 代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz, String version) {
        return (T) CLIENT_PROXY_CACHE.computeIfAbsent(ServiceUtils.getServiceName(clazz.getName(), version),
                serviceName -> {
            // 如果目标类是一个接口或者 是 java.lang.reflect.Proxy 的子类 则使用 JDK 动态代理
            if (clazz.isInterface() || Proxy.isProxyClass(clazz)) {
                return Proxy.newProxyInstance(clazz.getClassLoader(),
                        new Class[]{clazz},
                        new ClientInvocationHandler(serviceDiscovery, rpcClient, serviceName, rpcClientProperties));
            } else { // 否则使用 CGLIB 动态代理
                Enhancer enhancer = new Enhancer();
                enhancer.setClassLoader(clazz.getClassLoader());
                enhancer.setSuperclass(clazz);
                enhancer.setCallback(
                        new ClientMethodInterceptor(serviceDiscovery, rpcClient, serviceName, rpcClientProperties));
                return enhancer.create();
            }
        });
    }
}
