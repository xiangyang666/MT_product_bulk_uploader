/**
 * 应用配置
 */

// 获取环境变量
const env = import.meta.env

// API 基础地址
export const API_BASE_URL = env.VITE_API_BASE_URL || 'http://localhost:8080'

// 当前环境
export const APP_ENV = env.VITE_APP_ENV || 'development'

// 是否为生产环境
export const IS_PRODUCTION = APP_ENV === 'production'

// 是否为开发环境
export const IS_DEVELOPMENT = APP_ENV === 'development'

console.log('当前环境:', APP_ENV)
console.log('API 地址:', API_BASE_URL)

export default {
  API_BASE_URL,
  APP_ENV,
  IS_PRODUCTION,
  IS_DEVELOPMENT
}
