package com.hjb;

import com.hjb.server.NettyServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RpcServerApplication {

    @Autowired
    private NettyServer nettyServer;
    
    public static void main(String[] args) {
        SpringApplication.run(RpcServerApplication.class, args);
    }

}
