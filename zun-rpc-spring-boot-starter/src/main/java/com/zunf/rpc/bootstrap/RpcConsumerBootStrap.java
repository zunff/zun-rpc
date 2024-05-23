package com.zunf.rpc.bootstrap;

import com.zunf.rpc.config.RpcConfig;
import com.zunf.rpc.proxy.ServiceProxyFactory;
import com.zunf.rpc.annotation.ZunRpcReference;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;

/**
 * Rpc消费者驱动
 *
 * @author zunf
 * @date 2024/5/23 16:02
 */
public class RpcConsumerBootStrap implements BeanPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    /**
     * Bean 初始化后执行，注入代理对象依赖
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        Field[] fields = beanClass.getDeclaredFields();
        for (Field field : fields) {
            ZunRpcReference zunRpcReference = field.getAnnotation(ZunRpcReference.class);
            if (zunRpcReference != null) {
                //字段标识了这个注解，说明需要注入代理对象
                Class<?> interfaceClass = zunRpcReference.interfaceClass();
                //如果没有设置，获取字段的类
                if (interfaceClass == void.class) {
                    interfaceClass = field.getType();
                }
                //注入代理对象
                //因为这个方法调用时正在构建IOC容器，所以无法从我们封装的工具类中获取RpcConfig
                Object proxyObject = ServiceProxyFactory.getProxy(interfaceClass, applicationContext.getBean(RpcConfig.class));
                field.setAccessible(true);

                try {
                    field.set(bean, proxyObject);
                    field.setAccessible(false);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("为对象注册代理对象失败" + e);
                }

            }
        }


        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
