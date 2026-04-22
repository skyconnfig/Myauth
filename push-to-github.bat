@echo off
chcp 65001 >nul
echo ========================================
echo   MyAuth - 提交到 GitHub
echo ========================================
echo.

echo [步骤1] 初始化 Git 仓库...
git init
if errorlevel 1 (
    echo ❌ Git 初始化失败
    pause
    exit /b 1
)
echo ✅ Git 仓库已初始化
echo.

echo [步骤2] 添加所有文件...
git add .
if errorlevel 1 (
    echo ❌ 添加文件失败
    pause
    exit /b 1
)
echo ✅ 文件已添加到暂存区
echo.

echo [步骤3] 创建提交...
git commit -m "feat: 完成离线授权管理系统开发

- 后端：Spring Boot 3.1.5 + MyBatis-Plus 3.5.7 + MySQL
- 前端：Vue 3 + Vite + Ant Design Vue
- 功能：用户认证、License生成与验证、密钥管理
- 特性：RSA签名、JWT Token、BCrypt密码加密
- UI：左右布局、深色主题、响应式设计"
if errorlevel 1 (
    echo ⚠️  提交失败（可能没有变更）
) else (
    echo ✅ 提交成功
)
echo.

echo [步骤4] 添加远程仓库...
echo 请输入你的 GitHub 仓库地址：
echo 例如：https://github.com/yourusername/myauth.git
set /p REPO_URL="仓库地址: "

git remote add origin %REPO_URL%
if errorlevel 1 (
    echo ⚠️  远程仓库已存在，尝试更新...
    git remote set-url origin %REPO_URL%
)
echo ✅ 远程仓库已配置
echo.

echo [步骤5] 推送到 GitHub...
git branch -M main
git push -u origin main
if errorlevel 1 (
    echo ❌ 推送失败
    echo.
    echo 可能的原因：
    echo 1. 仓库地址不正确
    echo 2. 需要登录 GitHub
    echo 3. 远程仓库已有内容，需要先 pull
    echo.
    echo 解决方案：
    echo   git pull origin main --allow-unrelated-histories
    echo   git push -u origin main
) else (
    echo ✅ 推送成功！
    echo.
    echo 🎉 项目已成功提交到 GitHub！
)
echo.

pause
