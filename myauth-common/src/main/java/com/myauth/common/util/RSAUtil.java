package com.myauth.common.util;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA加密工具类
 */
public class RSAUtil {
    
    private static final String ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    
    /**
     * 生成密钥对
     */
    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }
    
    /**
     * 私钥签名
     */
    public static String sign(String data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateKey);
        signature.update(data.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(signature.sign());
    }
    
    /**
     * 公钥验签
     */
    public static boolean verify(String data, String sign, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicKey);
        signature.update(data.getBytes("UTF-8"));
        return signature.verify(Base64.getDecoder().decode(sign));
    }
    
    /**
     * 将私钥转换为Base64字符串
     */
    public static String privateKeyToBase64(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }
    
    /**
     * 将公钥转换为Base64字符串
     */
    public static String publicKeyToBase64(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
    
    /**
     * 从Base64字符串加载私钥
     */
    public static PrivateKey loadPrivateKeyFromBase64(String base64PrivateKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64PrivateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        return keyFactory.generatePrivate(keySpec);
    }
    
    /**
     * 从Base64字符串加载公钥
     */
    public static PublicKey loadPublicKeyFromBase64(String base64PublicKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64PublicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        return keyFactory.generatePublic(keySpec);
    }
}
