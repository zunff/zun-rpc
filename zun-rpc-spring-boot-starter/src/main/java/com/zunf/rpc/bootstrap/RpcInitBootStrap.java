package com.zunf.rpc.bootstrap;

import com.zunf.rpc.config.RegistryConfig;
import com.zunf.rpc.config.RpcConfig;
import com.zunf.rpc.constants.RpcConstants;
import com.zunf.rpc.registry.Registry;
import com.zunf.rpc.registry.RegistryFactory;
import com.zunf.rpc.utils.ConfigUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * ZunRpc框架启动
 *
 * @author zunf
 * @date 2024/5/23 14:00
 */
@Slf4j
public class RpcInitBootStrap implements ImportBeanDefinitionRegistrar {

    /**
     * Spring IOC 容器初始化时调用
     *
     * @param importingClassMetadata 导入的类的元信息
     * @param beanDefinitionRegistry 注册器，用于注册对象
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        // 初始化配置和注册中心，并将RpcConfig注册进IOC
        RpcConfig rpcConfig;
        //读取配置对象
        try {
            rpcConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstants.CONFIG_PREFIX);
            rpcConfig.setRegistryConfig(ConfigUtils.loadConfig(RegistryConfig.class, RpcConstants.REGISTRY_CONFIG_PREFIX));
            //注册进IOC
            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(RpcConfig.class);
            RpcConfig finalRpcConfig = rpcConfig;
            beanDefinition.setInstanceSupplier(() -> finalRpcConfig);

            beanDefinitionRegistry.registerBeanDefinition("rpcConfig", beanDefinition);

        } catch (Exception e) {
            //配置初始化失败，使用默认值
            rpcConfig = new RpcConfig();
            log.error("rpc init config error, using default config");
        }

        //注册中心初始化
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getType());
        registry.init(registryConfig);
        log.info("rpc init config = {}", registry);
        //初始化结束后，创建 ShutdownHook ,JVM退出时执行
        Runtime.getRuntime().addShutdownHook(new Thread(registry::destroy));
    }
}
