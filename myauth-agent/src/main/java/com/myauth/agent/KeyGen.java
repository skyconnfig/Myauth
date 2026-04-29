package com.myauth.agent;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 授权文件生成工具 - 用于生成RSA密钥对和License授权文件
 * <p>
 * 用法:
 *   java com.myauth.agent.KeyGen [目标目录]
 *   默认目标目录: ./license
 */
public class KeyGen {

    private static final String KEY_ALGORITHM = "RSA";
    private static final String SIGN_ALGORITHM = "SHA256withRSA";
    private static final int KEY_SIZE = 2048;

    public static void main(String[] args) throws Exception {
        String targetDir = args.length > 0 ? args[0] : "./license";

        System.out.println("=========================================");
        System.out.println("  MyAuth License 授权文件生成工具");
        System.out.println("=========================================");
        System.out.println();

        // 1. 生成RSA密钥对
        System.out.println("[1/4] 生成 RSA 2048 密钥对...");
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyGen.initialize(KEY_SIZE);
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();
        System.out.println("  OK - 密钥对生成完成");

        // 2. 保存公钥
        System.out.println("[2/4] 保存公钥...");
        File dir = new File(targetDir);
        if (!dir.exists()) dir.mkdirs();

        String pubKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        Files.write(Paths.get(targetDir, "public.key"), pubKeyBase64.getBytes(StandardCharsets.UTF_8));
        System.out.println("  OK - public.key 已保存到: " + new File(targetDir, "public.key").getAbsolutePath());
        System.out.println("  公钥内容: " + pubKeyBase64.substring(0, 40) + "...");

        // 3. 构建License JSON并签名
        System.out.println("[3/4] 生成 License 授权码...");
        String licenseJson = String.format(
            "{\"licenseKey\":\"%s\",\"expireTime\":\"%s\",\"machineId\":\"%s\",\"type\":\"%s\",\"maxUsers\":%d,\"modules\":\"%s\",\"customerName\":\"%s\",\"remark\":\"%s\"}",
            "KIFTD-LICENSE-001",
            "2027-05-30",
            "",
            "enterprise",
            9999,
            "[]",
            "kiftd-enterprise",
            "kiftd network disk - MyAuth License Agent"
        );
        System.out.println("  License JSON: " + licenseJson);

        // 用私钥签名
        Signature signature = Signature.getInstance(SIGN_ALGORITHM);
        signature.initSign(privateKey);
        signature.update(licenseJson.getBytes(StandardCharsets.UTF_8));
        String signBase64 = Base64.getEncoder().encodeToString(signature.sign());

        // 编码：Base64(data.signature)
        String combined = licenseJson + "." + signBase64;
        String licenseKey = Base64.getEncoder().encodeToString(combined.getBytes(StandardCharsets.UTF_8));
        System.out.println("  签名完成");

        // 4. 保存License Key
        System.out.println("[4/4] 保存 License Key 文件...");
        Files.write(Paths.get(targetDir, "license.key"), licenseKey.getBytes(StandardCharsets.UTF_8));
        System.out.println("  OK - license.key 已保存到: " + new File(targetDir, "license.key").getAbsolutePath());
        System.out.println("  License Key: " + licenseKey.substring(0, 60) + "...");

        // ====== 验证生成的License ======
        System.out.println();
        System.out.println("=========================================");
        System.out.println("  验证生成的 License...");
        System.out.println("=========================================");
        System.out.println();

        // 解码验证
        String decoded = new String(Base64.getDecoder().decode(licenseKey), StandardCharsets.UTF_8);
        int dotIdx = decoded.lastIndexOf('.');
        String data = decoded.substring(0, dotIdx);
        String sign = decoded.substring(dotIdx + 1);

        Signature verifier = Signature.getInstance(SIGN_ALGORITHM);
        verifier.initVerify(publicKey);
        verifier.update(data.getBytes(StandardCharsets.UTF_8));
        boolean valid = verifier.verify(Base64.getDecoder().decode(sign));

        System.out.println("  License验证: " + (valid ? "PASS" : "FAIL"));
        System.out.println("  授权数据: " + data);
        System.out.println("  过期时间: 2027-05-30");

        // 保存私钥（用于将来生成新的授权）
        System.out.println();
        System.out.println("[可选] 保存私钥...");
        String privKeyBase64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        Files.write(Paths.get(targetDir, "private.key"), privKeyBase64.getBytes(StandardCharsets.UTF_8));
        System.out.println("  private.key 已保存 (请妥善保管, 勿泄露)");

        System.out.println();
        System.out.println("=========================================");
        System.out.println("  生成完成！");
        System.out.println("=========================================");
        System.out.println();
        System.out.println("文件清单:");
        System.out.println("  " + new File(targetDir, "public.key").getAbsolutePath());
        System.out.println("  " + new File(targetDir, "license.key").getAbsolutePath());
        System.out.println("  " + new File(targetDir, "private.key").getAbsolutePath() + " (私钥, 请安全保管)");
        System.out.println();
        System.out.println("启动方式:");
        System.out.println("  java -javaagent:myauth-agent.jar -jar your-app.jar");
    }
}
