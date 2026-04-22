# MyAuth Python 集成 - 文件说明

## 📁 文件结构

```
myauth-python/
├── license_validator.py          # 🔑 核心验证库（必需）
├── flask_app.py                  # 🌶️ Flask 集成示例
├── fastapi_app.py                # ⚡ FastAPI 集成示例
├── config_example.py             # ⚙️ 配置示例
├── test_license.py               # 🧪 测试脚本
├── requirements.txt              # 📦 依赖列表
├── start.bat                     # 🚀 Windows 快速启动
├── .gitignore                    # 🚫 Git 忽略规则
│
├── README_PYTHON_INTEGRATION.md  # 📖 完整集成指南
├── ENCRYPTION_GUIDE.md           # 🔐 加密方案对比
├── QUICK_REFERENCE.md            # 📝 快速参考
└── FILES_OVERVIEW.md             # 📋 本文件
```

---

## 📄 文件详解

### 核心文件

#### 1. `license_validator.py` ⭐⭐⭐⭐⭐
**用途：** 核心 License 验证库  
**包含：**
- `License` - License 数据实体
- `LicenseCodec` - 编解码工具
- `LicenseValidator` - 验证器
- `LicenseManager` - 管理器（推荐使用）

**使用：**
```python
from license_validator import LicenseManager
manager = LicenseManager(public_key_path="license/public.key")
manager.initialize()
```

---

### 示例文件

#### 2. `flask_app.py` ⭐⭐⭐⭐
**用途：** Flask 框架集成示例  
**功能：**
- 装饰器方式验证
- 试用模式支持
- 在线激活接口
- 定时检查任务

**运行：**
```bash
python flask_app.py
```

---

#### 3. `fastapi_app.py` ⭐⭐⭐⭐
**用途：** FastAPI 框架集成示例  
**功能：**
- 依赖注入方式验证
- 中间件支持
- 后台任务
- 自动文档（Swagger）

**运行：**
```bash
uvicorn fastapi_app:app --reload
```

---

### 配置文件

#### 4. `config_example.py` ⭐⭐⭐
**用途：** 配置模板  
**使用：**
```bash
cp config_example.py config.py
# 编辑 config.py
```

**配置项：**
- 公钥路径
- License 文件路径
- 运行模式（开发/生产）
- 检查间隔
- 日志配置

---

#### 5. `requirements.txt` ⭐⭐⭐
**用途：** Python 依赖列表  
**安装：**
```bash
pip install -r requirements.txt
```

**依赖：**
- `cryptography` - RSA 加密库（必需）
- `flask` - Flask 框架（可选）
- `fastapi` - FastAPI 框架（可选）
- `uvicorn` - ASGI 服务器（可选）

---

### 测试文件

#### 6. `test_license.py` ⭐⭐⭐
**用途：** 验证库测试脚本  
**运行：**
```bash
python test_license.py
```

**测试内容：**
- 编解码功能
- 机器码生成
- 剩余天数计算
- License 验证
- 错误处理

---

### 启动脚本

#### 7. `start.bat` ⭐⭐
**用途：** Windows 快速启动脚本  
**使用：** 双击运行  
**功能：**
- 自动检查依赖
- 选择框架（Flask/FastAPI）
- 启动服务

---

### 文档文件

#### 8. `README_PYTHON_INTEGRATION.md` ⭐⭐⭐⭐⭐
**用途：** 完整集成指南  
**内容：**
- 架构说明
- Flask 详细教程
- FastAPI 详细教程
- Vue 3 前端集成
- 安全建议
- 常见问题

**推荐阅读顺序：** 第 1 个阅读

---

#### 9. `ENCRYPTION_GUIDE.md` ⭐⭐⭐⭐
**用途：** 加密方案对比  
**内容：**
- MD5 vs HMAC-SHA256 vs RSA-SHA256
- 安全性分析
- 性能对比
- 最佳实践
- 代码示例

**推荐阅读：** 想了解为什么选择 RSA 时阅读

---

#### 10. `QUICK_REFERENCE.md` ⭐⭐⭐⭐
**用途：** 快速参考卡片  
**内容：**
- 5分钟快速开始
- 常用 API
- 代码片段
- 调试技巧
- 常见问题

**推荐使用：** 日常开发时查阅

---

#### 11. `FILES_OVERVIEW.md` ⭐⭐
**用途：** 本文件，文件说明  
**内容：** 所有文件的用途和使用方法

---

## 🎯 使用流程

### 新手入门

1. **阅读文档**
   ```
   README_PYTHON_INTEGRATION.md → 了解整体架构
   ```

2. **安装依赖**
   ```bash
   pip install -r requirements.txt
   ```

3. **运行测试**
   ```bash
   python test_license.py
   ```

4. **查看示例**
   ```bash
   # Flask
   python flask_app.py
   
   # FastAPI
   uvicorn fastapi_app:app --reload
   ```

5. **集成到项目**
   - 复制 `license_validator.py` 到你的项目
   - 按照示例代码集成
   - 配置公钥和 License 文件

---

### 快速查询

| 需求 | 查看文件 |
|------|----------|
| 如何安装？ | `requirements.txt` |
| 如何使用？ | `QUICK_REFERENCE.md` |
| Flask 示例 | `flask_app.py` |
| FastAPI 示例 | `fastapi_app.py` |
| 完整教程 | `README_PYTHON_INTEGRATION.md` |
| 为什么选 RSA？ | `ENCRYPTION_GUIDE.md` |
| 测试验证 | `test_license.py` |
| 配置说明 | `config_example.py` |

---

## 🔗 与其他模块的关系

```
myauth-generator (Java)
    ↓ 生成密钥对和 License
myauth-python (Python)
    ↓ 验证 License
你的 Python 应用
```

```
myauth-web (Vue 3)
    ↓ 前端管理界面
myauth-python (Python)
    ↓ 提供 API
你的 Python 应用
```

---

## 💡 提示

### 必须文件
- ✅ `license_validator.py` - 核心库
- ✅ `license/public.key` - 公钥文件（从 Java 项目复制）

### 可选文件
- 📝 示例文件（学习用）
- 📝 文档文件（参考用）
- 📝 测试文件（验证用）

### 不要提交到 Git
- ❌ `config.py` - 包含敏感信息
- ❌ `license/*.key` - License 文件
- ❌ `__pycache__/` - Python 缓存

---

## 📊 文件大小

| 文件 | 大小 | 类型 |
|------|------|------|
| `license_validator.py` | ~10KB | 核心库 |
| `flask_app.py` | ~8KB | 示例 |
| `fastapi_app.py` | ~9KB | 示例 |
| `README_PYTHON_INTEGRATION.md` | ~15KB | 文档 |
| `ENCRYPTION_GUIDE.md` | ~12KB | 文档 |
| `QUICK_REFERENCE.md` | ~6KB | 文档 |
| 其他 | <5KB | 辅助 |

**总计：** ~65KB（非常轻量！）

---

## 🎓 学习路径

### Level 1: 基础（30分钟）
1. 阅读 `QUICK_REFERENCE.md`
2. 运行 `test_license.py`
3. 启动 `flask_app.py`

### Level 2: 进阶（2小时）
1. 阅读 `README_PYTHON_INTEGRATION.md`
2. 理解 `license_validator.py` 源码
3. 修改示例代码适应你的需求

### Level 3: 精通（1天）
1. 阅读 `ENCRYPTION_GUIDE.md`
2. 自定义验证逻辑
3. 集成到你的项目
4. 添加监控和日志

---

## 🆘 获取帮助

1. **查看文档** - 90% 的问题都能在文档中找到答案
2. **运行测试** - 确认环境配置正确
3. **查看示例** - 参考示例代码
4. **提交 Issue** - 描述清楚问题和复现步骤

---

## 📝 更新日志

### v1.0.0 (2026-04-22)
- ✅ 初始版本发布
- ✅ 支持 Flask 和 FastAPI
- ✅ 完整的文档和示例
- ✅ Vue 3 前端集成
- ✅ RSA-SHA256 加密

---

**祝使用愉快！** 🎉

如有问题，请查看文档或联系开发团队。
