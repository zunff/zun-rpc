package com.zunf.rpc.bootstrap;

import com.zunf.rpc.annotation.ZunRpcService;
import com.zunf.rpc.config.RpcConfig;
import com.zunf.rpc.model.ServiceMetaInfo;
import com.zunf.rpc.registry.LocalRegistry;
import com.zunf.rpc.registry.Registry;
import com.zunf.rpc.registry.RegistryFactory;
import com.zunf.rpc.server.WebServer;
import com.zunf.rpc.server.impl.VertxTcpServer;
import com.zunf.rpc.utils.SpringContextUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 服务提供者驱动
 *
 * @author zunf
 * @date 2024/5/23 15:33
 */
public class RpcProviderBootStrap implements BeanPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private boolean webServerIsStarted = false;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        Class<?> beanClass = bean.getClass();
        ZunRpcService zunRpcService = beanClass.getAnnotation(ZunRpcService.class);
        if (zunRpcService != null) {
            //1.类标识了我们的注解，将服务注册到注册中心
            //因为这个方法调用时正在构建IOC容器，所以无法从我们封装的工具类中获取RpcConfig
            RpcConfig rpcConfig = applicationContext.getBean(RpcConfig.class);;

            Class<?> interfaceClass = zunRpcService.interfaceClass();
            //没有指定，就自己获取实现接口的第一个
            if (interfaceClass == void.class) {
                interfaceClass = beanClass.getInterfaces()[0];
            }
            String serviceName = interfaceClass.getName();

            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setHost(rpcConfig.getServerHost());
            serviceMetaInfo.setPort(rpcConfig.getServerPort());

            Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getType());
            try {
                registry.register(serviceMetaInfo);
            } catch (Exception e) {
                throw new RuntimeException(serviceName + "注册到服务中心时失败：" + e);
            }

            //2.注册到本地注册器，因为传过来的是接口名称（UserService），需要映射为Class对象
            LocalRegistry.register(serviceName, beanClass);

            //3.如果没有启动Web服务器，启动
            if (!webServerIsStarted) {
                WebServer webServer = new VertxTcpServer();
                webServer.doStart(rpcConfig.getServerPort());
                webServerIsStarted = true;
            }
        }

        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
