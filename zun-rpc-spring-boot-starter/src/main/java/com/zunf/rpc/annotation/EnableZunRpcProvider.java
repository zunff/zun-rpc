package com.zunf.rpc.annotation;

import com.zunf.rpc.bootstrap.RpcInitBootStrap;
import com.zunf.rpc.bootstrap.RpcProviderBootStrap;
import com.zunf.rpc.utils.SpringContextUtil;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用 Rpc 服务提供者
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({RpcInitBootStrap.class, SpringContextUtil.class, RpcProviderBootStrap.class})
public @interface EnableZunRpcProvider {

}
