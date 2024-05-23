package com.zunf.rpc.server.impl;

import com.zunf.rpc.server.WebServer;
import com.zunf.rpc.server.handler.VertxHttpServerHandler;
import io.vertx.core.Vertx;

/**
 * VertxHttp服务器
 *
 * @author zunf
 * @date 2024/5/6 09:21
 */
public class VertxHttpServer implements WebServer {
    @Override
    public void doStart(int port) {

        //创建 Vert.x 实例
        Vertx vertx = Vertx.vertx();

        //创建 HTTP 服务器
        io.vertx.core.http.HttpServer server = vertx.createHttpServer();

        //设置请求的处理器
        server.requestHandler(new VertxHttpServerHandler());

        //启动 HTTP 服务器并监听指定端口
        server.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("Server is now listening on port " + port);
            } else {
                System.out.println("Failed to start server: " + result.cause());
            }
        });
    }
}
