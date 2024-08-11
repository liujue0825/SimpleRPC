package com.lj.rpc.core.extension;


import java.lang.annotation.*;

/**
 * 可扩展接口的标记，被 @SPI 注解标识的类表示为需要加载的扩展类接口
 *
 * @author liujue
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SPI {

}
