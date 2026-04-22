package com.myauth.common.util;

import java.util.Base64;

/**
 * License编码/解码工具类
 */
public class LicenseCodec {
    
    /**
     * 编码：将授权数据和签名组合成License Key
     * 格式: Base64(data.signature)
     */
    public static String encode(String data, String sign) {
        String combined = data + "." + sign;
        return Base64.getEncoder().encodeToString(combined.getBytes());
    }
    
    /**
     * 解码：从License Key中分离出授权数据和签名
     * 返回数组: [data, sign]
     */
    public static String[] decode(String licenseKey) {
        String decoded = new String(Base64.getDecoder().decode(licenseKey));
        int dotIndex = decoded.lastIndexOf('.');
        if (dotIndex == -1) {
            throw new IllegalArgumentException("无效的License Key格式");
        }
        String data = decoded.substring(0, dotIndex);
        String sign = decoded.substring(dotIndex + 1);
        return new String[]{data, sign};
    }
}
