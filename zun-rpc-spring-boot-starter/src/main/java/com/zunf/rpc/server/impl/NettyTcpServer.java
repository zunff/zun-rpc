package com.zunf.rpc.server.impl;

import com.zunf.rpc.protocol.NettyProtocolDecoder;
import com.zunf.rpc.protocol.NettyProtocolEncoder;
import com.zunf.rpc.server.WebServer;
import com.zunf.rpc.server.handler.NettyTcpServerHandler;
import com.zunf.rpc.server.handler.ServerHeartbeatHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyTcpServer implements WebServer {

    @Override
    public void doStart(int port) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new IdleStateHandler(60, 0, 0, java.util.concurrent.TimeUnit.SECONDS))
                                .addLast(new NettyProtocolDecoder())
                                .addLast(NettyProtocolEncoder.INSTANCE)
                                .addLast(ServerHeartbeatHandler.INSTANCE)
                                .addLast(NettyTcpServerHandler.INSTANCE);
                    }
                });

        try {
            bootstrap.bind(port).sync();
            log.info("Netty TCP Server started on port {}", port);
        } catch (InterruptedException e) {
            log.error("Failed to start Netty TCP Server", e);
            Thread.currentThread().interrupt();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }));
    }
}
