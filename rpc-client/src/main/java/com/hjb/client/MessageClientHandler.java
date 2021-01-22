package com.hjb.client;

import com.hjb.model.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;


@Component
@ChannelHandler.Sharable
public class MessageClientHandler extends ChannelInboundHandlerAdapter {

    private ConcurrentHashMap<String, SynchronousQueue<Object>> queueMap = new ConcurrentHashMap<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Message message = (Message) msg;
        SynchronousQueue<Object> queue = queueMap.get(message.getUuId());
        queue.put(message);
        queueMap.remove(message.getUuId());
        System.out.println("客户端业务收到服务端消息，处理逻辑" + msg);
    }

    public SynchronousQueue<Object> sendRequest(Message request, Channel channel) {
        SynchronousQueue<Object> queue = new SynchronousQueue<>();
        queueMap.put(request.getUuId(), queue);
        channel.writeAndFlush(request);
        return queue;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("发生异常");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

}
