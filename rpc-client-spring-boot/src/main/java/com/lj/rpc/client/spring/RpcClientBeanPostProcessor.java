package com.lj.rpc.client.spring;

import com.lj.rpc.client.annotation.RpcReference;
import com.lj.rpc.client.proxy.ClientProxyFactory;
import com.lj.rpc.core.exception.RpcException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;

/**
 * 自定义的 RPC 客户端的后置处理器
 * <p>实现将自定义的注解扫描{@link com.lj.rpc.client.annotation.RpcReference}, 并获取对应的代理对象进行替换
 *
 * @author liujue
 */
public class RpcClientBeanPostProcessor implements BeanPostProcessor {

    private final ClientProxyFactory clientProxyFactory;

    public RpcClientBeanPostProcessor(ClientProxyFactory clientProxyFactory) {
        this.clientProxyFactory = clientProxyFactory;
    }

    /**
     * 在 bean 实例化完后，扫描 bean 中需要进行 rpc 注入的属性，将对应的属性使用 代理对象 进行替换
     *
     * @param bean     bean 对象
     * @param beanName bean 名称
     * @return 后置增强后的 bean 对象
     * @throws BeansException bean 异常
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 获取该 bean 的类的所有属性
        Field[] fields = bean.getClass().getDeclaredFields();
        // 遍历所有属性
        for (Field field : fields) {
            // 判断是否被 RpcReference 注解标注
            if (field.isAnnotationPresent(RpcReference.class)) {
                // 获得 RpcReference 注解
                RpcReference rpcReference = field.getAnnotation(RpcReference.class);
                // 默认类为属性当前类型
                Class<?> clazz = field.getType();
                try {
                    // 如果指定了全限定类型接口名
                    if (!"".equals(rpcReference.interfaceName())) {
                        clazz = Class.forName(rpcReference.interfaceName());
                    }
                    // 如果指定了接口类型
                    if (rpcReference.interfaceClass() != void.class) {
                        clazz = rpcReference.interfaceClass();
                    }
                    // 获取指定类型的代理对象
                    Object proxy = clientProxyFactory.getProxy(clazz, rpcReference.version());
                    // 关闭安全检查
                    field.setAccessible(true);
                    // 设置域的值为代理对象
                    field.set(bean, proxy);
                } catch (ClassNotFoundException | IllegalAccessException e) {
                    throw new RpcException(String.format("Failed to obtain proxy object, the type of field %s is %s, " +
                            "and the specified loaded proxy type is %s.", field.getName(), field.getClass(), clazz), e);
                }
            }
        }
        return bean;
    }
}
