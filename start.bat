@echo off
chcp 65001 >nul
echo ========================================
echo   MyAuth 授权管理系统 - 快速启动
echo ========================================
echo.

echo [1/3] 检查 MySQL 服务...
sc query MySQL80 | find "RUNNING" >nul
if errorlevel 1 (
    echo ❌ MySQL 服务未运行，请先启动 MySQL！
    pause
    exit /b 1
)
echo ✅ MySQL 服务运行正常
echo.

echo [2/3] 启动后端服务...
start "MyAuth Generator" cmd /k "cd myauth-generator && mvn spring-boot:run"
echo ⏳ 等待后端启动...
timeout /t 10 /nobreak >nul
echo ✅ 后端服务已启动
echo.

echo [3/3] 启动前端服务...
start "MyAuth Web" cmd /k "cd myauth-web && npm run dev"
echo ⏳ 等待前端启动...
timeout /t 5 /nobreak >nul
echo ✅ 前端服务已启动
echo.

echo ========================================
echo   启动完成！
echo ========================================
echo.
echo 📱 前端界面: http://localhost:3000
echo 📚 API文档: http://localhost:8080/doc.html
echo.
echo 按任意键退出...
pause >nul
