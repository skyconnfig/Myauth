# kiftd 授权过期控制方案 — 加密与验证详解

## 概述

为已打包的第三方 JAR 程序（kiftd-1.1.0-RELEASE.jar）注入过期时间控制，**不修改原始 JAR**，通过 `-javaagent` 参数挂载 License Agent，在 JVM 启动时执行 RSA 签名验证 + 时间校验。

---

## 一、整体架构

```
┌─────────────────────────────────────────────────────────┐
│                    授权管理端 (KeyGen)                     │
│                                                         │
│  1. 生成 RSA 2048 密钥对                                  │
│  2. 构建 License JSON（含 expireTime）                     │
│  3. 用私钥签名 → Base64 编码 → 写入 license.key            │
│  4. 公钥写入 public.key                                   │
└────────────────────────────┬────────────────────────────┘
                             │
            license.key  +  public.key (外部文件)
                             │
                             ▼
┌─────────────────────────────────────────────────────────┐
│                  客户端 (Java Agent)                       │
│                                                         │
│  JVM启动时：(premain)                                     │
│  ├─ 读取 public.key                                      │
│  ├─ 读取 license.key                                     │
│  ├─ Base64 解码 → 分离 JSON 数据和签名                     │
│  ├─ RSA-SHA256 验签                                      │
│  ├─ 解析 expireTime 并与当前时间比对                        │
│  ├─ 通过 → 继续启动主程序                                  │
│  ├─ 失败 → System.exit(1) 退出                            │
│                                                         │
│  运行期间：(定时任务, 每小时)                               │
│  └─ 重新验证 → 防时间回退攻击                              │
└─────────────────────────────────────────────────────────┘
```

---

## 二、RSA 密钥对生成

### 算法参数

| 参数          | 值            |
|---------------|---------------|
| 算法          | RSA           |
| 密钥长度      | 2048 bit      |
| 签名算法      | SHA256withRSA |
| 私钥格式      | PKCS#8        |
| 公钥格式      | X.509 SubjectPublicKeyInfo |

### 核心代码

```java
KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
keyGen.initialize(2048);
KeyPair keyPair = keyGen.generateKeyPair();
PrivateKey privateKey = keyPair.getPrivate();   // 签名用
PublicKey  publicKey  = keyPair.getPublic();    // 验签用
```

### 保存格式

公钥和私钥均以 **Base64 编码** 保存为纯文本文件，方便存储和分发。

```java
// 公钥 → Base64
String pubKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
Files.write(Paths.get("license/public.key"), pubKeyBase64.getBytes(UTF_8));

// 私钥 → Base64
String privKeyBase64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());
Files.write(Paths.get("license/private.key"), privKeyBase64.getBytes(UTF_8));
```

#### 公钥文件示例 (public.key)

```
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyA7DFi6...
```

---

## 三、License 授权文件生成

### 3.1 License JSON 数据结构

```json
{
  "licenseKey":  "KIFTD-LICENSE-001",
  "expireTime":  "2027-05-30",
  "machineId":   "",
  "type":        "enterprise",
  "maxUsers":    9999,
  "modules":     "[]",
  "customerName":"kiftd-enterprise",
  "remark":      "kiftd network disk - MyAuth License Agent"
}
```

| 字段         | 说明                     |
|--------------|--------------------------|
| licenseKey   | 授权码编号               |
| expireTime   | 过期日期 (yyyy-MM-dd)     |
| machineId    | 可选，绑定指定机器        |
| type         | 授权类型                 |
| maxUsers     | 最大用户数               |
| modules      | 功能模块（JSON数组字符串）|
| customerName | 客户名称                 |
| remark       | 备注说明                 |

### 3.2 签名流程

```
License JSON 字符串
        │
        ▼
   SHA256withRSA   ◄── 私钥 (private.key)
        │
        ▼
   Base64 编码签名
        │
        ▼
   JSON数据 + "." + Base64签名  → 组合字符串
        │
        ▼
   整体 Base64 编码  → 最终 license.key
```

#### 核心代码

```java
// 1. 私钥签名
Signature signature = Signature.getInstance("SHA256withRSA");
signature.initSign(privateKey);
signature.update(licenseJson.getBytes(StandardCharsets.UTF_8));
byte[] signBytes = signature.sign();
String signBase64 = Base64.getEncoder().encodeToString(signBytes);

// 2. 组合 data.signature
String combined = licenseJson + "." + signBase64;

// 3. 整体 Base64 编码
String licenseKey = Base64.getEncoder().encodeToString(
    combined.getBytes(StandardCharsets.UTF_8)
);

// 4. 写入文件
Files.write(Paths.get("license/license.key"), licenseKey.getBytes(StandardCharsets.UTF_8));
```

---

## 四、校验流程 (Agent 端)

### 4.1 解码过程

```
license.key (Base64 字符串)
        │
        ▼
   Base64 解码
        │
        ▼
   JSON数据.signature  (以最后一个 '.' 分隔)
   │           │
   │           ▼
   │      Base64 解码签名
   │
   ▼
   原始 License JSON
```

#### 核心代码

```java
// 解码
byte[] decodedBytes = Base64.getDecoder().decode(licenseKey);
String decoded = new String(decodedBytes, StandardCharsets.UTF_8);
int dotIndex = decoded.lastIndexOf('.');
String jsonData = decoded.substring(0, dotIndex);
String signature = decoded.substring(dotIndex + 1);
```

### 4.2 验签过程

```
JSON数据 + 签名 + 公钥
        │
        ▼
   SHA256withRSA.verify()
        │
   ┌────┴────┐
   │         │
   通过     失败 → 退出 JVM
    │
    ▼
   解析 expireTime
        │
        ▼
   与 LocalDate.now() 比较
        │
   ┌────┴────┐
   │         │
  未过期    已过期 → 退出 JVM
    │
    ▼
   启动主程序
```

#### 核心代码

```java
// 1. 加载公钥
byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
KeyFactory keyFactory = KeyFactory.getInstance("RSA");
PublicKey publicKey = keyFactory.generatePublic(keySpec);

// 2. 验签
Signature sig = Signature.getInstance("SHA256withRSA");
sig.initVerify(publicKey);
sig.update(jsonData.getBytes(StandardCharsets.UTF_8));
boolean valid = sig.verify(Base64.getDecoder().decode(signature));
if (!valid) { System.exit(1); }

// 3. 解析过期时间
// 用正则从JSON中提取 "expireTime":"2027-05-30"
Pattern pattern = Pattern.compile("\"expireTime\"\\s*:\\s*\"([^\"]+)\"");
Matcher matcher = pattern.matcher(jsonData);
String expireTime = matcher.find() ? matcher.group(1) : null;

// 4. 比较日期
LocalDate expireDate = LocalDate.parse(expireTime, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
if (LocalDate.now().isAfter(expireDate)) { System.exit(1); }
```

### 4.3 防时间回退机制

程序启动后，每隔 **1小时**（可配置）自动重新执行一次完整的验证流程：

```java
Timer timer = new Timer("license-checker", true);
timer.scheduleAtFixedRate(new TimerTask() {
    @Override
    public void run() {
        if (!validateLicense(licenseDir)) {
            System.err.println("定时验证失败 - 时间可能被篡改！");
            System.exit(1);
        }
    }
}, intervalSeconds * 1000, intervalSeconds * 1000);
```

这样可以防止用户：
- 启动程序后把系统时间改回到过期日期之前
- 修改 `license.key` 或 `public.key` 文件内容

---

## 五、文件清单与格式

### 完整文件列表

```
jar/
├── kiftd-1.1.0-RELEASE.jar          # 原始程序 (未修改)
├── myauth-agent.jar                  # Java Agent (9.7KB)
├── start-kiftd.bat                   # 启动脚本
└── license/
    ├── public.key    (392 bytes)     # RSA 公钥 (Base64 文本)
    ├── license.key   (744 bytes)     # 授权文件 (Base64 编码签名数据)
    └── private.key   (1.6 KB)        # RSA 私钥 (仅授权管理端保存)
```

### 文件安全

| 文件         | 安全级别 | 说明                               |
|--------------|----------|------------------------------------|
| public.key   | 可公开   | 用于验签，随程序分发                 |
| license.key  | 防篡改   | 一旦修改，签名失效，拒绝运行          |
| private.key  | 绝密     | 仅用于生成授权，**严禁随程序分发**    |

---

## 六、命令行参数

Agent 支持以下 JVM 系统属性：

| 参数                         | 默认值      | 说明                        |
|------------------------------|-------------|-----------------------------|
| `-Dagent.license.dir`        | `./license` | License 文件所在目录         |
| `-Dagent.exit.on.fail`       | `true`      | 验证失败是否退出程序          |
| `-Dagent.check.interval`     | `3600`      | 定时检查间隔（秒）           |

### 启动命令

```bash
java -javaagent:myauth-agent.jar ^
     -Dagent.license.dir=./license ^
     -jar kiftd-1.1.0-RELEASE.jar
```

---

## 七、如何续期 / 生成新授权

### 方法一：使用 KeyGen 工具

```bash
# 切换到授权目录，运行 KeyGen 重新生成（需保留私钥）
java -cp myauth-agent.jar com.myauth.agent.KeyGen ./license
```

### 方法二：手动修改（需要保留原始私钥）

1. 将 `license/private.key` 放到安全位置
2. 修改 KeyGen.java 中的 `expireTime` 字段
3. 重新编译运行 KeyGen
4. 将新生成的 `license.key` 替换到客户端

### 方法三：与 MyAuth 生成器集成

如果你已经配置好了 MyAuth 项目的 `myauth-generator` 模块：

1. 将 KeyGen 生成的私钥导入生成器
2. 通过 Web UI 生成新的 License Key
3. 将 License Key 保存为 `license/license.key`

---

## 八、安全特性总结

| 特性                | 实现方式                                      |
|---------------------|-----------------------------------------------|
| 防篡改              | RSA 2048 签名，修改任何字节都会导致验签失败      |
| 防伪造              | 没有私钥无法生成合法授权                         |
| 防时间回退          | 每小时定时重新验证                               |
| 防复制              | 可扩展：添加 machineId 绑定机器码               |
| 无侵入              | Java Agent 方式，不修改原始 JAR                |
| 零依赖              | Agent 仅使用 JDK 标准库，无第三方依赖           |
| 兼容性              | 适用于任意 Java 程序（JAR/WAR/类文件）          |

---

## 九、完整数据流示例

```
┌─ 生成端 ───────────────────────────────────────────────┐
│                                                        │
│  RSA密钥对                                             │
│  ├─ PrivateKey (PKCS#8) → Base64 → private.key         │
│  └─ PublicKey  (X.509)  → Base64 → public.key          │
│                                                        │
│  License JSON:                                          │
│  {"expireTime":"2027-05-30","customerName":"kiftd",...} │
│       │                                                 │
│       ▼ SHA256withRSA                                   │
│  签名值 (byte[])                                        │
│       │                                                 │
│       ▼ Base64                                          │
│  签名串 (String)                                        │
│       │                                                 │
│  JSON数据 + "." + 签名串                                │
│       │                                                 │
│       ▼ Base64                                          │
│  license.key (最终文件)                                  │
│                                                        │
└────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─ 客户端 ───────────────────────────────────────────────┐
│                                                        │
│  license.key                                           │
│       │                                                 │
│       ▼ Base64解码                                      │
│  JSON数据.signature                                     │
│       │                                                 │
│   ┌───┴───┐                                            │
│   │       │                                             │
│   ▼       ▼                                             │
│  JSON   签名串                                           │
│   │       │                                             │
│   │       ▼ Base64解码                                   │
│   │   签名值 (byte[])                                    │
│   │       │                                             │
│   ▼       ▼ SHA256withRSA.verify(publicKey)             │
│  时间校验 ◄──── 验签结果                                  │
│   │                                                      │
│   ▼                                                      │
│  通过 → 启动程序 / 失败 → System.exit(1)                 │
│                                                        │
└────────────────────────────────────────────────────────┘
```

---

## 十、常见问题

**Q: 如果用户直接删除 `license` 目录会怎样？**
A: Agent 找不到公钥文件 → 验证失败 → 程序退出。

**Q: 如果用户复制 `license` 目录到其他机器能用吗？**
A: 可以，当前未绑定机器码。如果需要防止复制，可在 KeyGen 生成时传入 `machineId` 字段。

**Q: 如果用户把系统时间调到 2026 年再启动？**
A: 能通过启动校验，但定时器每小时会检查一次。如果用户运行期间把时间调回 2026，下一次定时检查时会发现时间回退 → 退出。

**Q: Agent JAR 会影响正常运行吗？**
A: 不会。Agent 仅执行一次 premain 方法（数毫秒）然后启动定时线程，对性能无影响。
