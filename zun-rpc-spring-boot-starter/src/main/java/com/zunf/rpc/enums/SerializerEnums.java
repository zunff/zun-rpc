package com.zunf.rpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 序列化器枚举
 *
 * @author zunf
 * @date 2024/5/15 09:51
 */
@Getter
@AllArgsConstructor
public enum SerializerEnums {

    /**
     * 序列化器：1-jdk、2-kryo、3-json、4-hessian
     */
    JDK(1, "jdk"),
    KRYO(2, "kryo"),
    JSON(3, "json"),
    HESSIAN(4, "hessian");


    private final int type;


    private final String value;

    public static SerializerEnums of(int type) {
        return Arrays.stream(values()).filter(e -> e.type == type).findFirst().orElse(null);
    }

    public static SerializerEnums of(String value) {
        return Arrays.stream(values()).filter(e -> e.value.equals(value)).findFirst().orElse(null);
    }

}
