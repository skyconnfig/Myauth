package com.myauth.generator;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 授权生成器启动类
 */
@SpringBootApplication
@MapperScan("com.myauth.generator.mapper")
public class GeneratorApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(GeneratorApplication.class, args);
        System.out.println("===========================================");
        System.out.println("   MyAuth 授权管理系统启动成功！");
        System.out.println("   API文档: http://localhost:8080/doc.html");
        System.out.println("===========================================");
    }
}
