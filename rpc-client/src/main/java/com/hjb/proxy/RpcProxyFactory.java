package com.hjb.proxy;

import com.alibaba.fastjson.JSON;
import com.hjb.client.MessageClientHandler;
import com.hjb.client.NettyClient;
import com.hjb.coder.MessageDecode;
import com.hjb.coder.MessageEncode;
import com.hjb.model.Message;
import com.hjb.model.RpcRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

public class RpcProxyFactory {

    public static <T> T create(Class<?> clazz){
        //clazz传进来本身就是interface
        MethodProxy proxy = new MethodProxy(clazz);
        Class<?> [] interfaces = clazz.isInterface() ?
                new Class[]{clazz} :
                clazz.getInterfaces();
        T result = (T) Proxy.newProxyInstance(clazz.getClassLoader(),interfaces,proxy);
        return result;
    }

    private static class MethodProxy implements InvocationHandler {
        private Class<?> clazz;

        public MethodProxy(Class<?> clazz) {
            this.clazz = clazz;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            //如果传进来是一个已实现的具体类（本次演示略过此逻辑)
            if (Object.class.equals(method.getDeclaringClass())) {
                try {
                    return method.invoke(this, args);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                //如果传进来的是一个接口（核心)
            } else {
                return rpcInvoke(proxy, method, args);
            }
            return null;
        }

        public Object rpcInvoke(Object proxy, Method method, Object[] args) {
            System.out.println("开始代理处理");

            RpcRequest request = new RpcRequest();
            request.setClassName(this.clazz.getName());
            request.setMethodName(method.getName());
            request.setParams(args);
            request.setParamsTypes(method.getParameterTypes());

            Message message = new Message();
            message.setVersionId(001);
            message.setExtField(002);
            message.setUuId(UUID.randomUUID().toString());
            message.setMessageType(1);
            message.setLength(JSON.toJSONString(request).length());
            message.setContent(JSON.toJSONString(request));

            NettyClient client = new NettyClient();
            Message result = client.doConnect(message);

            return result.getContent();
        }

    }
}
