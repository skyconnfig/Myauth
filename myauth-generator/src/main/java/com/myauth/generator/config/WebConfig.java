package com.myauth.generator.config;

import com.myauth.generator.interceptor.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置 - 注册拦截器和CORS
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    
    private final JwtInterceptor jwtInterceptor;
    
    /**
     * 配置CORS跨域
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")  // 允许所有来源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
    
    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加JWT拦截器
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")  // 拦截所有/api开头的请求
                .excludePathPatterns(
                        "/api/auth/login",   // 排除登录接口
                        "/api/auth/register", // 排除注册接口
                        "/doc.html",          // 排除API文档
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**"
                );
    }
}
