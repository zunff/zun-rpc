package com.zunf.rpc.client;

import com.zunf.rpc.protocol.NettyProtocolDecoder;
import com.zunf.rpc.protocol.NettyProtocolEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ChannelPoolManager {

    private static final int MAX_CONNECTIONS = 10;

    private static final EventLoopGroup EVENT_LOOP_GROUP = new NioEventLoopGroup();

    private static final ConcurrentHashMap<String, FixedChannelPool> POOL_MAP = new ConcurrentHashMap<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(ChannelPoolManager::shutdown));
    }

    public static Channel acquire(String host, int port) throws Exception {
        String key = host + ":" + port;
        FixedChannelPool pool = POOL_MAP.computeIfAbsent(key, k -> createPool(host, port));
        return pool.acquire().sync().getNow();
    }

    public static void release(String host, int port, Channel channel) {
        String key = host + ":" + port;
        FixedChannelPool pool = POOL_MAP.get(key);
        if (pool != null && channel.isActive()) {
            pool.release(channel);
        } else {
            channel.close();
        }
    }

    public static void remove(String host, int port) {
        String key = host + ":" + port;
        FixedChannelPool pool = POOL_MAP.remove(key);
        if (pool != null) {
            pool.close();
        }
    }

    private static FixedChannelPool createPool(String host, int port) {
        Bootstrap bootstrap = new Bootstrap()
                .group(EVENT_LOOP_GROUP)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .remoteAddress(new InetSocketAddress(host, port));

        return new FixedChannelPool(bootstrap, new AbstractChannelPoolHandler() {
            @Override
            public void channelCreated(Channel ch) {
                ch.pipeline()
                        .addLast(new IdleStateHandler(0, 30, 0, TimeUnit.SECONDS))
                        .addLast(new NettyProtocolDecoder())
                        .addLast(NettyProtocolEncoder.INSTANCE)
                        .addLast(NettyTcpClientHandler.INSTANCE)
                        .addLast(ClientHeartbeatHandler.INSTANCE);
                log.info("Channel created for {}:{}", host, port);
            }
        }, MAX_CONNECTIONS);
    }

    public static void shutdown() {
        POOL_MAP.values().forEach(FixedChannelPool::close);
        EVENT_LOOP_GROUP.shutdownGracefully();
    }
}
