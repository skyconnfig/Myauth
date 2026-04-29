package com.myauth.agent;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MyAuth License Agent
 * <p>
 * 为已打包的第三方JAR提供License授权过期控制，无需修改原始JAR文件。
 * <p>
 * 用法:
 * java -javaagent:myauth-agent.jar [-Dagent.license.dir=./license] -jar your-app.jar
 * <p>
 * 系统属性:
 * -Dagent.license.dir=./license  指定license目录（默认./license）
 * -Dagent.exit.on.fail=true      验证失败时是否退出程序（默认true）
 * -Dagent.check.interval=3600    定时检查间隔秒数（默认3600=1小时）
 */
public class LicenseAgent {

    private static final String ALGORITHM_RSA = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    private static final String LICENSE_FILE = "license.key";
    private static final String PUBLIC_KEY_FILE = "public.key";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static volatile boolean licenseValid = false;
    private static volatile String expireTimeStr = null;
    private static volatile String customerName = null;
    private static volatile String licenseType = null;

    /**
     * JVM Agent入口 - premain (JDK 1.5+)
     */
    public static void premain(String args, Instrumentation inst) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║        MyAuth License Agent v1.0            ║");
        System.out.println("║      授权过期控制 - 无需修改原始JAR          ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.println();

        // 读取配置
        String licenseDir = System.getProperty("agent.license.dir", "./license");
        boolean exitOnFail = Boolean.parseBoolean(System.getProperty("agent.exit.on.fail", "true"));
        long checkInterval = Long.parseLong(System.getProperty("agent.check.interval", "3600"));

        System.out.println("  授权目录: " + new File(licenseDir).getAbsolutePath());
        System.out.println();

        // 执行验证
        boolean passed = validateLicense(licenseDir);

        if (!passed) {
            System.err.println("  ❌ 授权验证失败！");
            if (exitOnFail) {
                System.err.println("  ⚠️  程序将在5秒后自动退出...");
                sleepSafe(5000);
                System.exit(1);
            } else {
                System.err.println("  ⚠️  授权验证失败，但程序将继续运行（受限模式）");
            }
        }

        // 启动定时检查（防时间回退）
        startPeriodicCheck(licenseDir, checkInterval);
    }

    // ==================== 授权验证逻辑 ====================

    /**
     * 验证License
     */
    private static boolean validateLicense(String licenseDir) {
        try {
            String publicKeyPath = licenseDir + File.separator + PUBLIC_KEY_FILE;
            String licenseKeyPath = licenseDir + File.separator + LICENSE_FILE;

            // 1. 检查公钥文件
            File pubKeyFile = new File(publicKeyPath);
            if (!pubKeyFile.exists()) {
                System.err.println("  ❌ 未找到公钥文件: " + publicKeyPath);
                return false;
            }

            // 2. 检查License文件
            File licFile = new File(licenseKeyPath);
            if (!licFile.exists()) {
                System.err.println("  ❌ 未找到授权文件: " + licenseKeyPath);
                return false;
            }

            // 3. 读取文件内容
            String publicKeyBase64 = readFile(publicKeyPath);
            String licenseKey = readFile(licenseKeyPath);

            if (publicKeyBase64 == null || publicKeyBase64.isEmpty()) {
                System.err.println("  ❌ 公钥文件内容为空");
                return false;
            }
            if (licenseKey == null || licenseKey.isEmpty()) {
                System.err.println("  ❌ 授权文件内容为空");
                return false;
            }

            // 4. 解码License Key
            String[] parts = decodeLicenseKey(licenseKey.trim());
            String jsonData = parts[0];
            String signature = parts[1];

            // 5. 加载公钥
            PublicKey publicKey = loadPublicKey(publicKeyBase64.trim());

            // 6. RSA验签
            if (!verifySignature(jsonData, signature, publicKey)) {
                System.err.println("  ❌ 授权签名无效！文件可能被篡改");
                return false;
            }

            // 7. 解析JSON获取过期时间
            expireTimeStr = extractJsonValue(jsonData, "expireTime");
            customerName = extractJsonValue(jsonData, "customerName");
            licenseType = extractJsonValue(jsonData, "type");

            if (expireTimeStr == null || expireTimeStr.isEmpty()) {
                System.err.println("  ❌ 授权文件中缺少过期时间(expireTime)");
                return false;
            }

            // 8. 检查过期
            LocalDate expireDate;
            try {
                expireDate = LocalDate.parse(expireTimeStr, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.err.println("  ❌ 授权过期时间格式无效: " + expireTimeStr);
                return false;
            }

            LocalDate now = LocalDate.now();
            if (now.isAfter(expireDate)) {
                System.err.println("  ❌ 授权已过期！");
                System.err.println("     过期时间: " + expireTimeStr);
                System.err.println("     当前时间: " + now.format(DATE_FORMATTER));
                return false;
            }

            // 9. 全部通过
            licenseValid = true;
            long remainingDays = expireDate.toEpochDay() - now.toEpochDay();

            System.out.println("  ┌────────────────────────────────────────┐");
            System.out.println("  │  ✅ 授权验证通过                       │");
            System.out.println("  │  客户名称: " + padRight(customerName != null ? customerName : "-", 28) + "│");
            System.out.println("  │  授权类型: " + padRight(licenseType != null ? licenseType : "-", 28) + "│");
            System.out.println("  │  过期时间: " + padRight(expireTimeStr, 28) + "│");
            System.out.println("  │  剩余天数: " + padRight(remainingDays + " 天", 28) + "│");
            System.out.println("  └────────────────────────────────────────┘");
            System.out.println();

            if (remainingDays <= 30) {
                System.out.println("  ⚠️  警告: License 将在 " + remainingDays + " 天后过期，请及时续费！");
                System.out.println();
            } else if (remainingDays <= 7) {
                System.out.println("  ⚠️  紧急: License 仅剩 " + remainingDays + " 天，请立即续费！");
                System.out.println();
            }

            return true;

        } catch (Exception e) {
            System.err.println("  ❌ 验证过程发生异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 定时检查（防时间回退攻击）
     */
    private static void startPeriodicCheck(String licenseDir, long intervalSeconds) {
        Timer timer = new Timer("license-checker", true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (!validateLicense(licenseDir)) {
                        System.err.println("  ❌ 定时验证失败 - 授权可能已过期或时间被篡改！");
                        System.err.println("  ⚠️  程序将在10秒后退出...");
                        sleepSafe(10000);
                        System.exit(1);
                    }
                } catch (Exception e) {
                    System.err.println("  ⚠️  定时验证异常: " + e.getMessage());
                }
            }
        }, intervalSeconds * 1000, intervalSeconds * 1000);

        System.out.println("  ℹ️  定时验证已启动，每 " + formatInterval(intervalSeconds) + " 检查一次");
        System.out.println();
    }

    // ==================== 辅助方法 ====================

    /**
     * 解码License Key: Base64(data.signature) → [data, signature]
     */
    private static String[] decodeLicenseKey(String licenseKey) {
        byte[] decodedBytes = Base64.getDecoder().decode(licenseKey);
        String decoded = new String(decodedBytes, StandardCharsets.UTF_8);
        int dotIndex = decoded.lastIndexOf('.');
        if (dotIndex == -1) {
            throw new IllegalArgumentException("无效的License Key格式（缺少分隔符）");
        }
        String data = decoded.substring(0, dotIndex);
        String sign = decoded.substring(dotIndex + 1);
        return new String[]{data, sign};
    }

    /**
     * 从Base64加载RSA公钥
     */
    private static PublicKey loadPublicKey(String base64Key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * RSA-SHA256验签
     */
    private static boolean verifySignature(String data, String signature, PublicKey publicKey) throws Exception {
        Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
        sig.initVerify(publicKey);
        sig.update(data.getBytes(StandardCharsets.UTF_8));
        return sig.verify(Base64.getDecoder().decode(signature));
    }

    /**
     * 从JSON字符串中提取指定字段的值（简单解析，无外部依赖）
     */
    private static String extractJsonValue(String json, String key) {
        // 匹配 "key": "value" 格式
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        // 匹配 "key": value (不带引号的数字)
        Pattern numPattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(\\d+)");
        Matcher numMatcher = numPattern.matcher(json);
        if (numMatcher.find()) {
            return numMatcher.group(1);
        }
        return null;
    }

    /**
     * 读取文件内容（自动去除BOM）
     */
    private static String readFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        // 去除UTF-8 BOM
        if (bytes.length >= 3 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
            return new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8).trim();
        }
        return new String(bytes, StandardCharsets.UTF_8).trim();
    }

    /**
     * 安全休眠
     */
    private static void sleepSafe(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 测试验证逻辑（供外部调用，返回结果不退出JVM）
     */
    public static boolean testValidate(String licenseDir) {
        return validateLicense(licenseDir);
    }

    /**
     * 重置内部状态（供测试用）
     */
    public static void resetState() {
        licenseValid = false;
        expireTimeStr = null;
        customerName = null;
        licenseType = null;
    }

    /**
     * 获取验证结果状态
     */
    public static boolean isLicenseValid() {
        return licenseValid;
    }

    // ==================== 辅助方法 ====================

    /**
     * 格式化时间间隔
     */
    private static String formatInterval(long seconds) {
        if (seconds < 60) return seconds + "秒";
        if (seconds < 3600) return (seconds / 60) + "分钟";
        return (seconds / 3600) + "小时";
    }

    /**
     * 右填充字符串
     */
    private static String padRight(String s, int length) {
        if (s == null) s = "-";
        if (s.length() >= length) return s.substring(0, length - 1) + ".";
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < length) {
            sb.append(' ');
        }
        return sb.toString();
    }
}
