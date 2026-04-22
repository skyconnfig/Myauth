@echo off
chcp 65001 >nul
echo ========================================
echo   MyAuth - 登录问题修复工具
echo ========================================
echo.

echo 此脚本将帮助你修复登录500错误
echo.

echo [步骤1] 检查MySQL是否运行...
sc query MySQL80 | find "RUNNING" >nul
if errorlevel 1 (
    echo ❌ MySQL未运行，请先启动MySQL服务
    pause
    exit /b 1
)
echo ✅ MySQL运行正常
echo.

echo [步骤2] 重新初始化数据库...
echo 请输入MySQL root密码：
set /p MYSQL_PASSWORD="密码: "

echo 正在删除旧数据库...
mysql -u root -p%MYSQL_PASSWORD% -e "DROP DATABASE IF EXISTS myauth;" 2>nul
if errorlevel 1 (
    echo ❌ 删除数据库失败，请检查密码是否正确
    pause
    exit /b 1
)

echo 正在创建新数据库...
mysql -u root -p%MYSQL_PASSWORD% < myauth-generator\src\main\resources\db\init.sql
if errorlevel 1 (
    echo ❌ 初始化数据库失败
    pause
    exit /b 1
)

echo ✅ 数据库初始化成功
echo.

echo [步骤3] 生成正确的密码哈希...
cd myauth-generator
call mvn compile exec:java -Dexec.mainClass="com.myauth.generator.PasswordEncoderTest" -q
cd ..
echo.

echo ========================================
echo   修复完成！
echo ========================================
echo.
echo 默认账号信息：
echo   用户名: admin
echo   密码: admin123
echo.
echo 下一步：
echo   1. 启动后端: cd myauth-generator ^&^& mvn spring-boot:run
echo   2. 启动前端: cd myauth-web ^&^& npm run dev
echo   3. 访问: http://localhost:3000
echo.

pause
