package com.zjh.rpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 自定义协议，消息类型枚举
 *
 * @author zunf
 * @date 2024/5/15 09:51
 */
@Getter
@AllArgsConstructor
public enum MessageTypeEnums {

    /**
     * 消息类型：1-请求、2-响应、3-心跳
     */
    REQUEST(1, "request"),
    RESPONSE(2, "response"),
    HEARTBEAT(3, "heartbeat");

    private final int type;

    private final String value;

    public static MessageTypeEnums of(int type) {
        return Arrays.stream(values()).filter(e -> e.type == type).findFirst().orElse(null);
    }
}
