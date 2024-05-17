package com.zjh.rpc.constants;

/**
 * 自定义协议常量
 *
 * @author zunf
 * @date 2024/5/15 10:18
 */
public interface ProtocolConstants {

    /**
     * 消息头长度
     */
    int MESSAGE_HEAD_LENGTH = 17;

    /**
     * 协议魔数
     */
    byte MAGIC = 0x00000011;

    /**
     * 协议版本号
     */
    byte VERSION = 1;

}
