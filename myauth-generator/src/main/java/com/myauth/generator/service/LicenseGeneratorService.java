package com.myauth.generator.service;

import cn.hutool.json.JSONUtil;
import com.myauth.common.entity.License;
import com.myauth.common.util.LicenseCodec;
import com.myauth.common.util.RSAUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * License生成服务
 */
@Slf4j
@Service
public class LicenseGeneratorService {
    
    @Value("${license.private-key-path:license/private.key}")
    private String privateKeyPath;
    
    @Value("${license.public-key-path:license/public.key}")
    private String publicKeyPath;
    
    /**
     * 生成密钥对并保存到文件
     */
    public void generateKeyPair() throws Exception {
        KeyPair keyPair = RSAUtil.generateKeyPair();
        
        // 获取工作目录，确保路径正确
        String workDir = System.getProperty("user.dir");
        log.info("当前工作目录: {}", workDir);
        
        // 保存私钥
        String privateKeyBase64 = RSAUtil.privateKeyToBase64(keyPair.getPrivate());
        File privateKeyFile = new File(workDir, privateKeyPath);
        
        // 确保父目录存在
        File privateParentDir = privateKeyFile.getParentFile();
        if (privateParentDir != null && !privateParentDir.exists()) {
            boolean created = privateParentDir.mkdirs();
            log.info("创建私钥目录: {}, 结果: {}", privateParentDir.getAbsolutePath(), created);
        }
        
        Files.writeString(privateKeyFile.toPath(), privateKeyBase64);
        log.info("私钥已保存到: {}", privateKeyFile.getAbsolutePath());
        
        // 保存公钥
        String publicKeyBase64 = RSAUtil.publicKeyToBase64(keyPair.getPublic());
        File publicKeyFile = new File(workDir, publicKeyPath);
        
        // 确保父目录存在
        File publicParentDir = publicKeyFile.getParentFile();
        if (publicParentDir != null && !publicParentDir.exists()) {
            boolean created = publicParentDir.mkdirs();
            log.info("创建公钥目录: {}, 结果: {}", publicParentDir.getAbsolutePath(), created);
        }
        
        Files.writeString(publicKeyFile.toPath(), publicKeyBase64);
        log.info("公钥已保存到: {}", publicKeyFile.getAbsolutePath());
        
        log.info("密钥对生成成功");
    }
    
    /**
     * 加载私钥
     */
    public PrivateKey loadPrivateKey() throws Exception {
        File file = new File(privateKeyPath);
        if (!file.exists()) {
            throw new RuntimeException("私钥文件不存在，请先生成密钥对");
        }
        String privateKeyBase64 = Files.readString(file.toPath());
        return RSAUtil.loadPrivateKeyFromBase64(privateKeyBase64);
    }
    
    /**
     * 加载公钥（Base64字符串）
     */
    public String loadPublicKeyBase64() throws Exception {
        File file = new File(publicKeyPath);
        if (!file.exists()) {
            throw new RuntimeException("公钥文件不存在，请先生成密钥对");
        }
        return Files.readString(file.toPath());
    }
    
    /**
     * 生成License Key
     */
    public String generateLicenseKey(License license) throws Exception {
        // 1. 将License对象转为JSON
        String jsonData = JSONUtil.toJsonStr(license);
        
        // 2. 使用私钥签名
        PrivateKey privateKey = loadPrivateKey();
        String signature = RSAUtil.sign(jsonData, privateKey);
        
        // 3. 编码生成License Key
        String licenseKey = LicenseCodec.encode(jsonData, signature);
        
        log.info("License生成成功 - 客户: {}, 类型: {}, 过期时间: {}", 
                license.getCustomerName(), license.getType(), license.getExpireTime());
        
        return licenseKey;
    }
    
    /**
     * 获取公钥Base64字符串（用于客户端验证）
     */
    public String getPublicKeyForClient() throws Exception {
        return loadPublicKeyBase64();
    }
}
