package com.myauth.common.util;

import cn.hutool.json.JSONUtil;
import com.myauth.common.entity.License;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.time.LocalDate;

/**
 * License验证器
 */
public class LicenseValidator {
    
    private static final String PUBLIC_KEY_FILE = "license/public.key";
    private static final String LICENSE_FILE = "license/license.key";
    
    /**
     * 验证License Key
     * 
     * @param licenseKey License密钥
     * @param publicKeyBase64 公钥（Base64格式）
     * @return License对象
     * @throws Exception 验证失败抛出异常
     */
    public static License validate(String licenseKey, String publicKeyBase64) throws Exception {
        // 1. 解码
        String[] parts = LicenseCodec.decode(licenseKey);
        String jsonData = parts[0];
        String signature = parts[1];
        
        // 2. 加载公钥
        PublicKey publicKey = RSAUtil.loadPublicKeyFromBase64(publicKeyBase64);
        
        // 3. 验签
        if (!RSAUtil.verify(jsonData, signature, publicKey)) {
            throw new RuntimeException("授权验证失败：签名无效");
        }
        
        // 4. 解析JSON
        License license = JSONUtil.toBean(jsonData, License.class);
        
        // 5. 时间校验
        LocalDate expireDate = LocalDate.parse(license.getExpireTime());
        LocalDate now = LocalDate.now();
        if (now.isAfter(expireDate)) {
            throw new RuntimeException("授权已过期，过期时间：" + license.getExpireTime());
        }
        
        // 6. 机器码校验（如果设置了machineId）
        if (license.getMachineId() != null && !license.getMachineId().isEmpty()) {
            String currentMachineId = MachineUtil.getMachineId();
            if (!license.getMachineId().equals(currentMachineId)) {
                throw new RuntimeException("设备不匹配，当前机器码：" + currentMachineId);
            }
        }
        
        return license;
    }
    
    /**
     * 从文件验证License
     */
    public static License validateFromFile(String publicKeyBase64) throws Exception {
        // 读取License文件
        File licenseFile = new File(LICENSE_FILE);
        if (!licenseFile.exists()) {
            throw new RuntimeException("未找到授权文件：" + LICENSE_FILE);
        }
        
        String licenseKey = new String(Files.readAllBytes(Paths.get(LICENSE_FILE)));
        return validate(licenseKey.trim(), publicKeyBase64);
    }
    
    /**
     * 检查License是否即将过期（剩余天数）
     */
    public static int getRemainingDays(String expireTime) {
        LocalDate expireDate = LocalDate.parse(expireTime);
        LocalDate now = LocalDate.now();
        return (int) (expireDate.toEpochDay() - now.toEpochDay());
    }
}
