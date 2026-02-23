@echo off
echo 正在清除 Vite 缓存...
cd /d "%~dp0"

if exist node_modules\.vite (
    echo 删除 node_modules\.vite 目录...
    rmdir /s /q node_modules\.vite
    echo Vite 缓存已清除
) else (
    echo 未找到 Vite 缓存目录
)

if exist dist (
    echo 删除 dist 目录...
    rmdir /s /q dist
    echo dist 目录已清除
)

echo.
echo 缓存清除完成！
echo 请重新运行 npm run dev 启动开发服务器
pause

