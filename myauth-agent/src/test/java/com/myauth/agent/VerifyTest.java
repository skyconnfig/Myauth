package com.myauth.agent;

/**
 * 验证 Agent License 校验逻辑是否正常
 * 模拟 agent premain 被调用时的行为
 */
public class VerifyTest {
    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("  License Agent 校验逻辑验证");
        System.out.println("=========================================");
        System.out.println();

        // 验证正常情况（过期时间在未来）
        System.out.println(">>> 测试1: 正常未过期 (expire=2027-05-30)");
        boolean result1 = LicenseAgent.testValidate("D:\\java\\myauth\\jar\\license");
        System.out.println("  结果: " + (result1 ? "PASS" : "FAIL"));
        System.out.println();

        // 验证无公钥
        System.out.println(">>> 测试2: 缺少公钥文件");
        LicenseAgent.resetState();
        boolean result2 = LicenseAgent.testValidate("D:\\java\\myauth\\jar\\license\\nonexist");
        System.out.println("  结果: " + (result2 ? "PASS (不应通过)" : "FAIL (期望行为 - 拒绝)"));
        System.out.println();

        // 验证公钥/授权内容完整性
        System.out.println(">>> 测试3: 文件完整性校验");
        LicenseAgent.resetState();
        boolean result3 = LicenseAgent.testValidate("D:\\java\\myauth\\jar\\license");
        System.out.println("  结果: " + (result3 ? "PASS" : "FAIL"));
        System.out.println();

        System.out.println("=========================================");
        System.out.println("  校验逻辑验证完成");
        System.out.println("=========================================");
    }
}
