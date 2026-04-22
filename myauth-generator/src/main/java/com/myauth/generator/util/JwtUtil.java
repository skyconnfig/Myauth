package com.myauth.generator.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT工具类
 */
public class JwtUtil {
    
    private static final long EXPIRATION_TIME = 86400000; // 24小时
    
    /**
     * 生成密钥
     */
    private static SecretKey getSecretKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 生成Token
     */
    public static String generateToken(String username, Long userId, String secret) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);
        
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSecretKey(secret))
                .compact();
    }
    
    /**
     * 解析Token
     */
    public static Claims parseToken(String token, String secret) {
        return Jwts.parser()
                .verifyWith(getSecretKey(secret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * 从Token中获取用户名
     */
    public static String getUsernameFromToken(String token, String secret) {
        Claims claims = parseToken(token, secret);
        return claims.getSubject();
    }
    
    /**
     * 从Token中获取用户ID
     */
    public static Long getUserIdFromToken(String token, String secret) {
        Claims claims = parseToken(token, secret);
        return claims.get("userId", Long.class);
    }
    
    /**
     * 验证Token是否有效
     */
    public static boolean validateToken(String token, String secret) {
        try {
            parseToken(token, secret);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
