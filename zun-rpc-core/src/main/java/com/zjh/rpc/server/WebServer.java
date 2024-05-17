package com.zjh.rpc.server;

/**
 *  Web 服务器接口
 *
 * @author zunf
 * @date 2024/5/14 18:06
 */
public interface WebServer {

    /**
     * 启动服务器
     * @param port 端口
     */
    void doStart(int port);

}
