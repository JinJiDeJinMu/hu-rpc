package com.hjb.client;


import com.hjb.coder.MessageDecode;
import com.hjb.coder.MessageEncode;
import com.hjb.model.Message;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;

import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class NettyClient {

    private EventLoopGroup group = new NioEventLoopGroup(1);
    private Bootstrap bootstrap = new Bootstrap();

    MessageClientHandler clientHandler;
    Channel channel;
    List<Channel> channels = new ArrayList<>();
    private AtomicInteger roundRobin = new AtomicInteger(0);

    public NettyClient(){
        this.clientHandler = new MessageClientHandler();
    }

    public Object send(Message request, Channel channel) throws InterruptedException{

        if (channel!=null && clientHandler != null) {
            SynchronousQueue<Object> queue = clientHandler.sendRequest(request,channel);
            Object result = queue.take();
            return result;
        }else{
            return "连接错误";
        }
    }
    public Message doConnect(Message message){
        Message object = null;
        clientHandler = new MessageClientHandler();
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup work = new NioEventLoopGroup();
        bootstrap.group(work)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline cp = ch.pipeline();
                        cp.addLast(new MessageEncode());
                        cp.addLast(new MessageDecode());
                        cp.addLast(clientHandler);
                    }
                });
        try {
            ChannelFuture future = bootstrap.connect("localhost", 8888).sync();
            SynchronousQueue<Object> objects = clientHandler.sendRequest(message, future.channel());
            object = (Message) objects.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            work.shutdownGracefully();
        }
        return object;
    }

    public  Channel chooseChannel() {
        if (channels.size()>0) {
            int size = channels.size();
            int index = (roundRobin.getAndAdd(1) + size) % size;
            return channels.get(index);
        }else{
            return null;
        }
    }
}
