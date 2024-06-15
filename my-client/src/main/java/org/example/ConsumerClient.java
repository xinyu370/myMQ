package org.example;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Data
@Slf4j
public class ConsumerClient {
    private EventLoopGroup nioEventLoopGroup = new NioEventLoopGroup();
    private Bootstrap bootstrap = null;

    private Channel channel = null;
    private MessageListener messageListener;

    private String host;

    private Integer port;

    private NettyClientHandler handler = null;
    public void start() throws InterruptedException {
        handler = new NettyClientHandler(messageListener);
        ChannelFuture channelFuture = new Bootstrap()
                .group(nioEventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast(new ObjectEncoder())
                                .addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)))
                                .addLast(handler)
                        ;
                    }
                })
                //this method was sync
                .connect(new InetSocketAddress(CheckUtil.checkAdd(host), port));
        channelFuture.sync();
        channel = channelFuture.channel();


        ChannelFuture closeFuture = channel.closeFuture();
        closeFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                System.out.println("waiting quite");
                nioEventLoopGroup.shutdownGracefully();
            }
        });
    }

    public void getMessage() throws Exception {
        log.info("获取消息");
        Request request = new Request(RequestType.GET_MESSAGE,null);
        if(channel==null){
            throw new Exception("未完成初始化");
        }
        channel.writeAndFlush(JSON.toJSONString(request));
    }


    public void shutDown(){
        if(channel==null){
            return;
        }
        channel.close();
    }
}
