import request from './index'

/**
 * 成员管理 API
 */

/**
 * 获取成员列表
 * @param {Object} params - 查询参数
 * @param {string} params.username - 用户名筛选（可选）
 * @param {string} params.role - 角色筛选（可选）
 * @param {number} params.status - 状态筛选（可选）
 * @param {number} params.page - 页码（默认1）
 * @param {number} params.size - 每页大小（默认10）
 * @returns {Promise}
 */
export const getMemberList = (params) => {
  return request.get('/members', { params })
}

/**
 * 获取成员详情
 * @param {number} id - 成员ID
 * @returns {Promise}
 */
export const getMemberDetail = (id) => {
  return request.get(`/members/${id}`)
}

/**
 * 创建成员
 * @param {Object} data - 成员数据
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @param {string} data.role - 角色（SUPER_ADMIN, ADMIN, USER）
 * @param {string} data.realName - 真实姓名（可选）
 * @param {string} data.email - 邮箱（可选）
 * @param {string} data.phone - 手机号（可选）
 * @returns {Promise}
 */
export const createMember = (data) => {
  return request.post('/members', data)
}

/**
 * 更新成员信息
 * @param {number} id - 成员ID
 * @param {Object} data - 更新数据
 * @returns {Promise}
 */
export const updateMember = (id, data) => {
  return request.put(`/members/${id}`, data)
}

/**
 * 删除成员
 * @param {number} id - 成员ID
 * @returns {Promise}
 */
export const deleteMember = (id) => {
  return request.delete(`/members/${id}`)
}

/**
 * 修改成员密码
 * @param {number} id - 成员ID
 * @param {Object} data - 密码数据
 * @param {string} data.newPassword - 新密码
 * @returns {Promise}
 */
export const changeMemberPassword = (id, data) => {
  return request.put(`/members/${id}/password`, data)
}

/**
 * 修改成员状态
 * @param {number} id - 成员ID
 * @param {Object} data - 状态数据
 * @param {number} data.status - 状态（0-禁用，1-启用）
 * @returns {Promise}
 */
export const changeMemberStatus = (id, data) => {
  return request.put(`/members/${id}/status`, data)
}

/**
 * 获取当前用户信息
 * @param {string} username - 用户名
 * @returns {Promise}
 */
export const getProfile = (username) => {
  return request.get('/profile', { params: { username } })
}

/**
 * 更新个人信息
 * @param {Object} data - 更新数据
 * @returns {Promise}
 */
export const updateProfile = (data) => {
  return request.put('/profile', data)
}

/**
 * 修改个人密码
 * @param {Object} data - 密码数据
 * @param {string} data.currentPassword - 当前密码
 * @param {string} data.newPassword - 新密码
 * @param {string} username - 用户名
 * @returns {Promise}
 */
export const changePassword = (data, username) => {
  return request.put('/profile/password', data, { params: { username } })
}
