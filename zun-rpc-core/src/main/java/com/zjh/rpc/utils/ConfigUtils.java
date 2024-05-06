package com.zjh.rpc.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;

/**
 * RPC配置工具类
 *
 * @author zunf
 * @date 2024/5/6 09:58
 */
public class ConfigUtils {

    /**
     * 读取resource下的application.properties文件，并返回一个对象
     * @param clazz 存储配置信息的类Class
     * @param prefix 配置文件加载前缀，例：rpc.name = "zun-rpc"的rpc前缀
     * @return 存储配置信息的对象
     * @param <T> 存储配置信息的类的类型
     */
    public static <T> T loadConfig(Class<T> clazz, String prefix) {
        return loadConfig(clazz, prefix, "");
    }

    /**
     * 读取resource下的application-${environment}.properties文件，并返回一个对象
     * @param clazz 存储配置信息的类Class
     * @param prefix 配置文件加载前缀，例：rpc.name = "zun-rpc"的rpc前缀
     * @param environment application配置文件的环境
     * @return 存储配置信息的对象
     * @param <T> 存储配置信息的类的类型
     */
    public static <T> T loadConfig(Class<T> clazz, String prefix, String environment) {

        StringBuilder sb = new StringBuilder();
        sb.append("application");
        if (StrUtil.isNotBlank(environment))  {
            sb.append("-").append(environment);
        }
        sb.append(".properties");

        Props props = new Props(sb.toString());
        return props.toBean(clazz, prefix);
    }
}
