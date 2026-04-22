package com.myauth.generator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j/Swagger配置
 */
@Configuration
public class Knife4jConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MyAuth 授权管理系统 API")
                        .version("1.0.0")
                        .description("Java离线授权管理系统 - 授权码生成和管理接口")
                        .contact(new Contact()
                                .name("MyAuth Team")
                                .email("support@myauth.com")));
    }
}
