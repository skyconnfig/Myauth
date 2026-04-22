# MyAuth Python 集成 - 快速参考

## 🚀 5分钟快速开始

### 1. 安装

```bash
cd myauth-python
pip install cryptography flask  # 或 fastapi uvicorn
```

### 2. 获取公钥

从 Java 管理界面复制公钥，或从文件读取：
```
myauth-generator/license/public.key
```

### 3. Flask 示例（最少代码）

```python
from flask import Flask
from license_validator import LicenseManager

app = Flask(__name__)

# 初始化
manager = LicenseManager(public_key_path="license/public.key")
manager.initialize()

@app.route('/')
def index():
    if manager.check_license():
        return "✅ License 有效"
    return "❌ License 无效", 403

if __name__ == '__main__':
    app.run(port=5000)
```

### 4. FastAPI 示例（最少代码）

```python
from fastapi import FastAPI, HTTPException
from license_validator import LicenseManager

app = FastAPI()
manager = LicenseManager(public_key_path="license/public.key")
manager.initialize()

@app.get("/")
def index():
    if not manager.check_license():
        raise HTTPException(status_code=403, detail="License 无效")
    return {"status": "valid"}
```

---

## 📝 常用 API

### LicenseManager

```python
# 初始化
manager = LicenseManager(public_key_path="license/public.key")
manager.initialize()  # 返回 License 对象

# 检查状态
is_valid = manager.check_license()

# 获取信息
info = manager.get_license_info()
# {
#   "status": "valid",
#   "type": "professional",
#   "expire_time": "2026-12-31",
#   "remaining_days": 365,
#   ...
# }
```

### LicenseValidator

```python
# 直接验证
validator = LicenseValidator(public_key_path="license/public.key")
license_obj = validator.validate(license_key)

# 计算剩余天数
days = LicenseValidator.get_remaining_days("2026-12-31")

# 获取机器码
machine_id = LicenseValidator._get_machine_id()
```

---

## 🎨 Flask 装饰器

```python
from flask import g

def require_license(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        if not manager.check_license():
            return {"error": "License 无效"}, 403
        g.license_info = manager.get_license_info()
        return f(*args, **kwargs)
    return decorated

@app.route('/api/protected')
@require_license
def protected():
    return {"data": "受保护"}
```

---

## 🔧 FastAPI 依赖

```python
from fastapi import Depends

async def verify_license():
    if not manager.check_license():
        raise HTTPException(status_code=403, detail="License 无效")
    return manager.get_license_info()

@app.get("/api/protected")
def protected(info: dict = Depends(verify_license)):
    return {"data": "受保护", "license": info}
```

---

## 🌐 Vue 3 集成

```javascript
import { licenseValidator } from '@/utils/license'

// 初始化
await licenseValidator.initialize()

// 检查有效性
if (licenseValidator.isValid()) {
  console.log('License 有效')
}

// 激活
await licenseValidator.activate('YOUR_LICENSE_KEY')

// 获取信息
const info = licenseValidator.getLicenseInfo()
```

---

## ⚙️ 配置方式

### 方式1：公钥文件

```python
manager = LicenseManager(public_key_path="license/public.key")
```

### 方式2：Base64 字符串

```python
PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A..."
manager = LicenseManager(public_key_base64=PUBLIC_KEY)
```

### 方式3：环境变量

```python
import os
PUBLIC_KEY = os.getenv('MYAUTH_PUBLIC_KEY')
manager = LicenseManager(public_key_base64=PUBLIC_KEY)
```

---

## 🔍 调试技巧

### 1. 启用日志

```python
import logging
logging.basicConfig(level=logging.DEBUG)
```

### 2. 测试 License

```python
# 运行测试脚本
python test_license.py
```

### 3. 查看详细信息

```python
info = manager.get_license_info()
print(json.dumps(info, indent=2))
```

---

## ❓ 常见问题

### Q: 如何生成 License？

使用 Java 管理界面：http://localhost:3000

### Q: 如何延期？

重新生成一个新的 License，替换旧文件。

### Q: 试用模式？

不设置 License，应用会以试用模式运行（可配置）。

### Q: 机器码绑定？

生成 License 时填入客户的机器码即可。

---

## 📚 更多资源

- [完整文档](README_PYTHON_INTEGRATION.md)
- [加密方案对比](ENCRYPTION_GUIDE.md)
- [主项目 README](../README.md)

---

**需要帮助？** 查看完整文档或提交 Issue。
