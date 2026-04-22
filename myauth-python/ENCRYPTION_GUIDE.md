# 加密方案对比与选择指南

## 📊 方案对比

### 1. MD5（❌ 不推荐）

**优点：**
- 实现简单
- 计算速度快

**缺点：**
- ❌ **可被碰撞攻击**：已证明可以生成相同 MD5 的不同数据
- ❌ **无签名机制**：无法验证数据来源
- ❌ **对称加密**：密钥泄露即失效
- ❌ **不适合授权验证**

**示例代码（仅用于学习，不要用于生产）：**

```python
import hashlib
import json
from datetime import datetime

def generate_md5_license(expire_date, secret_key):
    """生成 MD5 License（不安全！）"""
    data = {
        "expire_time": expire_date,
        "type": "trial",
        "timestamp": datetime.now().isoformat()
    }
    
    # 生成签名
    json_str = json.dumps(data, sort_keys=True)
    signature = hashlib.md5((json_str + secret_key).encode()).hexdigest()
    
    # 组合
    license_data = f"{json_str}|{signature}"
    return license_data

def verify_md5_license(license_str, secret_key):
    """验证 MD5 License（不安全！）"""
    try:
        parts = license_str.rsplit('|', 1)
        if len(parts) != 2:
            return False
        
        json_str, signature = parts
        
        # 重新计算签名
        expected_signature = hashlib.md5((json_str + secret_key).encode()).hexdigest()
        
        # 比较
        if signature != expected_signature:
            return False
        
        # 解析数据
        data = json.loads(json_str)
        
        # 检查过期时间
        expire_date = datetime.strptime(data['expire_time'], '%Y-%m-%d')
        if datetime.now() > expire_date:
            return False
        
        return True
    except:
        return False

# 使用
secret = "my-secret-key"
license_key = generate_md5_license("2026-12-31", secret)
is_valid = verify_md5_license(license_key, secret)
```

**为什么 MD5 不安全？**
1. 彩虹表攻击：可以预先计算常见数据的 MD5
2. 碰撞攻击：可以找到不同数据产生相同 MD5
3. 暴力破解：如果密钥简单，可以被猜解

---

### 2. HMAC-SHA256（⚠️ 中等安全）

**优点：**
- ✅ 比 MD5 安全得多
- ✅ 难以碰撞
- ✅ 实现相对简单

**缺点：**
- ⚠️ **对称加密**：密钥必须在客户端和服务器端都保存
- ⚠️ **密钥管理复杂**：如果密钥泄露，所有人都可以生成假 License
- ⚠️ **不支持离线分发**：需要安全通道传输密钥

**示例代码：**

```python
import hmac
import hashlib
import json
import base64
from datetime import datetime

def generate_hmac_license(expire_date, secret_key):
    """生成 HMAC-SHA256 License"""
    data = {
        "expire_time": expire_date,
        "type": "trial",
        "customer": "Test Customer"
    }
    
    # 生成签名
    json_str = json.dumps(data, sort_keys=True)
    signature = hmac.new(
        secret_key.encode(),
        json_str.encode(),
        hashlib.sha256
    ).hexdigest()
    
    # 编码
    license_data = base64.b64encode(
        f"{json_str}.{signature}".encode()
    ).decode()
    
    return license_data

def verify_hmac_license(license_str, secret_key):
    """验证 HMAC-SHA256 License"""
    try:
        # 解码
        decoded = base64.b64decode(license_str).decode()
        parts = decoded.rsplit('.', 1)
        
        if len(parts) != 2:
            return False, "格式错误"
        
        json_str, signature = parts
        
        # 重新计算签名
        expected_signature = hmac.new(
            secret_key.encode(),
            json_str.encode(),
            hashlib.sha256
        ).hexdigest()
        
        # 比较（使用时间常量比较防止时序攻击）
        if not hmac.compare_digest(signature, expected_signature):
            return False, "签名无效"
        
        # 解析数据
        data = json.loads(json_str)
        
        # 检查过期时间
        expire_date = datetime.strptime(data['expire_time'], '%Y-%m-%d')
        if datetime.now() > expire_date:
            return False, "已过期"
        
        return True, data
    except Exception as e:
        return False, str(e)

# 使用
secret = "your-secret-key-keep-it-safe"
license_key = generate_hmac_license("2026-12-31", secret)
is_valid, result = verify_hmac_license(license_key, secret)
```

**适用场景：**
- 内部系统
- 密钥可以安全保管的环境
- 不需要分发给第三方

---

### 3. RSA-SHA256（✅ 强烈推荐）

**优点：**
- ✅ **非对称加密**：私钥签名，公钥验证
- ✅ **无法逆向**：从公钥无法推导私钥
- ✅ **支持离线验证**：公钥可以公开
- ✅ **行业标准**：广泛使用，经过充分测试
- ✅ **防篡改**：任何修改都会导致验证失败

**缺点：**
- 实现稍复杂（但 MyAuth 已经封装好）
- 计算速度略慢（但影响可忽略）

**示例代码（使用 MyAuth）：**

```python
from license_validator import LicenseManager, LicenseValidator

# 方式1：使用管理器（推荐）
manager = LicenseManager(public_key_path="license/public.key")
license_info = manager.initialize()

print(f"客户: {license_info.customer_name}")
print(f"类型: {license_info.type}")
print(f"过期时间: {license_info.expire_time}")

# 方式2：直接验证
validator = LicenseValidator(public_key_path="license/public.key")
license_obj = validator.validate(license_key)

# 检查剩余天数
remaining_days = LicenseValidator.get_remaining_days(license_obj.expire_time)
print(f"剩余天数: {remaining_days}")
```

**Java 生成 License：**

```java
// 在 Java 管理界面生成
License license = new License();
license.setExpireTime("2026-12-31");
license.setType("professional");
license.setCustomerName("测试客户");

// 使用私钥签名
String licenseKey = licenseGeneratorService.generate(license);
```

**工作流程：**

```
1. Java 服务器（私钥）
   ↓ 生成并签名
2. License Key
   ↓ 分发给客户
3. Python 客户端（公钥）
   ↓ 验证签名
4. 验证通过 ✓
```

**为什么 RSA 最安全？**
1. 私钥只在服务器端，永远不会泄露给客户端
2. 即使公钥被公开，也无法伪造 License
3. 每个 License 都有唯一签名，无法篡改
4. 支持机器码绑定，防止复制

---

## 🔐 安全级别对比

| 特性 | MD5 | HMAC-SHA256 | RSA-SHA256 |
|------|-----|-------------|------------|
| 抗碰撞性 | ❌ 弱 | ✅ 强 | ✅ 强 |
| 密钥管理 | ⚠️ 困难 | ⚠️ 困难 | ✅ 简单 |
| 离线验证 | ❌ 不支持 | ⚠️ 需共享密钥 | ✅ 完美支持 |
| 防伪造 | ❌ 容易 | ⚠️ 密钥泄露即可 | ✅ 几乎不可能 |
| 行业标准 | ❌ 过时 | ⚠️ 一般 | ✅ 推荐 |
| 实现难度 | ✅ 简单 | ✅ 简单 | ⚠️ 中等 |
| 性能 | ✅ 最快 | ✅ 快 | ✅ 足够快 |

---

## 💡 推荐方案

### 生产环境（强烈推荐）

**使用 RSA-SHA256（MyAuth 默认方案）**

理由：
- 最高安全性
- 支持离线验证
- 易于管理和分发
- 行业标准

### 内部系统（可选）

**使用 HMAC-SHA256**

适用场景：
- 公司内部使用
- 密钥可以安全保管
- 不需要分发给外部客户

### 绝对不要使用

**MD5 或任何纯哈希算法**

原因：
- 已被证明不安全
- 容易被破解
- 不适合授权验证

---

## 🛡️ 最佳实践

### 1. 多层验证

```python
# 启动时验证
manager.initialize()

# 定时验证（每小时）
schedule.every().hour.do(manager.check_license)

# 关键操作前验证
@app.route('/api/premium-feature')
@require_license
def premium_feature():
    # 再次验证
    if not manager.check_license():
        return {"error": "License 无效"}, 403
```

### 2. 机器码绑定

```python
# 生成 License 时绑定机器码
license = {
    "expire_time": "2026-12-31",
    "machine_id": "ABC123DEF456",  # 客户机器码
    "type": "professional"
}

# 验证时检查
validator.validate(license_key, check_machine_id=True)
```

### 3. 日志记录

```python
import logging

logging.basicConfig(filename='license.log', level=logging.INFO)

def log_verification(result, license_key=None):
    logging.info(f"验证结果: {result}, Key: {license_key[:20]}...")
```

### 4. 错误处理

```python
try:
    manager.initialize()
except Exception as e:
    logging.error(f"License 验证失败: {e}")
    # 根据策略决定：退出、试用模式、或继续运行
    if ENFORCE_LICENSE:
        sys.exit(1)
    else:
        enable_trial_mode()
```

---

## 📈 性能对比

| 操作 | MD5 | HMAC-SHA256 | RSA-SHA256 |
|------|-----|-------------|------------|
| 单次验证 | ~0.01ms | ~0.05ms | ~1ms |
| 内存占用 | ~1KB | ~2KB | ~5KB |
| 对应用影响 | 忽略不计 | 忽略不计 | 忽略不计 |

**结论：** RSA 的性能完全可接受，不应该成为选择其他方案的 reason。

---

## 🎯 总结

### 选择建议

1. **如果你在开发商业软件** → 使用 RSA-SHA256（MyAuth）
2. **如果你是内部系统** → 可以使用 HMAC-SHA256
3. **如果你考虑 MD5** → 请重新考虑，选择不安全的方案会带来更大风险

### MyAuth 的优势

- ✅ 已经实现了完整的 RSA-SHA256 方案
- ✅ 支持 Java、Python (Flask/FastAPI)、Vue 3
- ✅ 提供完整的管理界面
- ✅ 支持机器码绑定
- ✅ 支持定时验证
- ✅ 开源且免费

### 下一步

1. 阅读 [Python 集成指南](README_PYTHON_INTEGRATION.md)
2. 运行示例代码
3. 生成测试 License
4. 集成到你的项目

---

**记住：安全无小事，选择合适的加密方案至关重要！** 🔐
