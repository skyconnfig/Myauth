@echo off
chcp 65001 >nul
title kiftd - MyAuth License Agent

echo ===========================================
echo   kiftd 网络文件管理系统
echo   MyAuth License Agent 启动脚本
echo ===========================================
echo.
echo  授权到期日: 2027-05-30
echo  授权目录: %~dp0license
echo.

rem 设置Java参数
set JAVA_OPTS=-Xms256m -Xmx1024m
set AGENT_OPTS=-javaagent:%~dp0myauth-agent.jar -Dagent.license.dir=%~dp0license

rem 启动kiftd
echo 正在启动 kiftd...
java %JAVA_OPTS% %AGENT_OPTS% -jar %~dp0kiftd-1.1.0-RELEASE.jar

if %ERRORLEVEL% neq 0 (
    echo.
    echo 程序已退出，错误代码: %ERRORLEVEL%
    pause
)
