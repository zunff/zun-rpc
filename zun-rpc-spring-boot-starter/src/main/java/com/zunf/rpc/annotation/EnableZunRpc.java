package com.zunf.rpc.annotation;

import com.zunf.rpc.bootstrap.RpcConsumerBootStrap;
import com.zunf.rpc.bootstrap.RpcInitBootStrap;
import com.zunf.rpc.bootstrap.RpcProviderBootStrap;
import com.zunf.rpc.utils.SpringContextUtil;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用 Rpc 注解
 *
 * @author zunf
 * @date 2024/5/23 14:52
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({SpringContextUtil.class, RpcInitBootStrap.class, RpcProviderBootStrap.class, RpcConsumerBootStrap.class})
public @interface EnableZunRpc {

}
