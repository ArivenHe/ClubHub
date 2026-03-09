import http from './http'

export function loginApi(data) {
  return http.post('/api/auth/login', data)
}

export function logoutApi() {
  return http.post('/api/auth/logout')
}

export function changePasswordApi(data) {
  return http.post('/api/auth/password', data)
}

export function meApi() {
  return http.get('/api/auth/me')
}
