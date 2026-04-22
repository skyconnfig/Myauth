package com.myauth.generator;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 启动时创建默认管理员账号
 */
@Component
public class DefaultUserInitializer implements CommandLineRunner {
    
    private final com.myauth.generator.mapper.SysUserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    public DefaultUserInitializer(com.myauth.generator.mapper.SysUserMapper userMapper) {
        this.userMapper = userMapper;
    }
    
    @Override
    public void run(String... args) throws Exception {
        try {
            // 检查是否已有admin用户
            var existingUser = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.myauth.generator.entity.SysUser>()
                    .eq(com.myauth.generator.entity.SysUser::getUsername, "admin")
            );
            
            if (existingUser == null) {
                // 创建默认管理员
                com.myauth.generator.entity.SysUser admin = new com.myauth.generator.entity.SysUser();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRealName("系统管理员");
                admin.setEmail("admin@myauth.com");
                admin.setStatus(1);
                admin.setCreateTime(LocalDateTime.now());
                admin.setUpdateTime(LocalDateTime.now());
                
                userMapper.insert(admin);
                System.out.println("✅ 默认管理员账号创建成功: admin / admin123");
            } else {
                System.out.println("ℹ️  管理员账号已存在");
            }
        } catch (Exception e) {
            System.err.println("⚠️  创建默认管理员账号失败: " + e.getMessage());
            System.err.println("请通过 API 注册新用户: POST /api/auth/register");
        }
    }
}
