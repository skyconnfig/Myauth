@echo off
chcp 65001 >nul
echo ========================================
echo   MyAuth Client - 测试环境配置工具
echo ========================================
echo.

echo 此脚本将帮助你快速配置测试环境
echo.

REM 检查 license 目录是否存在
if not exist "license" (
    echo 创建 license 目录...
    mkdir license
)

echo.
echo 请按以下步骤操作：
echo.
echo 1. 启动授权管理系统（myauth-generator）
echo    cd myauth-generator
echo    mvn spring-boot:run
echo.
echo 2. 访问 http://localhost:8080/doc.html
echo.
echo 3. 在 API 文档中找到 "密钥管理" 接口
echo    - 调用 POST /api/license/generate-keypair 生成密钥对
echo    - 调用 GET /api/license/public-key 获取公钥
echo.
echo 4. 复制公钥内容，保存为 license/public.key 文件
echo.
echo 5. 在 API 文档中找到 "License管理" 接口
echo    - 调用 POST /api/license/generate 生成授权码
echo    - 填写客户信息、过期时间等
echo.
echo 6. 复制生成的 licenseKey，保存为 license/license.key 文件
echo.
echo 7. 重新启动客户端
echo    cd myauth-client
echo    mvn spring-boot:run
echo.

pause
