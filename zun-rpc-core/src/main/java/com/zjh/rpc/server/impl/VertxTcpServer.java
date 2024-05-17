package com.zjh.rpc.server.impl;

import com.zjh.rpc.server.WebServer;
import com.zjh.rpc.server.handler.VertxTcpServerHandler;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;

/**
 * Vertx实现的Tcp协议服务器
 *
 * @author zunf
 * @date 2024/5/14 18:09
 */
public class VertxTcpServer implements WebServer {

    @Override
    public void doStart(int port) {
        //创建Vertx实例
        Vertx vertx = Vertx.vertx();

        //创建 Tcp 服务器
        NetServer server = vertx.createNetServer();

        //设置 Tcp 处理器
        server.connectHandler(new VertxTcpServerHandler());

        //启动 Tcp 服务器并监听指定端口
        server.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("Server is now listening on port " + port);
            } else {
                System.out.println("Failed to start server: " + result.cause());
            }
        });
    }
}
