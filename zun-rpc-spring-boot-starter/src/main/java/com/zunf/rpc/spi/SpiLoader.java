package com.zunf.rpc.spi;

import cn.hutool.core.io.resource.ResourceUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SPI 服务加载器
 *
 * @author zunf
 * @date 2024/5/7 09:41
 */
public class SpiLoader {

    /**
     * 扫描路径
     */
    private static final String[] SCAN_DIRS = new String[]{"META-INF/rpc/system/", "META-INF/rpc/custom/"};

    /**
     * 已加载的全类名。接口全类名 => (key => 接口实现类全类名)
     */
    private static final Map<String, Map<String, Class<?>>> LOAD_CLASS_MAP = new ConcurrentHashMap<>();

    /**
     * 服务对象缓存。全类名 => 类对象实例
     */
    private static final Map<String, Object> SPI_OBJECT_MAP = new ConcurrentHashMap<>();


    /**
     * 加载 META-INF/rpc/system、custom 下的类路径，custom定义的类优先级高，可覆盖system中的设置
     *
     * @param loadClass 需要加载的接口
     */
    public static void load(Class<?> loadClass) {
        //全类名所对应注册的实现类map
        Map<String, Class<?>> keyClassMap = new HashMap<>();

        for (String scanDir : SCAN_DIRS) {
            //扫描 system、custom 下的接口全类名文件
            List<URL> resources = ResourceUtil.getResources(scanDir + loadClass.getName());
            for (URL resource : resources) {
                try {
                    InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] split = line.split("=");
                        if (split.length == 2) {
                            String key = split[0];
                            String className = split[1];
                            keyClassMap.put(key, Class.forName(className));
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        //缓存到全局对象中
        LOAD_CLASS_MAP.put(loadClass.getName(), keyClassMap);
    }


    public static <T> T getSpiObject(Class<T> clazz, String key) {
        Map<String, Class<?>> keyClassMap = LOAD_CLASS_MAP.get(clazz.getName());
        if (keyClassMap == null || !keyClassMap.containsKey(key)) {
            throw new RuntimeException(String.format("SpiLoad 不存在 %s => %s", clazz.getName(), key));
        }
        //获取要加载的类型
        Class<?> aClass = keyClassMap.get(key);
        //从对象缓存中获取对象
        if (!SPI_OBJECT_MAP.containsKey(aClass.getName())) {
            synchronized (SPI_OBJECT_MAP) {
                if (!SPI_OBJECT_MAP.containsKey(aClass.getName())) {
                    try {
                        SPI_OBJECT_MAP.put(aClass.getName(), aClass.newInstance());
                    } catch (Exception e) {
                        throw new RuntimeException(String.format("%s 类实例化失败", aClass.getName()), e);
                    }
                }
            }
        }
        return (T) SPI_OBJECT_MAP.get(aClass.getName());
    }
}
