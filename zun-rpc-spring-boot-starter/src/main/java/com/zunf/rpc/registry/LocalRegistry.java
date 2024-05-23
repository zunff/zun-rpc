package com.zunf.rpc.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地服务注册器
 */
public class LocalRegistry {

    /**
     * 注册信息存储, Key为ServiceName, Value为 Class 对象
     */
    private static Map<String, Class<?>> map = new ConcurrentHashMap<>();

    /**
     * 注册服务
     * @param serviceName
     * @param clazz
     */
    public static void register(String serviceName, Class<?> clazz) {
        map.put(serviceName, clazz);
    }

    /**
     * 获取服务类信息
     * @param serviceName
     */
    public static Class<?> get(String serviceName) {
        return map.get(serviceName);
    }

    /**
     * 删除服务
     * @param serviceName
     */
    public static void remove(String serviceName) {
        map.remove(serviceName);
    }

}
