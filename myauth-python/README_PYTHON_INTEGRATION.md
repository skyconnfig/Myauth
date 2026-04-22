# MyAuth Python 集成指南

本指南介绍如何将 MyAuth 离线授权系统集成到 Python (Flask/FastAPI) 和 Vue 3 项目中。

---

## 📋 目录

- [架构说明](#架构说明)
- [Python 集成](#python-集成)
  - [安装依赖](#安装依赖)
  - [Flask 集成](#flask-集成)
  - [FastAPI 集成](#fastapi-集成)
- [Vue 3 前端集成](#vue-3-前端集成)
- [安全建议](#安全建议)
- [常见问题](#常见问题)

---

## 架构说明

### 加密方式对比

| 方式 | 安全性 | 适用场景 | 说明 |
|------|--------|----------|------|
| **RSA-SHA256**（推荐） | ⭐⭐⭐⭐⭐ | 生产环境 | 非对称加密，无法破解，支持离线验证 |
| MD5 | ⭐ | 不推荐 | 可被碰撞攻击，不适合授权验证 |
| AES | ⭐⭐⭐ | 内部系统 | 对称加密，密钥管理复杂 |

**强烈建议使用 RSA-SHA256**，因为：
- ✅ 无法逆向推导私钥
- ✅ 支持离线验证（不需要联网）
- ✅ 防篡改（签名验证）
- ✅ 行业标准

---

## Python 集成

### 安装依赖

```bash
pip install cryptography
```

对于 Flask：
```bash
pip install flask cryptography
```

对于 FastAPI：
```bash
pip install fastapi uvicorn cryptography pydantic
```

---

### Flask 集成

#### 1. 基本使用

```python
from license_validator import LicenseManager

# 初始化（应用启动时）
manager = LicenseManager(public_key_path="license/public.key")
license_info = manager.initialize()

print(f"客户: {license_info.customer_name}")
print(f"类型: {license_info.type}")
print(f"过期时间: {license_info.expire_time}")
```

#### 2. 使用装饰器保护路由

```python
from flask import Flask, g
from license_validator import LicenseManager

app = Flask(__name__)
license_manager = LicenseManager(public_key_path="license/public.key")
license_manager.initialize()

@app.route('/api/protected')
@require_license  # 需要有效 License
def protected_route():
    return {"data": "受保护的数据"}

@app.route('/api/premium')
@require_license
def premium_feature():
    # 检查授权类型
    if g.license_info['type'] not in ['professional', 'enterprise']:
        return {"error": "需要专业版授权"}, 403
    return {"data": "高级功能"}
```

#### 3. 试用模式

```python
@app.route('/api/basic')
@check_trial_mode  # 允许试用
def basic_feature():
    if g.license_info['status'] == 'trial':
        # 试用模式限制
        return {"data": "基础功能（试用）", "limit": "最多5个用户"}
    return {"data": "完整功能"}
```

#### 4. 在线激活

```python
@app.route('/api/activate', methods=['POST'])
def activate():
    license_key = request.json.get('license_key')
    try:
        license_obj = license_manager.initialize(license_key)
        return {"message": "激活成功", "info": license_obj}
    except Exception as e:
        return {"error": str(e)}, 400
```

---

### FastAPI 集成

#### 1. 基本使用

```python
from fastapi import FastAPI, Depends
from license_validator import LicenseManager

app = FastAPI()

# 全局管理器
license_manager = LicenseManager(public_key_path="license/public.key")

@app.on_event("startup")
async def startup():
    license_manager.initialize()
```

#### 2. 使用依赖注入

```python
from fastapi import HTTPException

async def verify_license() -> dict:
    """验证 License 依赖"""
    if not license_manager.check_license():
        raise HTTPException(status_code=403, detail="License 无效")
    return license_manager.get_license_info()

@app.get("/api/protected")
async def protected_route(license_info: dict = Depends(verify_license)):
    return {"data": "受保护的数据", "license": license_info}
```

#### 3. 检查授权类型

```python
async def verify_premium(license_info: dict = Depends(verify_license)):
    """验证是否为专业版"""
    if license_info['type'] not in ['professional', 'enterprise']:
        raise HTTPException(status_code=403, detail="需要专业版授权")
    return license_info

@app.get("/api/premium")
async def premium_feature(license_info: dict = Depends(verify_premium)):
    return {"data": "高级功能"}
```

#### 4. 中间件方式

```python
@app.middleware("http")
async def license_middleware(request: Request, call_next):
    # 排除公开接口
    if request.url.path not in ["/", "/docs", "/api/status"]:
        if not license_manager.check_license():
            return JSONResponse(
                status_code=403,
                content={"detail": "License 无效"}
            )
    
    response = await call_next(request)
    return response
```

---

## Vue 3 前端集成

### 1. 安装插件

在 `main.js` 中注册：

```javascript
import { createApp } from 'vue'
import App from './App.vue'
import licensePlugin from '@/utils/license'

const app = createApp(App)

// 注册 License 插件
app.use(licensePlugin, {
  API_BASE_URL: 'http://localhost:8080'
})

app.mount('#app')
```

### 2. 在组件中使用

#### 组合式 API

```vue
<script setup>
import { inject, onMounted } from 'vue'
import { licenseValidator } from '@/utils/license'

const license = inject('license')

onMounted(async () => {
  // 初始化验证
  const isValid = await license.initialize()
  
  if (isValid) {
    console.log('License 有效')
  } else {
    console.log('试用模式')
  }
})

// 检查权限
const canUseFeature = () => {
  return license.hasModulePermission('advanced')
}
</script>
```

#### 选项式 API

```vue
<script>
export default {
  mounted() {
    this.$license.initialize()
  },
  methods: {
    checkPermission() {
      return this.$license.isValid()
    }
  }
}
</script>
```

### 3. 使用指令

```vue
<template>
  <!-- 只有有效 License 才显示 -->
  <div v-license>
    这是受保护的内容
  </div>
  
  <!-- 只有专业版才显示 -->
  <div v-license="'professional'">
    这是专业版功能
  </div>
</template>
```

### 4. 激活页面

参考 `LicenseActivation.vue` 组件，提供用户界面用于输入和激活 License。

### 5. 事件监听

```javascript
// 监听 License 激活
window.addEventListener('license:activated', (event) => {
  console.log('License 已激活:', event.detail)
})

// 监听验证失败
window.addEventListener('license:error', (event) => {
  console.error('验证失败:', event.detail.message)
})
```

---

## 安全建议

### 🔒 最佳实践

1. **不要在前端进行核心验证**
   - ❌ 错误：仅在前端验证 License
   - ✅ 正确：后端验证为主，前端仅做用户体验优化

2. **保护公钥文件**
   - 将公钥文件放在服务器安全目录
   - 不要将公钥硬编码在前端代码中

3. **定期验证**
   - 应用启动时验证
   - 定时检查（每小时）
   - 关键操作前验证

4. **机器码绑定**
   - 生成 License 时绑定机器码
   - 防止 License 被复制到其他设备

5. **日志记录**
   - 记录所有验证尝试
   - 监控异常行为

### ⚠️ 关于 MD5

**不建议使用 MD5 进行授权验证**，原因：

1. ❌ **可被破解**：MD5 碰撞攻击已被证明可行
2. ❌ **可被逆向**：如果算法泄露，可以生成假 License
3. ❌ **无签名机制**：无法验证数据来源的真实性

**如果必须使用简单加密**，可以考虑：

```python
import hashlib
import hmac

# 使用 HMAC-SHA256（比 MD5 安全得多）
secret_key = "your-secret-key"
message = "expire_time=2026-12-31&type=trial"
signature = hmac.new(
    secret_key.encode(),
    message.encode(),
    hashlib.sha256
).hexdigest()

license_key = f"{message}|{signature}"
```

但这种方式仍然是**对称加密**，密钥一旦泄露就无法保证安全。

---

## 从 Java 生成器获取公钥

### 方法 1：从文件读取

Java 生成的公钥文件位置：
```
myauth-generator/license/public.key
```

复制到 Python 项目：
```bash
cp myauth-generator/license/public.key myauth-python/license/
```

### 方法 2：使用 Base64 字符串

从 Java 管理界面复制公钥（Base64 格式），然后在 Python 中使用：

```python
PUBLIC_KEY_BASE64 = """
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...
"""

manager = LicenseManager(public_key_base64=PUBLIC_KEY_BASE64)
```

---

## 常见问题

### Q1: 如何生成测试 License？

使用 Java 管理界面生成，或通过 API：

```bash
curl -X POST http://localhost:8080/api/license/generate \
  -H "Content-Type: application/json" \
  -d '{
    "expireTime": "2026-12-31",
    "type": "trial",
    "customerName": "测试客户"
  }'
```

### Q2: 如何实现试用期？

在生成 License 时设置较短的过期时间：

```python
# 30天试用
from datetime import datetime, timedelta
expire_date = (datetime.now() + timedelta(days=30)).strftime('%Y-%m-%d')

# 调用 Java API 生成试用 License
```

### Q3: 如何延期 License？

重新生成一个新的 License，覆盖旧文件：

```python
# 保存新 License
with open('license/license.key', 'w') as f:
    f.write(new_license_key)

# 重新初始化
license_manager.initialize()
```

### Q4: 如何处理离线环境？

RSA 验证本身就是离线的，只需要：
1. 将公钥文件打包到应用中
2. 将 License 文件放到指定位置
3. 无需联网即可验证

### Q5: 性能影响？

RSA 验证非常快（毫秒级），对性能影响可忽略：
- 启动时验证：1次
- 定时检查：每小时1次
- 建议缓存验证结果

---

## 示例项目结构

```
myauth-python/
├── license_validator.py      # 核心验证库
├── flask_app.py              # Flask 示例
├── fastapi_app.py            # FastAPI 示例
├── license/
│   ├── public.key            # 公钥文件
│   └── license.key           # License 文件
├── requirements.txt          # 依赖列表
└── README.md                 # 本文档
```

---

## 快速开始

### Flask

```bash
cd myauth-python
pip install flask cryptography
python flask_app.py
```

访问：http://localhost:5000

### FastAPI

```bash
cd myauth-python
pip install fastapi uvicorn cryptography pydantic
uvicorn fastapi_app:app --reload --port 8000
```

访问：http://localhost:8000

### Vue 3

```bash
cd myauth-web
npm install
npm run dev
```

访问：http://localhost:3000

---

## 技术支持

如有问题，请查看：
- [主项目 README](../README.md)
- [架构文档](../ARCHITECTURE.md)
- [快速开始](../QUICKSTART.md)

---

**祝使用愉快！** 🎉
