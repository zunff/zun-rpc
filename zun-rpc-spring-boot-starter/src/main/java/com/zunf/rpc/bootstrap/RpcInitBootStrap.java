package com.zunf.rpc.bootstrap;

import com.zunf.rpc.RpcApplication;
import com.zunf.rpc.annotation.EnableZunRpc;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * ZunRpc框架启动
 *
 * @author zunf
 * @date 2024/5/23 14:00
 */
public class RpcInitBootStrap implements ImportBeanDefinitionRegistrar {

    /**
     * Spring IOC 容器初始化时调用
     * @param importingClassMetadata 导入的类的元信息
     * @param registry 注册器，用于注册对象
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        //包含@EnableZunRpc注解，才会初始化
        if (importingClassMetadata.hasAnnotation(EnableZunRpc.class.getName())) {
            // 初始化配置和注册中心
            RpcApplication.init();
        }
       //todo 将RpcConfig注册进IOC

    }
}
