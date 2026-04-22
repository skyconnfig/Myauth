# MyAuth Client 配置指南

## 问题说明

启动客户端时出现以下错误：
```
License验证失败: 未配置公钥，请在application.yml中配置license.public-key
```

这是因为客户端需要公钥来验证授权码的有效性。

---

## 解决方案（3种方式）

### 方式一：使用快速配置工具（推荐）⭐

这是最简单的方式，自动完成所有配置。

#### 步骤：

1. **启动授权管理系统**
   ```bash
   cd myauth-generator
   mvn spring-boot:run
   ```

2. **运行快速配置工具**
   ```bash
   cd myauth-client
   mvn compile exec:java -Dexec.mainClass="com.myauth.client.QuickSetup"
   ```

3. **启动客户端**
   ```bash
   mvn spring-boot:run
   ```

✅ 完成！系统会自动生成公钥和授权文件。

---

### 方式二：手动通过 API 配置

#### 步骤：

1. **启动授权管理系统**
   ```bash
   cd myauth-generator
   mvn spring-boot:run
   ```

2. **访问 API 文档**
   
   打开浏览器：http://localhost:8080/doc.html

3. **生成密钥对**
   - 找到 "密钥管理" -> "生成密钥对"
   - 点击 "Try it out" -> "Execute"
   - 记录返回的公钥路径

4. **获取公钥**
   - 找到 "密钥管理" -> "获取公钥"
   - 点击 "Try it out" -> "Execute"
   - 复制返回的公钥内容（data 字段）

5. **保存公钥**
   
   在 `myauth-client` 目录下创建 `license/public.key` 文件：
   ```
   license/
     └── public.key  （粘贴公钥内容）
   ```

6. **生成授权码**
   - 找到 "License管理" -> "生成新的License"
   - 填写信息：
     ```json
     {
       "customerName": "测试客户",
       "type": "professional",
       "expireTime": "2027-12-31",
       "maxUsers": 100,
       "machineId": "",
       "modules": "[\"module1\", \"module2\"]",
       "remark": "测试授权"
     }
     ```
   - 点击 "Execute"
   - 复制返回的 `licenseKey`

7. **保存授权码**
   
   在 `myauth-client` 目录下创建 `license/license.key` 文件：
   ```
   license/
     ├── public.key    （已创建）
     └── license.key   （粘贴授权码）
   ```

8. **启动客户端**
   ```bash
   cd myauth-client
   mvn spring-boot:run
   ```

---

### 方式三：配置文件方式

#### 步骤：

1. **获取公钥**（参考方式二的步骤 1-5）

2. **编辑配置文件**
   
   编辑 `myauth-client/src/main/resources/application.yml`：
   ```yaml
   license:
     public-key: "粘贴公钥内容"  # 将公钥粘贴到这里
     license-file: license/license.key
   ```

3. **生成并保存授权码**（参考方式二的步骤 6-7）

4. **启动客户端**
   ```bash
   cd myauth-client
   mvn spring-boot:run
   ```

---

## 文件结构

配置完成后，`myauth-client` 目录应该是这样的：

```
myauth-client/
├── license/
│   ├── public.key      # 公钥文件（从生成器获取）
│   └── license.key     # 授权码文件（从生成器获取）
├── src/
├── pom.xml
└── setup-test.bat      # Windows 配置助手
```

---

## 常见问题

### Q1: 提示 "未找到License文件"

**解决**：确保 `license/license.key` 文件存在且包含有效的授权码。

### Q2: 提示 "授权验证失败：签名无效"

**解决**：
- 检查公钥是否正确
- 确保公钥和授权码是同一对密钥生成的
- 重新生成密钥对和授权码

### Q3: 提示 "授权已过期"

**解决**：
- 检查系统时间是否正确
- 联系管理员延期授权
- 或在生成器中重新生成授权码

### Q4: 如何更新授权？

**解决**：
1. 从生成器获取新的授权码
2. 覆盖 `license/license.key` 文件
3. 重启客户端

---

## 验证配置

启动客户端后，如果看到以下信息表示配置成功：

```
===========================================
   License验证成功！
   客户名称: 测试客户
   授权类型: professional
   过期时间: 2027-12-31
   剩余天数: 365
   最大用户数: 100
===========================================
```

---

## 生产环境建议

1. **妥善保管私钥**
   - 私钥只在生成器服务器
   - 不要泄露给客户端

2. **公钥分发**
   - 可以将公钥打包到客户端
   - 或通过安全渠道分发

3. **授权文件保护**
   - 设置文件权限
   - 防止被篡改

4. **定期更新**
   - 建议在授权到期前30天更新
   - 可以使用定时任务提醒

---

**配置完成！** 🎉

如有问题，请查看日志输出中的详细错误信息。
