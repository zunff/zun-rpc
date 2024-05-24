package com.zunf.rpc.bootstrap;

import com.zunf.rpc.config.RegistryConfig;
import com.zunf.rpc.registry.Registry;
import com.zunf.rpc.registry.RegistryFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * ZunRpc框架启动
 *
 * @author zunf
 * @date 2024/5/23 14:00
 */
public class RpcInitBootStrap implements BeanPostProcessor {

    @Autowired
    private RegistryConfig registryConfig;

    /**
     * 是否已经初始化过注册器了
     */
    private boolean isInitRegistry = false;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (!isInitRegistry) {
            //注册中心初始化
            Registry registry = RegistryFactory.getInstance(registryConfig.getType());
            registry.init(registryConfig);
            //初始化结束后，创建 ShutdownHook ,JVM退出时执行
            Runtime.getRuntime().addShutdownHook(new Thread(registry::destroy));
            isInitRegistry = true;
        }
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }
}
