# 批量上传 assets 目录下的图片到阿里云 OSS
# 使用方法: .\upload-assets-to-oss.ps1

$ErrorActionPreference = "Stop"

# 配置
$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$assetsDir = Join-Path $scriptRoot "src\assets"
$apiBaseUrl = $env:VITE_API_BASE_URL
if (-not $apiBaseUrl) {
    $apiBaseUrl = "http://localhost:8081"
}
$uploadFolder = "product"  # 上传到 images/product 目录

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "批量上传图片到阿里云 OSS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "资源目录: $assetsDir" -ForegroundColor Yellow
Write-Host "上传目录: images/$uploadFolder" -ForegroundColor Yellow
Write-Host "API地址: $apiBaseUrl" -ForegroundColor Yellow
Write-Host ""

# 检查目录是否存在
if (-not (Test-Path $assetsDir)) {
    Write-Host "错误: 目录不存在 $assetsDir" -ForegroundColor Red
    exit 1
}

# 获取所有图片文件
$imageExtensions = @('.jpg', '.jpeg', '.png', '.gif', '.webp', '.bmp', '.svg')
$imageFiles = Get-ChildItem -Path $assetsDir -File | Where-Object {
    $imageExtensions -contains $_.Extension.ToLower()
}

if ($imageFiles.Count -eq 0) {
    Write-Host "未找到图片文件" -ForegroundColor Yellow
    exit 0
}

Write-Host "找到 $($imageFiles.Count) 个图片文件:" -ForegroundColor Green
$imageFiles | ForEach-Object { 
    $sizeKB = [math]::Round($_.Length / 1KB, 2)
    Write-Host "  - $($_.Name) ($sizeKB KB)" 
}
Write-Host ""

# 确认是否继续
$confirm = Read-Host "是否开始上传？(y/n)"
if ($confirm -ne 'y' -and $confirm -ne 'Y') {
    Write-Host "已取消上传" -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "开始上传..." -ForegroundColor Green
Write-Host ""

$results = @{
    success = @()
    failed = @()
}

# 逐个上传文件
foreach ($file in $imageFiles) {
    try {
        $filePath = $file.FullName
        $fileName = $file.Name
        $sizeKB = [math]::Round($file.Length / 1KB, 2)
        
        Write-Host "正在上传: $fileName ($sizeKB KB)..." -ForegroundColor Cyan
        
        # 使用 PowerShell 的 Invoke-RestMethod 上传
        $uri = "$apiBaseUrl/api/upload/image"
        $form = @{
            file = Get-Item $filePath
            folder = $uploadFolder
        }
        
        try {
            $response = Invoke-RestMethod -Uri $uri -Method Post -Form $form -TimeoutSec 60
            
            if ($response.code -eq 200) {
                $url = $response.data
                $results.success += @{
                    name = $fileName
                    url = $url
                }
                Write-Host "✓ $fileName -> $url" -ForegroundColor Green
                Write-Host ""
            } else {
                throw "上传失败: $($response.message)"
            }
        } catch {
            throw "请求失败: $($_.Exception.Message)"
        }
        
        # 延迟一下，避免请求过快
        Start-Sleep -Milliseconds 500
        
    } catch {
        $errorMsg = $_.Exception.Message
        $results.failed += @{
            name = $file.Name
            error = $errorMsg
        }
        Write-Host "✗ $($file.Name) 上传失败: $errorMsg" -ForegroundColor Red
        Write-Host ""
    }
}

# 输出结果摘要
Write-Host ""
Write-Host "========== 上传完成 ==========" -ForegroundColor Cyan
Write-Host "成功: $($results.success.Count) 个" -ForegroundColor Green
Write-Host "失败: $($results.failed.Count) 个" -ForegroundColor $(if ($results.failed.Count -gt 0) { "Red" } else { "Green" })

if ($results.success.Count -gt 0) {
    Write-Host ""
    Write-Host "成功上传的文件:" -ForegroundColor Green
    foreach ($item in $results.success) {
        Write-Host "  - $($item.name)" -ForegroundColor White
        Write-Host "    $($item.url)" -ForegroundColor Gray
    }
}

if ($results.failed.Count -gt 0) {
    Write-Host ""
    Write-Host "上传失败的文件:" -ForegroundColor Red
    foreach ($item in $results.failed) {
        Write-Host "  - $($item.name): $($item.error)" -ForegroundColor Yellow
    }
}

# 保存结果到文件
$resultFile = Join-Path $PSScriptRoot "upload-results.json"
$results | ConvertTo-Json -Depth 10 | Out-File -FilePath $resultFile -Encoding UTF8
Write-Host ""
Write-Host "结果已保存到: $resultFile" -ForegroundColor Cyan

