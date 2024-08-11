package com.lj.rpc.server.annotation;

import java.lang.annotation.*;

/**
 * RPC 服务注解，用来配置 RPC 的服务提供方
 *
 * <p> 实现方式参考: {@link org.apache.dubbo.config.annotation.DubboService} </p>
 *
 * @author liujue
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RpcService {

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
