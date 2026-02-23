# 清除 Vite 缓存的 PowerShell 脚本

Write-Host "正在清除 Vite 缓存..." -ForegroundColor Yellow

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

# 清除 Vite 缓存
if (Test-Path "node_modules\.vite") {
    Write-Host "删除 node_modules\.vite 目录..." -ForegroundColor Cyan
    Remove-Item -Recurse -Force "node_modules\.vite" -ErrorAction SilentlyContinue
    Write-Host "Vite 缓存已清除" -ForegroundColor Green
} else {
    Write-Host "未找到 Vite 缓存目录" -ForegroundColor Gray
}

# 清除构建目录
if (Test-Path "dist") {
    Write-Host "删除 dist 目录..." -ForegroundColor Cyan
    Remove-Item -Recurse -Force "dist" -ErrorAction SilentlyContinue
    Write-Host "dist 目录已清除" -ForegroundColor Green
}

Write-Host ""
Write-Host "缓存清除完成！" -ForegroundColor Green
Write-Host "请重新运行 npm run dev 启动开发服务器" -ForegroundColor Yellow

