@echo off
chcp 65001 >nul
echo ========================================
echo 批量上传图片到阿里云 OSS
echo ========================================
echo.

cd /d "%~dp0"

REM 检查 Node.js 是否可用
where node >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到 Node.js，请先安装 Node.js
    pause
    exit /b 1
)

REM 检查是否安装了 form-data
echo 检查依赖...
call npm list form-data >nul 2>&1
if %errorlevel% neq 0 (
    echo 正在安装 form-data...
    call npm install form-data --save-dev
)

REM 运行上传脚本
echo.
echo 开始上传...
node upload-assets-to-oss.js

pause

