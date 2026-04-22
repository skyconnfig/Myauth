package com.myauth.generator.controller;

import com.myauth.generator.common.Result;
import com.myauth.generator.entity.SysUser;
import com.myauth.generator.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 */
@Tag(name = "用户认证", description = "登录、注册、用户信息接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        try {
            Map<String, Object> result = authService.login(request.getUsername(), request.getPassword());
            return Result.success("登录成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterRequest request) {
        try {
            authService.register(
                request.getUsername(),
                request.getPassword(),
                request.getRealName(),
                request.getEmail(),
                request.getPhone()
            );
            return Result.success("注册成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/userinfo")
    public Result<SysUser> getUserInfo(@RequestAttribute Long userId) {
        try {
            SysUser user = authService.getUserInfo(userId);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @Operation(summary = "修改密码")
    @PostMapping("/change-password")
    public Result<Void> changePassword(
            @RequestAttribute Long userId,
            @RequestBody ChangePasswordRequest request) {
        try {
            authService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
            return Result.success("密码修改成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    // 请求DTO类
    public static class LoginRequest {
        private String username;
        private String password;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class RegisterRequest {
        private String username;
        private String password;
        private String realName;
        private String email;
        private String phone;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }
    
    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;
        
        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}
