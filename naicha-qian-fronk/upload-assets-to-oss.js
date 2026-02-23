/**
 * 批量上传 assets 目录下的图片到阿里云 OSS
 * 
 * 使用方法：
 * node upload-assets-to-oss.js
 */

const fs = require('fs')
const path = require('path')

// 动态加载依赖
let FormData, axios
try {
  FormData = require('form-data')
  axios = require('axios')
} catch (error) {
  console.error('缺少依赖，正在安装...')
  console.error('请运行: npm install form-data axios --save-dev')
  process.exit(1)
}

// 配置
const ASSETS_DIR = path.join(__dirname, 'src/assets')
const API_BASE_URL = process.env.VITE_API_BASE_URL || 'http://localhost:8081'
const UPLOAD_FOLDER = 'product' // 上传到 images/product 目录

// 支持的图片格式
const IMAGE_EXTENSIONS = ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.bmp', '.svg']

/**
 * 获取所有图片文件
 */
function getAllImageFiles(dir) {
  const files = []
  
  try {
    const items = fs.readdirSync(dir)
    
    for (const item of items) {
      const fullPath = path.join(dir, item)
      const stat = fs.statSync(fullPath)
      
      if (stat.isFile()) {
        const ext = path.extname(item).toLowerCase()
        if (IMAGE_EXTENSIONS.includes(ext)) {
          files.push({
            name: item,
            path: fullPath,
            size: stat.size
          })
        }
      }
    }
  } catch (error) {
    console.error('读取目录失败:', error)
  }
  
  return files
}

/**
 * 上传单个文件到 OSS
 */
async function uploadFile(filePath, fileName, folder) {
  try {
    const formData = new FormData()
    const fileStream = fs.createReadStream(filePath)
    
    formData.append('file', fileStream, fileName)
    if (folder) {
      formData.append('folder', folder)
    }
    
    console.log(`正在上传: ${fileName} (${(fs.statSync(filePath).size / 1024).toFixed(2)} KB)...`)
    
    const response = await axios.post(
      `${API_BASE_URL}/api/upload/image`,
      formData,
      {
        headers: {
          ...formData.getHeaders(),
          // 如果需要认证，添加 token
          // 'Authorization': `Bearer ${token}`
        },
        timeout: 60000 // 60秒超时
      }
    )
    
    if (response.data && response.data.code === 200) {
      return response.data.data || response.data
    } else {
      throw new Error(response.data?.message || '上传失败')
    }
  } catch (error) {
    if (error.response) {
      throw new Error(`上传失败: ${error.response.status} - ${error.response.data?.message || error.message}`)
    } else {
      throw new Error(`上传失败: ${error.message}`)
    }
  }
}

/**
 * 批量上传文件
 */
async function uploadAllFiles() {
  console.log('开始批量上传图片到 OSS...\n')
  console.log(`资源目录: ${ASSETS_DIR}`)
  console.log(`上传目录: images/${UPLOAD_FOLDER}`)
  console.log(`API地址: ${API_BASE_URL}\n`)
  
  // 检查目录是否存在
  if (!fs.existsSync(ASSETS_DIR)) {
    console.error(`错误: 目录不存在 ${ASSETS_DIR}`)
    process.exit(1)
  }
  
  // 获取所有图片文件
  const imageFiles = getAllImageFiles(ASSETS_DIR)
  
  if (imageFiles.length === 0) {
    console.log('未找到图片文件')
    return
  }
  
  console.log(`找到 ${imageFiles.length} 个图片文件:\n`)
  imageFiles.forEach((file, index) => {
    console.log(`${index + 1}. ${file.name} (${(file.size / 1024).toFixed(2)} KB)`)
  })
  console.log()
  
  // 询问是否继续
  const readline = require('readline')
  const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
  })
  
  return new Promise((resolve) => {
    rl.question('是否开始上传？(y/n): ', async (answer) => {
      rl.close()
      
      if (answer.toLowerCase() !== 'y' && answer.toLowerCase() !== 'yes') {
        console.log('已取消上传')
        resolve()
        return
      }
      
      console.log('\n开始上传...\n')
      
      const results = {
        success: [],
        failed: []
      }
      
      // 逐个上传文件
      for (let i = 0; i < imageFiles.length; i++) {
        const file = imageFiles[i]
        try {
          const url = await uploadFile(file.path, file.name, UPLOAD_FOLDER)
          results.success.push({
            name: file.name,
            url: url
          })
          console.log(`✓ ${file.name} -> ${url}\n`)
          
          // 延迟一下，避免请求过快
          if (i < imageFiles.length - 1) {
            await new Promise(resolve => setTimeout(resolve, 500))
          }
        } catch (error) {
          results.failed.push({
            name: file.name,
            error: error.message
          })
          console.error(`✗ ${file.name} 上传失败: ${error.message}\n`)
        }
      }
      
      // 输出结果摘要
      console.log('\n========== 上传完成 ==========')
      console.log(`成功: ${results.success.length} 个`)
      console.log(`失败: ${results.failed.length} 个`)
      
      if (results.success.length > 0) {
        console.log('\n成功上传的文件:')
        results.success.forEach(item => {
          console.log(`  - ${item.name}`)
          console.log(`    ${item.url}`)
        })
      }
      
      if (results.failed.length > 0) {
        console.log('\n上传失败的文件:')
        results.failed.forEach(item => {
          console.log(`  - ${item.name}: ${item.error}`)
        })
      }
      
      // 生成结果文件
      const resultFile = path.join(__dirname, 'upload-results.json')
      fs.writeFileSync(resultFile, JSON.stringify(results, null, 2))
      console.log(`\n结果已保存到: ${resultFile}`)
      
      resolve()
    })
  })
}

// 运行上传
if (require.main === module) {
  uploadAllFiles().catch(error => {
    console.error('执行失败:', error)
    process.exit(1)
  })
}

module.exports = { uploadAllFiles, uploadFile }

