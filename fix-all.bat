@echo off
chcp 65001 >nul
echo ========================================
echo   MyAuth - 完整修复登录问题
echo ========================================
echo.

echo 此脚本将：
echo 1. 重新初始化数据库
echo 2. 清理并重新编译项目
echo 3. 启动后端服务
echo.

pause

echo.
echo [步骤1] 重新初始化数据库...
echo 请输入MySQL root密码：
set /p MYSQL_PASSWORD="密码: "

echo 正在删除旧数据库...
mysql -u root -p%MYSQL_PASSWORD% -e "DROP DATABASE IF EXISTS myauth;" 2>nul

echo 正在创建新数据库和表...
mysql -u root -p%MYSQL_PASSWORD% < myauth-generator\src\main\resources\db\init.sql
if errorlevel 1 (
    echo ❌ 数据库初始化失败
    pause
    exit /b 1
)
echo ✅ 数据库初始化成功
echo.

echo [步骤2] 清理并编译项目...
call mvn clean compile -DskipTests
if errorlevel 1 (
    echo ❌ 编译失败
    pause
    exit /b 1
)
echo ✅ 编译成功
echo.

echo ========================================
echo   准备启动后端服务...
echo ========================================
echo.
echo 请在新的终端窗口运行：
echo   cd myauth-generator
echo   mvn spring-boot:run
echo.
echo 然后访问 http://localhost:3000 登录
echo 用户名: admin
echo 密码: admin123
echo.

pause
