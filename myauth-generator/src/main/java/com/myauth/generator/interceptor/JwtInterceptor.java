package com.myauth.generator.interceptor;

import com.myauth.generator.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT认证拦截器
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {
    
    @Value("${jwt.secret:myauth-secret-key-for-jwt-token-generation-2024}")
    private String jwtSecret;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        
        // 获取Token
        String token = request.getHeader("Authorization");
        
        if (token == null || token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或Token已过期\"}");
            return false;
        }
        
        // 去除Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        // 验证Token
        try {
            if (!JwtUtil.validateToken(token, jwtSecret)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期\"}");
                return false;
            }
            
            // 将用户信息存入request
            Long userId = JwtUtil.getUserIdFromToken(token, jwtSecret);
            String username = JwtUtil.getUsernameFromToken(token, jwtSecret);
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            
            return true;
        } catch (Exception e) {
            log.error("JWT验证失败: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token验证失败\"}");
            return false;
        }
    }
}
