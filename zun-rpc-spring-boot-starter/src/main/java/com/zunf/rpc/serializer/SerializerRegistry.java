package com.zunf.rpc.serializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SerializerRegistry {

    private static final Map<String, Serializer> SERIALIZERS = new ConcurrentHashMap<>();

    public static void register(String key, Serializer serializer) {
        SERIALIZERS.put(key, serializer);
    }

    public static Serializer get(String key) {
        return SERIALIZERS.get(key);
    }
}
