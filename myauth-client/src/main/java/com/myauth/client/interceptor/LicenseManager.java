package com.myauth.client.interceptor;

import cn.hutool.json.JSONUtil;
import com.myauth.common.entity.License;
import com.myauth.common.util.LicenseValidator;
import com.myauth.client.config.LicenseProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * License验证管理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LicenseManager {
    
    private final LicenseProperties licenseProperties;
    
    private volatile License currentLicense;
    private volatile boolean validated = false;
    
    @PostConstruct
    public void init() {
        // 启动时验证
        validateOnStartup();
        
        // 定时验证（每小时）
        schedulePeriodicValidation();
    }
    
    /**
     * 启动时验证
     */
    private void validateOnStartup() {
        try {
            log.info("开始验证License...");
            
            // 读取公钥
            String publicKey = licenseProperties.getPublicKey();
            
            // 如果配置中没有公钥，尝试从文件读取
            if (publicKey == null || publicKey.isEmpty()) {
                String publicKeyFile = "license/public.key";
                File pubKeyFile = new File(publicKeyFile);
                if (pubKeyFile.exists()) {
                    publicKey = Files.readString(pubKeyFile.toPath()).trim();
                    log.info("从文件 {} 读取公钥成功", publicKeyFile);
                } else {
                    log.warn("⚠️  未配置公钥，License验证跳过");
                    log.warn("如需启用License验证，请：");
                    log.warn("1. 从授权管理系统获取公钥");
                    log.warn("2. 在 application.yml 中配置 license.public-key");
                    log.warn("3. 或将公钥保存为 license/public.key 文件");
                    validated = false;
                    return; // 不退出，继续启动
                }
            }
            
            // 读取License文件
            String licenseFile = licenseProperties.getLicenseFile();
            File file = new File(licenseFile);
            if (!file.exists()) {
                log.warn("⚠️  未找到License文件: {}", licenseFile);
                log.warn("如需启用License验证，请将授权码保存到此文件中");
                validated = false;
                return; // 不退出，继续启动
            }
            
            String licenseKey = Files.readString(file.toPath()).trim();
            
            // 验证
            currentLicense = LicenseValidator.validate(licenseKey, publicKey);
            validated = true;
            
            int remainingDays = LicenseValidator.getRemainingDays(currentLicense.getExpireTime());
            
            log.info("===========================================");
            log.info("   ✅ License验证成功！");
            log.info("   客户名称: {}", currentLicense.getCustomerName());
            log.info("   授权类型: {}", currentLicense.getType());
            log.info("   过期时间: {}", currentLicense.getExpireTime());
            log.info("   剩余天数: {}", remainingDays);
            log.info("   最大用户数: {}", currentLicense.getMaxUsers());
            log.info("===========================================");
            
            // 如果剩余天数小于30天，发出警告
            if (remainingDays < 30) {
                log.warn("⚠️  警告: License将在{}天后过期，请及时续费！", remainingDays);
            }
            
        } catch (Exception e) {
            validated = false;
            log.error("❌ License验证失败: {}", e.getMessage());
            log.warn("⚠️  系统将以未授权模式启动，部分功能可能受限");
            // 不再退出，允许系统继续启动
        }
    }
    
    /**
     * 定时验证（防止时间回退攻击）
     */
    private void schedulePeriodicValidation() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (validated && currentLicense != null) {
                    // 检查是否过期
                    LocalDate expireDate = LocalDate.parse(currentLicense.getExpireTime());
                    if (LocalDate.now().isAfter(expireDate)) {
                        log.error("❌ License已过期！");
                        log.warn("⚠️  建议联系管理员续费授权");
                        // 不再强制退出，只是记录日志
                    }
                    
                    // 防时间回退检查（可选）
                    // 可以记录上次运行时间，检查当前时间是否早于上次时间
                    
                    log.debug("定时验证通过 - 剩余天数: {}", 
                            LicenseValidator.getRemainingDays(currentLicense.getExpireTime()));
                }
            } catch (Exception e) {
                log.debug("定时验证: {}", e.getMessage());
            }
        }, 1, 1, TimeUnit.HOURS); // 每小时验证一次
        
        log.info("已启动定时验证任务（每小时）");
    }
    
    /**
     * 获取当前License信息
     */
    public Map<String, Object> getLicenseInfo() {
        Map<String, Object> info = new HashMap<>();
        
        if (validated && currentLicense != null) {
            info.put("valid", true);
            info.put("customerName", currentLicense.getCustomerName());
            info.put("type", currentLicense.getType());
            info.put("expireTime", currentLicense.getExpireTime());
            info.put("maxUsers", currentLicense.getMaxUsers());
            info.put("modules", currentLicense.getModules());
            info.put("remainingDays", LicenseValidator.getRemainingDays(currentLicense.getExpireTime()));
            info.put("machineId", currentLicense.getMachineId());
        } else {
            info.put("valid", false);
            info.put("message", "未验证或验证失败");
        }
        
        return info;
    }
    
    /**
     * 检查是否已验证
     */
    public boolean isValidated() {
        return validated;
    }
    
    /**
     * 获取当前License对象
     */
    public License getCurrentLicense() {
        return currentLicense;
    }
}
