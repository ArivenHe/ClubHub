import http from './http'

export function listTagsApi() {
  return http.get('/api/tags')
}

export function createTagApi(data) {
  return http.post('/api/tags', data)
}

export function adminListTagsApi() {
  return http.get('/api/admin/tags')
}

export function adminCreateTagApi(data) {
  return http.post('/api/admin/tags', data)
}

export function adminUpdateTagApi(id, data) {
  return http.put(`/api/admin/tags/${id}`, data)
}

export function adminDeleteTagApi(id) {
  return http.delete(`/api/admin/tags/${id}`)
}
