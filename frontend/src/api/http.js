import axios from 'axios'
import { getToken, logoutLocal } from '../utils/auth'

const TOKEN_HEADER_NAME = import.meta.env.VITE_SA_TOKEN_NAME || 'jmi-openatom'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 15000
})

service.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers[TOKEN_HEADER_NAME] = token
    config.headers.satoken = token
  }
  return config
})

service.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (payload && payload.success) {
      return payload
    }
    const msg = payload?.message || '请求失败'
    if (msg.includes('未登录')) {
      logoutLocal()
      window.location.href = '/login'
    }
    return Promise.reject(new Error(msg))
  },
  (error) => Promise.reject(error)
)

export default service
