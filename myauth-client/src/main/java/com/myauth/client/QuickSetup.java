package com.myauth.client;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 快速配置工具 - 自动生成测试用的公钥和授权文件
 */
public class QuickSetup {
    
    private static final String BASE_URL = "http://localhost:8080/api";
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  MyAuth Client - 快速配置工具");
        System.out.println("========================================\n");
        
        try {
            // 1. 检查后端是否运行
            System.out.println("[1/4] 检查授权管理系统是否运行...");
            if (!isServerRunning()) {
                System.err.println("❌ 错误：授权管理系统未运行！");
                System.err.println("请先启动 myauth-generator 服务");
                System.exit(1);
            }
            System.out.println("✅ 授权管理系统运行正常\n");
            
            // 2. 生成密钥对
            System.out.println("[2/4] 生成密钥对...");
            generateKeyPair();
            System.out.println("✅ 密钥对生成成功\n");
            
            // 3. 获取公钥
            System.out.println("[3/4] 获取公钥...");
            String publicKey = getPublicKey();
            savePublicKey(publicKey);
            System.out.println("✅ 公钥已保存到 license/public.key\n");
            
            // 4. 生成授权码
            System.out.println("[4/4] 生成测试授权码...");
            String licenseKey = generateLicense();
            saveLicenseKey(licenseKey);
            System.out.println("✅ 授权码已保存到 license/license.key\n");
            
            System.out.println("========================================");
            System.out.println("  配置完成！");
            System.out.println("========================================");
            System.out.println("\n现在可以启动客户端了：");
            System.out.println("  mvn spring-boot:run");
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("\n❌ 配置失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * 检查服务器是否运行
     */
    private static boolean isServerRunning() {
        try {
            HttpRequest.get(BASE_URL + "/license/public-key")
                    .timeout(3000)
                    .execute();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 生成密钥对
     */
    private static void generateKeyPair() {
        String response = HttpRequest.post(BASE_URL + "/license/generate-keypair")
                .timeout(5000)
                .execute()
                .body();
        
        JSONObject json = JSONUtil.parseObj(response);
        if (json.getInt("code") != 200) {
            throw new RuntimeException("生成密钥对失败: " + json.getStr("message"));
        }
    }
    
    /**
     * 获取公钥
     */
    private static String getPublicKey() {
        String response = HttpRequest.get(BASE_URL + "/license/public-key")
                .timeout(5000)
                .execute()
                .body();
        
        JSONObject json = JSONUtil.parseObj(response);
        if (json.getInt("code") != 200) {
            throw new RuntimeException("获取公钥失败: " + json.getStr("message"));
        }
        
        return json.getStr("data");
    }
    
    /**
     * 保存公钥到文件
     */
    private static void savePublicKey(String publicKey) throws Exception {
        File dir = new File("license");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        File file = new File("license/public.key");
        Files.writeString(file.toPath(), publicKey);
    }
    
    /**
     * 生成授权码
     */
    private static String generateLicense() {
        // 构建授权数据
        JSONObject licenseData = new JSONObject();
        licenseData.set("customerName", "测试客户");
        licenseData.set("type", "professional");
        licenseData.set("expireTime", LocalDate.now().plusYears(1).format(DateTimeFormatter.ISO_DATE));
        licenseData.set("maxUsers", 100);
        licenseData.set("machineId", "");
        licenseData.set("modules", "[\"module1\", \"module2\"]");
        licenseData.set("remark", "测试授权码");
        
        String response = HttpRequest.post(BASE_URL + "/license/generate")
                .body(licenseData.toString())
                .contentType("application/json")
                .timeout(5000)
                .execute()
                .body();
        
        JSONObject json = JSONUtil.parseObj(response);
        if (json.getInt("code") != 200) {
            throw new RuntimeException("生成授权码失败: " + json.getStr("message"));
        }
        
        return json.getJSONObject("data").getStr("licenseKey");
    }
    
    /**
     * 保存授权码到文件
     */
    private static void saveLicenseKey(String licenseKey) throws Exception {
        File dir = new File("license");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        File file = new File("license/license.key");
        Files.writeString(file.toPath(), licenseKey);
    }
}
