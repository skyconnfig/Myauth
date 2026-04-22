# MyAuth - 离线授权管理系统

## 📋 项目简介

MyAuth 是一个完整的离线授权管理系统，采用 RSA 非对称加密技术，支持完全离线的授权验证。

### 技术栈

**后端：**
- Spring Boot 3.2.0 (Java)
- Flask / FastAPI (Python)
- MySQL 8.0
- MyBatis-Plus 3.5.5
- JWT (jjwt)
- Knife4j (Swagger API 文档)
- Hutool 工具库
- cryptography (Python 加密库)

**前端：**
- Vue 3
- Vite 5
- Ant Design Vue 4
- Pinia 状态管理
- Axios

---

## 🏗️ 项目结构

```
myauth/
├── myauth-common          # 公共模块（实体类、工具类）- Java
├── myauth-generator       # 授权生成器（管理端后端）- Java
├── myauth-client          # 授权验证客户端（示例）- Java
├── myauth-python          # Python 集成库（Flask/FastAPI）
├── myauth-web             # 前端管理界面 - Vue 3
└── pom.xml                # 父工程
```

---

## 🚀 快速开始

### 1. 数据库初始化

执行 SQL 脚本创建数据库和表：

```bash
mysql -u root -p < myauth-generator/src/main/resources/db/init.sql
```

或手动执行 `init.sql` 文件中的 SQL 语句。

### 2. 修改配置

编辑 `myauth-generator/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/myauth
    username: root
    password: your_password  # 修改为你的数据库密码
```

### 3. 编译项目

```bash
cd myauth
mvn clean install
```

### 4. 启动后端服务

```bash
cd myauth-generator
mvn spring-boot:run
```

访问 API 文档：http://localhost:8080/doc.html

### 5. 启动前端服务

```bash
cd myauth-web
npm install
npm run dev
```

访问前端界面：http://localhost:3000

---

## 💡 核心功能

### 1. 密钥对管理

- 生成 RSA 2048 位密钥对
- 私钥用于签名授权码
- 公钥用于客户端验证

### 2. 授权码生成

支持以下配置：
- ✅ 过期时间控制
- ✅ 设备绑定（机器码）
- ✅ 用户数限制
- ✅ 功能模块控制
- ✅ 客户信息管理

### 3. 授权验证

客户端验证流程：
1. 读取授权文件 `license/license.key`
2. 使用公钥验证签名
3. 检查过期时间
4. 验证设备绑定（可选）
5. 定时验证（每小时）

### 4. 授权管理

- 查看所有授权记录
- 启用/禁用授权
- 延期授权（重新生成）
- 删除授权
- 搜索和分页

---

## 🔐 安全特性

### RSA 签名机制

授权码结构：
```
Base64( JSON数据 + "." + RSA签名 )
```

### 防破解措施

1. **签名验证**：防止伪造授权码
2. **时间校验**：防止修改系统时间
3. **设备绑定**：防止复制到其他设备
4. **定时验证**：每小时自动验证一次
5. **代码混淆**：建议使用 ProGuard 或 Allatori

---

## 📖 使用示例

### 生成授权码

1. 访问前端界面 http://localhost:3000
2. 进入"生成授权"页面
3. 填写客户信息、授权类型、过期时间等
4. 点击"生成授权"
5. 复制生成的授权码

### 客户端集成

#### 1. 添加依赖

在你的项目中添加 `myauth-client` 依赖：

```xml
<dependency>
    <groupId>com.myauth</groupId>
    <artifactId>myauth-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### 2. 配置公钥

在 `application.yml` 中配置：

```yaml
license:
  public-key: "你的公钥Base64字符串"
  license-file: license/license.key
```

#### 3. 放置授权文件

将授权码保存为 `license/license.key` 文件。

#### 4. 启动应用

应用启动时会自动验证授权，如果验证失败会退出。

### Python 客户端集成

MyAuth 现在支持 Python 项目（Flask/FastAPI）！

#### 1. 安装依赖

```bash
cd myauth-python
pip install -r requirements.txt
```

#### 2. Flask 集成

```python
from license_validator import LicenseManager

# 初始化
manager = LicenseManager(public_key_path="license/public.key")
manager.initialize()

# 使用装饰器保护路由
@app.route('/api/protected')
@require_license
def protected_route():
    return {"data": "受保护的数据"}
```

#### 3. FastAPI 集成

```python
from fastapi import Depends

async def verify_license() -> dict:
    if not license_manager.check_license():
        raise HTTPException(status_code=403, detail="License 无效")
    return license_manager.get_license_info()

@app.get("/api/protected")
async def protected_route(license_info: dict = Depends(verify_license)):
    return {"data": "受保护的数据"}
```

详细文档请查看：[Python 集成指南](myauth-python/README_PYTHON_INTEGRATION.md)

---

## 🛠️ API 接口

### 授权管理接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/license/generate-keypair` | POST | 生成密钥对 |
| `/api/license/public-key` | GET | 获取公钥 |
| `/api/license/generate` | POST | 生成授权 |
| `/api/license/page` | GET | 分页查询 |
| `/api/license/list` | GET | 获取所有授权 |
| `/api/license/{id}` | GET | 根据ID查询 |
| `/api/license/{id}/status` | PUT | 更新状态 |
| `/api/license/{id}` | DELETE | 删除授权 |
| `/api/license/{id}/regenerate` | POST | 重新生成（延期） |

完整 API 文档：http://localhost:8080/doc.html

---

## ⚙️ 高级配置

### 自定义验证逻辑

在 `LicenseManager` 中添加自定义验证：

```java
// 检查功能模块
if (license.getModules() != null) {
    // 验证模块权限
}

// 检查用户数
if (currentUserCount > license.getMaxUsers()) {
    throw new RuntimeException("超出用户数限制");
}
```

### 多点验证

在关键业务逻辑中添加验证：

```java
@Autowired
private LicenseManager licenseManager;

public void businessMethod() {
    if (!licenseManager.isValidated()) {
        throw new RuntimeException("系统未授权");
    }
    // 业务逻辑
}
```

---

## 📝 注意事项

1. **私钥安全**：私钥必须妥善保管，不要泄露给客户端
2. **公钥分发**：公钥可以公开，用于客户端验证
3. **授权文件**：建议将 `license.key` 文件放在不易被篡改的位置
4. **时间同步**：确保服务器时间准确，建议使用 NTP 同步
5. **备份**：定期备份数据库和密钥文件

---

## 🔧 常见问题

### Q1: 授权验证失败？

检查：
- 公钥是否正确配置
- 授权文件格式是否正确
- 系统时间是否正确
- 机器码是否匹配（如果绑定了设备）

### Q2: 如何延期授权？

两种方式：
1. 在前端界面找到对应授权，点击"延期"按钮
2. 调用 API：`POST /api/license/{id}/regenerate`

### Q3: 如何绑定设备？

生成授权时填写客户的机器码：
1. 客户运行 `MachineUtil.getMachineId()` 获取机器码
2. 生成授权时填入该机器码
3. 授权将只能在该设备上使用

---

## 📄 License

MIT License

---

## 👥 联系方式

如有问题，请提交 Issue 或联系开发团队。

---

**祝使用愉快！** 🎉
