package com.lj.rpc.client.annotation;

import java.lang.annotation.*;

/**
 * RPC 引用注解，自动注入对应的实现类, 用来配置 RPC 服务消费方
 *
 * <p> 将服务调用相关参数通过注解进行配置
 * <p> 实现方式参考: {@link org.apache.dubbo.config.annotation.DubboReference}
 *
 * @author liujue
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcReference {

    /**
     * 对外暴露服务的接口类型，默认为 void.class
     */
    Class<?> interfaceClass() default void.class;

    /**
     * 对外暴露服务的接口名（全限定名），默认为 ""
     */
    String interfaceName() default "";

    /**
     * 版本号，默认 1.0
     */
    String version() default "1.0";

}
