import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import SmartImage from './components/SmartImage.vue'

// 导入全局样式
import './styles/variables.css'
import './styles/global.css'
import './styles/element-theme.css'
import './styles/responsive.css'

const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 注册全局图片组件
app.component('SmartImage', SmartImage)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

app.mount('#app')
