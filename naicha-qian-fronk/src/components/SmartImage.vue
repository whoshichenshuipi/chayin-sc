<template>
  <img
    v-if="processedSrc"
    ref="imgRef"
    :src="processedSrc"
    :alt="alt"
    :class="imageClass"
    :style="imageStyle"
    :loading="loading"
    @error="handleError"
    @load="handleLoad"
  />
</template>

<script setup>
import { computed, ref, onBeforeUnmount } from 'vue'
import { getImageUrl, handleImageError as handleImageErrorUtil } from '@/utils/imageUtils'

const props = defineProps({
  // 图片源（可以是相对路径、完整URL或data URI）
  src: {
    type: String,
    default: ''
  },
  // 图片alt文本
  alt: {
    type: String,
    default: ''
  },
  // 自定义CSS类
  imageClass: {
    type: String,
    default: ''
  },
  // 自定义样式
  imageStyle: {
    type: [String, Object],
    default: ''
  },
  // 懒加载
  loading: {
    type: String,
    default: 'lazy',
    validator: (value) => ['lazy', 'eager'].includes(value)
  },
  // 默认图片（当加载失败时使用）
  defaultImage: {
    type: String,
    default: null
  }
})

const emit = defineEmits(['error', 'load'])

const imgRef = ref(null)

// 处理后的图片URL
const processedSrc = computed(() => {
  if (!props.src) {
    return props.defaultImage || null
  }
  
  // 如果是完整的 URL，直接返回
  if (props.src.startsWith('http://') || props.src.startsWith('https://')) {
    return props.src
  }
  
  // 如果是 data URI，直接返回
  if (props.src.startsWith('data:')) {
    return props.src
  }
  
  // 如果是 Vite 处理的资源路径（/assets/ 或 /src/assets/），直接返回
  // Vite 会自动处理这些路径
  if (props.src.startsWith('/assets/') || props.src.startsWith('/src/assets/')) {
    return props.src
  }
  
  // 使用 getImageUrl 处理相对路径
  const processed = getImageUrl(props.src)
  return processed || props.defaultImage || props.src
})

// 默认商品图片（SVG base64）
const defaultProductImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgdmlld0JveD0iMCAwIDIwMCAyMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIyMDAiIGhlaWdodD0iMjAwIiBmaWxsPSIjRjVGNUY1Ii8+CjxwYXRoIGQ9Ik0xMDAgNzBDODQuNjg2MyA3MCA3MiA4Mi42ODYzIDcyIDk4QzcyIDExMy4zMTQgODQuNjg2MyAxMjYgMTAwIDEyNkMxMTUuMzE0IDEyNiAxMjggMTEzLjMxNCAxMjggOThDMTI4IDgyLjY4NjMgMTE1LjMxNCA3MCAxMDAgNzBaIiBmaWxsPSIjQ0NDQ0NDIi8+CjxwYXRoIGQ9Ik01MCAxNDBDNTAgMTI1LjU4MiA2My41ODE3IDExMiA4MCAxMTJIMTIwQzEzNi40MTggMTEyIDE1MCAxMjUuNTgyIDE1MCAxNDBWMTYwSDUwVjE0MFoiIGZpbGw9IiNDQ0NDQ0MiLz4KPC9zdmc+'

// 图片加载错误处理
const handleError = (event) => {
  // 检查元素是否还存在
  if (!event || !event.target) {
    return
  }
  
  const target = event.target
  const currentSrc = target.src
  
  // 如果当前已经是默认图片，避免无限循环
  if (props.defaultImage && currentSrc === props.defaultImage) {
    return
  }
  
  // 如果当前已经是通用默认图片，也避免无限循环
  if (currentSrc === defaultProductImage) {
    return
  }
  
  // 记录错误日志（仅在开发环境）
  if (import.meta.env.DEV) {
    console.warn('图片加载失败:', currentSrc)
  }
  
  // 使用自定义默认图片，如果没有则使用通用默认图片
  const fallbackImage = props.defaultImage || defaultProductImage
  target.src = fallbackImage
  
  // 触发错误事件
  emit('error', event)
}

// 图片加载成功处理
const handleLoad = (event) => {
  if (event && event.target) {
    emit('load', event)
  }
}

// 组件卸载前清理
onBeforeUnmount(() => {
  if (imgRef.value) {
    imgRef.value = null
  }
})
</script>

