import { createApp } from 'vue'
import './assets/styles/variables.css'
import './assets/styles/global.css'
import './assets/styles/animations.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(router)
app.mount('#app')
