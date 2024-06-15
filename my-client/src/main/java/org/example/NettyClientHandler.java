package org.example;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Slf4j
public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    private MessageListener messageListener;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("接收到来自服务端消息：:"+msg);
        Response response= JSON.parseObject(msg+"", Response.class);
        if(messageListener!=null)
            messageListener.myInterface(response.getMessage());
    }
}
