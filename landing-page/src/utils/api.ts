// 从环境变量获取API基础URL,如果未设置则使用默认值
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://106.55.102.48:8080/api'

export interface CompanyInfo {
  id: number
  companyName: string
  slogan: string
  logo: string
  introPara1: string
  introPara2: string
  introPara3: string
  vision: string
  mission: string
  values: string
  email: string
  phone: string
  address: string
  wechatQrcode: string
  icpNumber: string
  createTime: string
  updateTime: string
}

export interface AppVersion {
  id: number
  version: string
  platform: string
  fileName: string
  fileSize: number
  filePath: string
  isLatest: boolean
  downloadCount: number
  releaseNotes: string
  uploadedBy: number
  createdAt: string
  updatedAt: string
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export async function getCompanyInfo(): Promise<CompanyInfo | null> {
  try {
    const response = await fetch(`${API_BASE_URL}/company`)
    const result: ApiResponse<CompanyInfo> = await response.json()
    
    if (result.code === 200 && result.data) {
      return result.data
    }
    return null
  } catch (error) {
    console.error('获取公司信息失败:', error)
    return null
  }
}

/**
 * 获取最新版本信息
 * @param platform - 平台: Windows 或 macOS
 */
export async function getLatestVersion(platform: string): Promise<AppVersion | null> {
  try {
    const response = await fetch(`${API_BASE_URL}/app-versions/latest/${platform}`)
    const result: ApiResponse<AppVersion> = await response.json()
    
    if (result.code === 200 && result.data) {
      return result.data
    }
    return null
  } catch (error) {
    console.error(`获取${platform}最新版本失败:`, error)
    return null
  }
}

/**
 * 获取下载链接
 * @param versionId - 版本ID
 */
export async function getDownloadUrl(versionId: number): Promise<string | null> {
  try {
    const response = await fetch(`${API_BASE_URL}/app-versions/${versionId}/download`)
    const result: ApiResponse<string> = await response.json()
    
    if (result.code === 200 && result.data) {
      return result.data
    }
    return null
  } catch (error) {
    console.error('获取下载链接失败:', error)
    return null
  }
}

/**
 * 格式化文件大小
 * @param bytes - 字节数
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}
