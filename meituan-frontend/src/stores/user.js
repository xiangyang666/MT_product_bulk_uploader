import { defineStore } from 'pinia'
import request from '@/api/index.js'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  }),

  getters: {
    isLoggedIn: (state) => !!state.token
  },

  actions: {
    // 登录
    async login(loginForm) {
      try {
        const response = await request.post('/auth/login', {
          username: loginForm.username,
          password: loginForm.password
        })

        if (response.code === 200) {
          this.token = response.data.token
          this.userInfo = response.data.userInfo

          // 保存到本地存储
          localStorage.setItem('token', this.token)
          localStorage.setItem('userInfo', JSON.stringify(this.userInfo))

          // 如果选择记住密码
          if (loginForm.remember) {
            localStorage.setItem('rememberedUsername', loginForm.username)
          } else {
            localStorage.removeItem('rememberedUsername')
          }

          return response.data
        } else {
          throw new Error(response.message || '登录失败')
        }
      } catch (error) {
        throw error
      }
    },

    // 注册
    async register(registerForm) {
      try {
        const response = await request.post('/auth/register', {
          username: registerForm.username,
          password: registerForm.password,
          email: registerForm.email
        })

        if (response.code === 200) {
          return response.data
        } else {
          throw new Error(response.message || '注册失败')
        }
      } catch (error) {
        throw error
      }
    },

    // 退出登录
    async logout() {
      try {
        await request.post('/auth/logout')
      } catch (error) {
        console.error('退出登录失败:', error)
      } finally {
        // 清除本地数据
        this.token = ''
        this.userInfo = {}
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
      }
    },

    // 获取用户信息
    async getUserInfo() {
      try {
        const response = await request.get('/auth/userinfo')
        if (response.code === 200) {
          this.userInfo = response.data
          localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
          return response.data
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
        throw error
      }
    }
  }
})
