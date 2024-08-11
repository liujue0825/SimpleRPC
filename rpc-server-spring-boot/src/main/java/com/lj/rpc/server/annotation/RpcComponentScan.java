package com.lj.rpc.server.annotation;

import com.lj.rpc.server.spring.RpcComponentScanRegistrar;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.annotation.*;

/**
 * 扫描服务注解
 *
 * <p>
 *     {@link RpcComponentScan} 类上用 {@link Import} 注解引入了 {@link RpcComponentScanRegistrar} 类，
 *     而这个类是一个 {@link ImportBeanDefinitionRegistrar} 的实现类，
 *     Spring 容器在解析该类型的 Bean 时会调用其
 *     {@link ImportBeanDefinitionRegistrar#registerBeanDefinitions(AnnotationMetadata, BeanDefinitionRegistry)} 方法，
 *     将 {@link RpcComponentScan} 注解上的信息提取成 {@link AnnotationMetadata} 以及容器注册器对象作为此方法的参数，
 *     上述流程就是自定义注解式组件扫描的关键逻辑。
 * </p>
 *
 * <p> 实现方式参考: {@link  org.apache.dubbo.config.spring.context.annotation.DubboComponentScan} </p>
 *
 * @author liujue
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(RpcComponentScanRegistrar.class)
public @interface RpcComponentScan {

    @AliasFor("basePackages")
    String[] value() default {};

    /**
     * 扫描包路径
     */
    @AliasFor("value")
    String[] basePackages() default {};

}
