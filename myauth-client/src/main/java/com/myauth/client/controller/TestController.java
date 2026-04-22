package com.myauth.client.controller;

import com.myauth.client.interceptor.LicenseManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 客户端测试控制器
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TestController {
    
    private final LicenseManager licenseManager;
    
    @GetMapping("/license-info")
    public Map<String, Object> getLicenseInfo() {
        return licenseManager.getLicenseInfo();
    }
    
    @GetMapping("/test")
    public String test() {
        if (!licenseManager.isValidated()) {
            return "系统未授权，无法访问";
        }
        return "系统运行正常，已授权！";
    }
}
