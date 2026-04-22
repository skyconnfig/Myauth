package com.myauth.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * License配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "license")
public class LicenseProperties {
    
    /**
     * 公钥Base64字符串
     */
    private String publicKey;
    
    /**
     * License文件路径
     */
    private String licenseFile;
}
