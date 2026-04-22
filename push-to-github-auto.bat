@echo off
chcp 65001 >nul
echo ========================================
echo   MyAuth - 自动推送到 GitHub
echo ========================================
echo.

echo ⚠️  安全提示：建议使用 Git Credential Manager 或 SSH 密钥
echo    而不是在脚本中硬编码 Token
echo.

echo [步骤1] 检查 Git 是否安装...
git --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Git 未安装，请先安装 Git
    pause
    exit /b 1
)
echo ✅ Git 已安装
echo.

echo [步骤2] 初始化 Git 仓库...
if not exist .git (
    git init
    echo ✅ Git 仓库已初始化
) else (
    echo ℹ️  Git 仓库已存在
)
echo.

echo [步骤3] 配置 Git 用户信息...
git config user.name "MyAuth Developer"
git config user.email "developer@myauth.com"
echo ✅ Git 用户信息已配置
echo.

echo [步骤4] 添加所有文件...
git add .
echo ✅ 文件已添加到暂存区
echo.

echo [步骤5] 创建提交...
git commit -m "feat: 完成离线授权管理系统开发

- 后端：Spring Boot 3.1.5 + MyBatis-Plus 3.5.7 + MySQL
- 前端：Vue 3 + Vite + Ant Design Vue  
- 功能：用户认证、License生成与验证、密钥管理
- 特性：RSA签名、JWT Token、BCrypt密码加密
- UI：左右布局、深色主题、响应式设计"
if errorlevel 1 (
    echo ⚠️  没有需要提交的变更或提交失败
) else (
    echo ✅ 提交成功
)
echo.

echo [步骤6] 配置远程仓库...
echo.
echo 请选择操作：
echo 1. 使用 HTTPS + Token（需要输入 Token）
echo 2. 使用 SSH（推荐，需要先配置 SSH 密钥）
echo 3. 手动输入仓库地址
echo.
set /p CHOICE="请选择 (1/2/3): "

if "%CHOICE%"=="1" (
    echo.
    echo 请输入你的 GitHub 用户名：
    set /p GITHUB_USER="用户名: "
    echo.
    echo 请输入你的 GitHub Token（以 ghp_ 开头）：
    set /p GITHUB_TOKEN="Token: "
    echo.
    echo 请输入仓库名称（例如：myauth）：
    set /p REPO_NAME="仓库名: "
    
    set REPO_URL=https://%GITHUB_USER%:%GITHUB_TOKEN%@github.com/%GITHUB_USER%/%REPO_NAME%.git
    
    git remote remove origin 2>nul
    git remote add origin %REPO_URL%
    echo ✅ HTTPS 远程仓库已配置
    
) else if "%CHOICE%"=="2" (
    echo.
    echo 请输入你的 GitHub 用户名：
    set /p GITHUB_USER="用户名: "
    echo.
    echo 请输入仓库名称（例如：myauth）：
    set /p REPO_NAME="仓库名: "
    
    set REPO_URL=git@github.com:%GITHUB_USER%/%REPO_NAME%.git
    
    git remote remove origin 2>nul
    git remote add origin %REPO_URL%
    echo ✅ SSH 远程仓库已配置
    echo.
    echo ⚠️  确保你已在 GitHub 配置 SSH 密钥
    echo    查看教程：https://docs.github.com/en/authentication/connecting-to-github-with-ssh
    
) else if "%CHOICE%"=="3" (
    echo.
    echo 请输入完整的仓库地址：
    echo 例如：https://github.com/username/myauth.git
    set /p REPO_URL="仓库地址: "
    
    git remote remove origin 2>nul
    git remote add origin %REPO_URL%
    echo ✅ 远程仓库已配置
) else (
    echo ❌ 无效选择
    pause
    exit /b 1
)
echo.

echo [步骤7] 推送到 GitHub...
git branch -M main
git push -u origin main

if errorlevel 1 (
    echo.
    echo ❌ 推送失败
    echo.
    echo 可能的原因和解决方案：
    echo.
    echo 1. 远程仓库不存在
    echo    → 先在 GitHub 上创建空仓库
    echo.
    echo 2. Token 无效或过期
    echo    → 重新生成 Token：https://github.com/settings/tokens
    echo.
    echo 3. 远程仓库已有内容
    echo    → 执行：git pull origin main --allow-unrelated-histories
    echo    → 然后再次推送：git push -u origin main
    echo.
    echo 4. 权限不足
    echo    → 检查 Token 是否有 repo 权限
    echo.
) else (
    echo.
    echo ========================================
    echo   🎉 推送成功！
    echo ========================================
    echo.
    echo 你的项目已成功上传到 GitHub！
    echo.
)

pause
