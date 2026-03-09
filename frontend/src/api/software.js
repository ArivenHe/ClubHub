import http from './http'

export function listToolsApi() {
  return http.get('/api/software-tools')
}

export function createToolApi(data) {
  return http.post('/api/software-tools', data)
}
