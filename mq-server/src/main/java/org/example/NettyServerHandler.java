package org.example;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

@Slf4j
public class NettyServerHandler  extends ChannelInboundHandlerAdapter{


//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        System.out.println("hehe");
//        super.channelRead(ctx, msg);
//    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
       // ByteBuf buf = (ByteBuf) msg;
//        String string = buf.toString(Charset.forName("utf-8"));
        log.info("接收到消息:"+msg);
        Request request = JSON.parseObject(msg+"", Request.class);
        switch (request.getType()) {
            case SEND_MESSAGE:
                handleSendMessage(ctx, request.getMessage());
                break;
            case GET_MESSAGE:
                handleGetMessage(ctx);
                break;
        }
    }

    private void handleSendMessage(ChannelHandlerContext ctx, Msg message) {
        NettyServer.queue.add(message);
        ctx.writeAndFlush(JSON.toJSONString(new Response()));
    }

    private void handleGetMessage(ChannelHandlerContext ctx) {
        Msg message = NettyServer.queue.poll();
        log.info("弹出消息:{}",JSON.toJSONString(message));
        if (message == null) {
            ctx.writeAndFlush(JSON.toJSONString(new Response(new Msg())));
        } else {
            ctx.writeAndFlush(JSON.toJSONString(new Response(message)));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
