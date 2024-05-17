package com.zjh.rpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 自定义协议-状态枚举
 *
 * @author zunf
 * @date 2024/5/15 09:58
 */
@Getter
@AllArgsConstructor
public enum MessageStatusEnums {

    /**
     * 状态枚举：1-成功、2-请求失败、3-响应失败
     */
    OK(1, "ok"),
    REQUEST_FAILED(2, "bad request"),
    RESPONSE_FAILED(3, "response failed");

    private final int type;

    private final String value;

    public static MessageStatusEnums of(int type) {
        return Arrays.stream(values()).filter(e -> e.type == type).findFirst().orElse(null);
    }
}
