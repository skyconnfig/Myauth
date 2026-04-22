package com.myauth.generator;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密工具 - 用于生成测试密码
 */
public class PasswordEncoderTest {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成 admin123 的加密密码
        String password = "admin123";
        String encoded = encoder.encode(password);
        
        System.out.println("原始密码: " + password);
        System.out.println("加密后: " + encoded);
        System.out.println();
        System.out.println("请在 init.sql 中使用以下SQL:");
        System.out.println("INSERT INTO `sys_user` (`username`, `password`, `real_name`, `email`, `status`) VALUES");
        System.out.println("('admin', '" + encoded + "', '系统管理员', 'admin@myauth.com', 1);");
    }
}
