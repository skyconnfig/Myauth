import axios from 'axios'
import { message } from 'ant-design-vue'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    
    if (res.code !== 200) {
      message.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res
  },
  error => {
    // 处理401未授权错误
    if (error.response && error.response.status === 401) {
      message.error('登录已过期，请重新登录')
      // 清除本地token
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('realName')
      localStorage.removeItem('userId')
      // 跳转到登录页
      window.location.href = '/login'
      return Promise.reject(error)
    }
    
    message.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
