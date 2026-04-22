package com.myauth.generator.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myauth.generator.entity.SysUser;
import com.myauth.generator.mapper.SysUserMapper;
import com.myauth.generator.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final SysUserMapper userMapper;
    
    @Value("${jwt.secret:myauth-secret-key-for-jwt-token-generation-2024}")
    private String jwtSecret;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 用户登录
     */
    public Map<String, Object> login(String username, String password) {
        // 1. 查询用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        SysUser user = userMapper.selectOne(wrapper);
        
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 2. 检查用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }
        
        // 3. 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 4. 生成Token
        String token = JwtUtil.generateToken(user.getUsername(), user.getId(), jwtSecret);
        
        // 5. 返回用户信息和Token
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("userId", user.getId());
        
        log.info("用户登录成功: {}", username);
        
        return result;
    }
    
    /**
     * 注册新用户
     */
    public void register(String username, String password, String realName, String email, String phone) {
        // 1. 检查用户名是否已存在
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 2. 创建新用户
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRealName(realName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setStatus(1);
        
        userMapper.insert(user);
        
        log.info("用户注册成功: {}", username);
    }
    
    /**
     * 修改密码
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        // 1. 查询用户
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 2. 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        
        // 3. 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        
        log.info("用户修改密码成功: {}", user.getUsername());
    }
    
    /**
     * 获取用户信息
     */
    public SysUser getUserInfo(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null); // 不返回密码
        }
        return user;
    }
}
