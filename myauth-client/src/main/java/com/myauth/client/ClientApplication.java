package com.myauth.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 授权验证客户端启动类
 */
@SpringBootApplication
public class ClientApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
        System.out.println("===========================================");
        System.out.println("   MyAuth 客户端启动成功！");
        System.out.println("   服务端口: http://localhost:8081");
        System.out.println("===========================================");
    }
}
