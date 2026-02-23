<template>
  <div class="page-container">
    <div class="page-header">
      <h2>评分与评价管理</h2>
    </div>
    
    <el-card>
      <div class="rating-overview">
        <div class="overall-rating">
          <span class="score">4.8</span>
          <el-rate v-model="overallRating" disabled show-score />
          <p class="rating-text">综合评分</p>
        </div>
        <div class="rating-stats">
          <div class="stat-item">
            <span class="label">口味评分</span>
            <el-rate v-model="tasteRating" disabled show-score />
          </div>
          <div class="stat-item">
            <span class="label">服务评分</span>
            <el-rate v-model="serviceRating" disabled show-score />
          </div>
          <div class="stat-item">
            <span class="label">配送评分</span>
            <el-rate v-model="deliveryRating" disabled show-score />
          </div>
        </div>
      </div>
    </el-card>
    
    <el-card style="margin-top: 20px;">
      <template #header>
        <span>评价列表</span>
      </template>
      
      <el-table :data="ratingList" v-loading="loading">
        <el-table-column prop="customer" label="客户" width="100" />
        <el-table-column prop="rating" label="评分" width="120">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled show-score />
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="评价内容" min-width="300" />
        <el-table-column prop="createTime" label="评价时间" width="150" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="text" @click="replyComment(row)">回复</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const overallRating = ref(4.8)
const tasteRating = ref(4.9)
const serviceRating = ref(4.7)
const deliveryRating = ref(4.6)

const ratingList = ref([
  {
    id: 1,
    customer: '张先生',
    rating: 5,
    comment: '奶茶很好喝，配送也很快！服务态度很好，推荐！',
    createTime: '2024-01-15 10:30'
  },
  {
    id: 2,
    customer: '李女士',
    rating: 4,
    comment: '味道不错，就是有点甜了，下次可以少糖',
    createTime: '2024-01-14 15:20'
  },
  {
    id: 3,
    customer: '王先生',
    rating: 5,
    comment: '服务态度很好，奶茶口感很棒，会继续光顾的！',
    createTime: '2024-01-13 18:45'
  }
])

const replyComment = (row) => {
  ElMessage.info(`回复评价: ${row.comment}`)
}
</script>

<style scoped>
.rating-overview {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 4px;
  
  .overall-rating {
    text-align: center;
    margin-right: 50px;
    
    .score {
      font-size: 48px;
      font-weight: bold;
      color: #409EFF;
      display: block;
    }
    
    .rating-text {
      margin: 10px 0 0 0;
      color: #666;
    }
  }
  
  .rating-stats {
    flex: 1;
    
    .stat-item {
      display: flex;
      align-items: center;
      margin-bottom: 15px;
      
      .label {
        width: 80px;
        margin-right: 10px;
      }
    }
  }
}
</style>
