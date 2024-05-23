package com.zunf.rpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务消费者注解（用于注入服务）
 *
 * @author zunf
 * @date 2024/5/23 14:57
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ZunRpcReference {

    /**
     * 服务接口类
     */
    Class<?> interfaceClass() default void.class;
}
