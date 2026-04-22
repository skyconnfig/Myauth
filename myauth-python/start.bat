@echo off
chcp 65001 >nul
echo ========================================
echo   MyAuth Python 集成 - 快速启动
echo ========================================
echo.

REM 检查 Python
python --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 Python，请先安装 Python 3.8+
    pause
    exit /b 1
)

echo [1/4] 检查依赖...
pip show cryptography >nul 2>&1
if errorlevel 1 (
    echo [提示] 正在安装依赖...
    pip install -r requirements.txt
    if errorlevel 1 (
        echo [错误] 依赖安装失败
        pause
        exit /b 1
    )
) else (
    echo [成功] 依赖已安装
)

echo.
echo [2/4] 选择框架:
echo   1. Flask
echo   2. FastAPI
echo   3. 退出
echo.
set /p choice="请选择 (1/2/3): "

if "%choice%"=="1" goto flask
if "%choice%"=="2" goto fastapi
if "%choice%"=="3" goto end
echo [错误] 无效选择
pause
exit /b 1

:flask
echo.
echo [3/4] 启动 Flask 应用...
echo [提示] 访问 http://localhost:5000
echo [提示] 按 Ctrl+C 停止服务
echo.
python flask_app.py
goto end

:fastapi
echo.
echo [3/4] 启动 FastAPI 应用...
echo [提示] 访问 http://localhost:8000
echo [提示] 按 Ctrl+C 停止服务
echo.
uvicorn fastapi_app:app --reload --host 0.0.0.0 --port 8000
goto end

:end
echo.
echo ========================================
echo   服务已停止
echo ========================================
pause
