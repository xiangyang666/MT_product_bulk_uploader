/**
 * 开发者工具保护模块
 * 拦截 F12 键和右键菜单，要求输入密码才能打开开发者工具
 */

import { ElMessageBox, ElMessage } from 'element-plus'
import { verifyDevToolsPassword, getDevToolsPasswordStatus } from '@/api/devTools'

let isProtectionEnabled = false
let hasPassword = false

/**
 * 初始化开发者工具保护
 */
export async function initDevToolsProtection() {
  try {
    // 检查是否已设置密码
    const res = await getDevToolsPasswordStatus()
    hasPassword = res.data?.hasPassword || false
    
    if (!hasPassword) {
      console.log('开发者工具密码未设置，不启用保护')
      return
    }
    
    // 启用保护
    enableProtection()
    console.log('开发者工具保护已启用')
  } catch (error) {
    console.error('初始化开发者工具保护失败:', error)
  }
}

/**
 * 启用保护
 */
function enableProtection() {
  if (isProtectionEnabled) return
  
  // 拦截 F12 键和其他快捷键
  document.addEventListener('keydown', handleKeyDown, true)
  
  isProtectionEnabled = true
}

/**
 * 禁用保护
 */
export function disableProtection() {
  if (!isProtectionEnabled) return
  
  document.removeEventListener('keydown', handleKeyDown, true)
  
  isProtectionEnabled = false
}

/**
 * 处理键盘事件
 */
async function handleKeyDown(event) {
  // F12 键
  if (event.key === 'F12' || event.keyCode === 123) {
    event.preventDefault()
    event.stopPropagation()
    await showPasswordDialog('F12')
    return false
  }
  
  // Ctrl+Shift+I (Windows/Linux)
  if (event.ctrlKey && event.shiftKey && event.key === 'I') {
    event.preventDefault()
    event.stopPropagation()
    await showPasswordDialog('Ctrl+Shift+I')
    return false
  }
  
  // Cmd+Option+I (Mac)
  if (event.metaKey && event.altKey && event.key === 'I') {
    event.preventDefault()
    event.stopPropagation()
    await showPasswordDialog('Cmd+Option+I')
    return false
  }
  
  // Ctrl+Shift+J (Windows/Linux)
  if (event.ctrlKey && event.shiftKey && event.key === 'J') {
    event.preventDefault()
    event.stopPropagation()
    await showPasswordDialog('Ctrl+Shift+J')
    return false
  }
  
  // Cmd+Option+J (Mac)
  if (event.metaKey && event.altKey && event.key === 'J') {
    event.preventDefault()
    event.stopPropagation()
    await showPasswordDialog('Cmd+Option+J')
    return false
  }
  
  // Ctrl+Shift+C (Windows/Linux)
  if (event.ctrlKey && event.shiftKey && event.key === 'C') {
    event.preventDefault()
    event.stopPropagation()
    await showPasswordDialog('Ctrl+Shift+C')
    return false
  }
  
  // Cmd+Option+C (Mac)
  if (event.metaKey && event.altKey && event.key === 'C') {
    event.preventDefault()
    event.stopPropagation()
    await showPasswordDialog('Cmd+Option+C')
    return false
  }
  
  // Ctrl+Shift+D (Windows/Linux) - 自定义开发者工具快捷键
  if (event.ctrlKey && event.shiftKey && event.key === 'D') {
    event.preventDefault()
    event.stopPropagation()
    await showPasswordDialog('Ctrl+Shift+D')
    return false
  }
  
  // Cmd+Option+D (Mac) - 自定义开发者工具快捷键
  if (event.metaKey && event.altKey && event.key === 'D') {
    event.preventDefault()
    event.stopPropagation()
    await showPasswordDialog('Cmd+Option+D')
    return false
  }
}

/**
 * 显示密码输入对话框
 */
async function showPasswordDialog(trigger) {
  try {
    const { value: password } = await ElMessageBox.prompt(
      '请输入开发者工具密码',
      '开发者工具访问验证',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputType: 'password',
        inputPlaceholder: '请输入密码',
        inputValidator: (value) => {
          if (!value) {
            return '密码不能为空'
          }
          if (value.length < 6) {
            return '密码长度至少6个字符'
          }
          return true
        },
        beforeClose: async (action, instance, done) => {
          if (action === 'confirm') {
            instance.confirmButtonLoading = true
            instance.confirmButtonText = '验证中...'
            
            try {
              // 验证密码
              const res = await verifyDevToolsPassword({ password: instance.inputValue })
              
              if (res.data?.valid) {
                ElMessage.success('密码验证通过')
                
                // 临时禁用保护，允许用户打开开发者工具
                disableProtection()
                
                // 提示用户手动打开开发者工具
                ElMessage.info({
                  message: '请再次按 F12 或右键打开开发者工具',
                  duration: 3000
                })
                
                // 3秒后重新启用保护
                setTimeout(() => {
                  enableProtection()
                }, 3000)
                
                done()
              } else {
                ElMessage.error('密码错误，请重试')
                instance.confirmButtonLoading = false
                instance.confirmButtonText = '确定'
              }
            } catch (error) {
              console.error('验证密码失败:', error)
              ElMessage.error('验证失败，请重试')
              instance.confirmButtonLoading = false
              instance.confirmButtonText = '确定'
            }
          } else {
            done()
          }
        }
      }
    )
  } catch (error) {
    // 用户取消
    if (error !== 'cancel') {
      console.error('密码对话框错误:', error)
    }
  }
}

/**
 * 刷新密码状态
 * 在 admin-plus 设置密码后调用
 */
export async function refreshPasswordStatus() {
  try {
    const res = await getDevToolsPasswordStatus()
    const newHasPassword = res.data?.hasPassword || false
    
    if (newHasPassword && !hasPassword) {
      // 从无密码变为有密码，启用保护
      hasPassword = true
      enableProtection()
      console.log('开发者工具保护已启用')
    } else if (!newHasPassword && hasPassword) {
      // 从有密码变为无密码，禁用保护
      hasPassword = false
      disableProtection()
      console.log('开发者工具保护已禁用')
    }
  } catch (error) {
    console.error('刷新密码状态失败:', error)
  }
}
