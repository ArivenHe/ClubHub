import http from './http'

export function listToolsApi() {
  return http.get('/api/software-tools')
}

export function submitToolApplicationApi(data) {
  return http.post('/api/software-tools/applications', data)
}

export function createToolApi(data) {
  return http.post('/api/software-tools', data)
}

export function adminListToolsApi() {
  return http.get('/api/admin/software-tools')
}

export function reviewToolApi(id, data) {
  return http.put(`/api/admin/software-tools/${id}/status`, data)
}

export function deleteToolApi(id) {
  return http.delete(`/api/admin/software-tools/${id}`)
}
