import request from './index'

/**
 * 设置开发者工具密码
 * @param {Object} data - { password: string }
 * @returns {Promise}
 */
export const setDevToolsPassword = (data) => {
  return request.post('/dev-tools/password', data)
}

/**
 * 验证开发者工具密码
 * @param {Object} data - { password: string }
 * @returns {Promise}
 */
export const verifyDevToolsPassword = (data) => {
  return request.post('/dev-tools/password/verify', data)
}

/**
 * 获取开发者工具密码设置状态
 * @returns {Promise}
 */
export const getDevToolsPasswordStatus = () => {
  return request.get('/dev-tools/password/status')
}
