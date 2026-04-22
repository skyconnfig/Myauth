# 🚀 MyAuth 快速开始指南

## 前置要求

- ✅ JDK 17+
- ✅ Maven 3.6+
- ✅ Node.js 18+
- ✅ MySQL 8.0+

---

## 第一步：初始化数据库

### Windows 用户

```bash
mysql -u root -p < myauth-generator\src\main\resources\db\init.sql
```

### Linux/Mac 用户

```bash
mysql -u root -p < myauth-generator/src/main/resources/db/init.sql
```

或者手动执行 SQL 文件中的内容。

---

## 第二步：修改配置

编辑 `myauth-generator/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    username: root
    password: your_password  # 👈 改为你的 MySQL 密码
```

---

## 第三步：编译项目

在项目根目录执行：

```bash
mvn clean install
```

这可能需要几分钟时间，取决于网络速度。

---

## 第四步：启动服务

### 方式一：使用启动脚本（推荐）

双击运行 `start.bat`（Windows）

### 方式二：手动启动

#### 1. 启动后端

```bash
cd myauth-generator
mvn spring-boot:run
```

等待看到以下信息表示启动成功：
```
===========================================
   MyAuth 授权管理系统启动成功！
   API文档: http://localhost:8080/doc.html
===========================================
```

#### 2. 启动前端（新终端）

```bash
cd myauth-web
npm install  # 首次需要安装依赖
npm run dev
```

等待看到以下信息：
```
VITE v5.x.x  ready in xxx ms

➜  Local:   http://localhost:3000/
```

---

## 第五步：访问系统

### Web 管理界面

打开浏览器访问：**http://localhost:3000**

### API 文档

访问：**http://localhost:8080/doc.html**

---

## 第六步：生成第一个授权

### 1. 生成密钥对

1. 访问 Web 界面
2. 点击左侧菜单 "密钥管理"
3. 点击 "生成密钥对" 按钮
4. 确认生成

### 2. 获取公钥

1. 在 "密钥管理" 页面
2. 点击 "获取公钥"
3. 复制公钥内容（后续客户端需要使用）

### 3. 生成授权码

1. 点击左侧菜单 "生成授权"
2. 填写表单：
   - 客户名称：测试客户
   - 授权类型：专业版
   - 过期时间：选择一年后的日期
   - 最大用户数：100
   - 备注：测试授权
3. 点击 "生成授权"
4. 复制生成的授权码

---

## 第七步：测试客户端验证

### 1. 创建授权文件

在 `myauth-client` 目录下创建 `license` 文件夹：

```bash
mkdir license
```

将刚才复制的授权码保存为 `license/license.key` 文件。

### 2. 配置公钥

编辑 `myauth-client/src/main/resources/application.yml`：

```yaml
license:
  public-key: "粘贴你刚才复制的公钥"  # 👈 粘贴公钥
  license-file: license/license.key
```

### 3. 启动客户端

```bash
cd myauth-client
mvn spring-boot:run
```

如果授权有效，你会看到：
```
===========================================
   License验证成功！
   客户名称: 测试客户
   授权类型: professional
   过期时间: 2027-04-21
   剩余天数: 365
   最大用户数: 100
===========================================
```

### 4. 测试接口

访问：**http://localhost:8081/api/test**

应该返回：`系统运行正常，已授权！`

查看授权信息：**http://localhost:8081/api/license-info**

---

## 🎉 恭喜！

你已经成功搭建并运行了 MyAuth 授权管理系统！

---

## 📚 下一步

- 📖 阅读 [README.md](README.md) 了解详细功能
- 🔐 学习 [DATABASE.md](DATABASE.md) 了解数据库结构
- 🛠️ 查看 [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) 了解项目架构

---

## ❓ 遇到问题？

### 问题1：端口被占用

**解决**：修改配置文件中的端口号

```yaml
# 后端
server:
  port: 8080  # 改为其他端口

# 前端
# vite.config.js
server:
  port: 3000  # 改为其他端口
```

### 问题2：数据库连接失败

**检查**：
- MySQL 是否启动
- 用户名密码是否正确
- 数据库是否创建

### 问题3：前端无法访问后端

**检查**：
- 后端是否正常启动
- 代理配置是否正确（vite.config.js）

### 问题4：授权验证失败

**检查**：
- 公钥是否正确配置
- 授权文件格式是否正确
- 系统时间是否正确

---

## 💡 提示

1. **首次启动较慢**，因为需要下载依赖
2. **确保 MySQL 正在运行**
3. **建议使用 Chrome 或 Edge 浏览器**
4. **API 文档非常有用**，可以测试所有接口

---

**祝你使用愉快！** 🎊
