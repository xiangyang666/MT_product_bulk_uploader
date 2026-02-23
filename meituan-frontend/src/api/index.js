import axios from 'axios'
import { ElMessage } from 'element-plus'
import { API_BASE_URL } from '@/config'

// 创建axios实例
const request = axios.create({
  baseURL: `${API_BASE_URL}/api`,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 添加 token 到请求头
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 如果是blob类型（文件下载），直接返回data
    if (response.config.responseType === 'blob') {
      return response.data
    }
    
    const res = response.data
    
    // 根据业务状态码处理
    if (res.code !== 200) {
      // 检查是否是静默模式
      if (!response.config._silent) {
        ElMessage.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res
  },
  error => {
    console.error('响应错误:', error)
    
    // 根据HTTP状态码显示不同的错误信息
    if (error.response) {
      switch (error.response.status) {
        case 400:
          ElMessage.error('请求参数错误，请检查输入')
          break
        case 401:
          ElMessage.error('未授权访问，请重新登录')
          break
        case 413:
          ElMessage.error('文件过大，请选择小于10MB的文件')
          break
        case 500:
          ElMessage.error('服务器错误，请稍后重试')
          break
        case 502:
          ElMessage.error('美团平台连接失败，请检查网络')
          break
        default:
          ElMessage.error('操作失败，请重试')
      }
    } else if (error.request) {
      ElMessage.error('网络连接失败，请检查网络设置')
    }
    
    return Promise.reject(error)
  }
)

// ==================== 批量上传全部商品 API ====================

/**
 * 获取商品统计信息
 * @param {number} merchantId - 商家ID (可选，默认1)
 * @returns {Promise<ProductStats>}
 */
export const getProductStats = (merchantId = 1) => {
  return request.get('/products/stats', {
    params: { merchantId }
  })
}

/**
 * 获取最近商品列表
 * @param {number} merchantId - 商家ID (可选，默认1)
 * @param {number} limit - 返回数量 (可选，默认10)
 * @returns {Promise<ProductPreview[]>}
 */
export const getRecentProducts = (merchantId = 1, limit = 10) => {
  return request.get('/products/recent', {
    params: { merchantId, limit }
  })
}

/**
 * 生成全部商品模板
 * @param {number} merchantId - 商家ID (可选，默认1)
 * @returns {Promise<Blob>}
 */
export const generateAllTemplate = (merchantId = 1) => {
  return request.post('/products/generate-all-template', null, {
    params: { merchantId },
    responseType: 'blob'
  })
}

/**
 * 获取最近操作历史
 * @param {number} merchantId - 商家ID (可选，默认1)
 * @param {string} type - 操作类型 (可选)
 * @param {number} limit - 返回数量 (可选，默认3)
 * @returns {Promise<OperationLog[]>}
 */
export const getRecentOperations = (merchantId = 1, type = null, limit = 3) => {
  return request.get('/products/operation-logs/recent', {
    params: { merchantId, type, limit }
  })
}

/**
 * 获取历史生成文件
 * @param {number} merchantId - 商家ID (可选，默认1)
 * @param {number} limit - 返回数量 (可选，默认10)
 * @returns {Promise<GeneratedFile[]>}
 */
export const getRecentGeneratedFiles = (merchantId = 1, limit = 10) => {
  return request.get('/products/generated-files/recent', {
    params: { merchantId, limit }
  })
}

/**
 * 下载历史文件
 * @param {number} fileId - 文件ID
 * @returns {Promise<Blob>}
 */
export const downloadGeneratedFile = (fileId) => {
  return request.get(`/products/generated-files/${fileId}/download`, {
    responseType: 'blob'
  })
}

export default request


// ==================== 应用版本管理 API ====================

/**
 * 上传新版本
 * @param {File} file - 安装包文件
 * @param {Object} data - 版本信息 {version, releaseNotes}
 * @param {Function} onProgress - 上传进度回调
 * @returns {Promise}
 */
export const uploadVersion = (file, data, onProgress) => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('version', data.version)
  if (data.releaseNotes) {
    formData.append('releaseNotes', data.releaseNotes)
  }
  
  return request.post('/app-versions/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 1200000, // 20分钟超时,适合大文件上传和服务器处理
    onUploadProgress: (progressEvent) => {
      if (onProgress && progressEvent.total) {
        const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(percentCompleted)
      }
    }
  })
}

/**
 * 获取版本列表
 * @param {string} platform - 平台 (Windows/macOS)
 * @param {number} page - 页码
 * @param {number} size - 每页数量
 * @returns {Promise}
 */
export const getVersionList = (platform, page = 1, size = 20) => {
  return request.get('/app-versions', {
    params: { platform, page, size }
  })
}

/**
 * 获取最新版本
 * @param {string} platform - 平台 (Windows/macOS)
 * @param {boolean} silent - 是否静默模式（不显示错误提示）
 * @returns {Promise}
 */
export const getLatestVersion = (platform, silent = false) => {
  return request.get(`/app-versions/latest/${platform}`, {
    // 添加自定义配置，用于在拦截器中判断
    _silent: silent
  }).catch(error => {
    // 静默模式下，返回一个默认结构而不是抛出错误
    if (silent) {
      return { code: 404, message: '未找到版本', data: null }
    }
    throw error
  })
}

/**
 * 设置为最新版本
 * @param {number} id - 版本ID
 * @returns {Promise}
 */
export const setLatestVersion = (id) => {
  return request.put(`/app-versions/${id}/set-latest`)
}

/**
 * 删除版本
 * @param {number} id - 版本ID
 * @returns {Promise}
 */
export const deleteVersion = (id) => {
  return request.delete(`/app-versions/${id}`)
}

/**
 * 获取下载链接
 * @param {number} id - 版本ID
 * @returns {Promise}
 */
export const getDownloadUrl = (id) => {
  return request.get(`/app-versions/${id}/download`)
}
