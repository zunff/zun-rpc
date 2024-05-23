package com.zunf.rpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务提供者注解（用于服务注册）
 *
 * @author zunf
 * @date 2024/5/23 14:55
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface ZunRpcService {

    /**
     * 服务接口类
     */
    Class<?> interfaceClass() default void.class;
}
