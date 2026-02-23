<template>
  <div class="not-found-container">
    <div class="container">
      <div class="not-found-content">
        <div class="error-code">404</div>
        <h1>页面未找到</h1>
        <p>抱歉，您访问的页面不存在或已被移除</p>
        <div class="actions">
          <el-button type="primary" @click="goHome">
            返回首页
          </el-button>
          <el-button @click="goBack">
            返回上一页
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/home')
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.not-found-container {
  min-height: calc(100vh - 120px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.not-found-content {
  text-align: center;
}

.error-code {
  font-size: 120px;
  font-weight: bold;
  color: #ff6b6b;
  line-height: 1;
  margin-bottom: 20px;
}

h1 {
  font-size: 32px;
  color: #333;
  margin-bottom: 16px;
}

p {
  font-size: 16px;
  color: #666;
  margin-bottom: 32px;
}

.actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

@media (max-width: 768px) {
  .error-code {
    font-size: 80px;
  }

  h1 {
    font-size: 24px;
  }

  .actions {
    flex-direction: column;
  }

  .actions .el-button {
    width: 100%;
  }
}
</style>

