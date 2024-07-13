package org.example;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;
import org.example.store.ConsumeIndexStore;

import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class NettyServer {
    public static ArrayBlockingQueue<Msg> queue = new ArrayBlockingQueue<>(1024);
    public static Map<String/**topic*/, ArrayBlockingQueue> topicQueue = new ConcurrentHashMap<>();
    public static void main(String[] args) throws InterruptedException {
        init();
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)//选择服务器的 ServerSocketChannel 实现
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                            nioSocketChannel.pipeline()
                                    .addLast(new ObjectEncoder()).addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)))
                                    .addLast(new NettyServerHandler()).addLast();
                        }
                    });
            ChannelFuture future = serverBootstrap.bind(8080).sync();
            System.out.println("Server started and listening on port " + 8080);
            future.channel().closeFuture().sync();
        }finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }

    }

    public static void init(){
//        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
//        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
//            @Override
//            public void run() {
//                System.out.println("当前队列size:"+topicQueue.size());
//            }
//        },0,10, TimeUnit.SECONDS);
        ConsumeIndexStore.getInstance().resumeMsg();
        //钩子函数->用于释放资源
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("资源释放...");
            ConsumeIndexStore.getInstance().shutDown();
        }));
    }

}
