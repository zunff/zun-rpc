package com.zunf.rpc.utils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Spring上下文工具类
 *
 * @author zunf
 * @date 2024/5/23 17:49
 */
@NoArgsConstructor
public class SpringContextUtil implements ApplicationContextAware {

    /**
     *  获取上下文
     */
    @Getter
    private static ApplicationContext applicationContext;

    /**
     * 设置上下文
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (SpringContextUtil.applicationContext == null) {
            SpringContextUtil.applicationContext = applicationContext;
        }

    }

    /**
     * 通过名字获取上下文中的bean
     * @param name
     * @return
     */
    public static Object getBean(String name){
        return applicationContext.getBean(name);
    }

    /**
     * 通过类型获取上下文中的bean
     * @param requiredType
     * @return
     */
    public static<T> T getBean(Class<T> requiredType){
        return applicationContext.getBean(requiredType);
    }
}
