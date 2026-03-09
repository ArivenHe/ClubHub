import http from './http'

export function createDocumentApi(data) {
  return http.post('/api/documents', data)
}

export function updateMyDocumentApi(id, data) {
  return http.put(`/api/documents/${id}`, data)
}

export function myDocumentsApi() {
  return http.get('/api/documents/mine')
}

export function allDocumentsApi() {
  return http.get('/api/documents')
}

export function documentDetailApi(id) {
  return http.get(`/api/documents/${id}`)
}

export function deleteMyDocumentApi(id) {
  return http.delete(`/api/documents/${id}`)
}

export function likeDocumentApi(id) {
  return http.post(`/api/documents/${id}/likes`)
}

export function unlikeDocumentApi(id) {
  return http.delete(`/api/documents/${id}/likes`)
}

export function recommendedDocumentsApi(limit = 6) {
  return http.get('/api/documents/recommended', { params: { limit } })
}

export function listDocumentCommentsApi(id) {
  return http.get(`/api/documents/${id}/comments`)
}

export function createDocumentCommentApi(id, data) {
  return http.post(`/api/documents/${id}/comments`, data)
}

export function deleteDocumentCommentApi(commentId) {
  return http.delete(`/api/documents/comments/${commentId}`)
}

export function adminArticlesApi(params = {}) {
  return http.get('/api/admin/articles', { params })
}

export function updateArticleStatusApi(id, status) {
  return http.put(`/api/admin/articles/${id}/status`, null, { params: { status } })
}

export function updateArticleRecommendedApi(id, recommended) {
  return http.put(`/api/admin/articles/${id}/recommended`, null, { params: { recommended } })
}

export function deleteArticleApi(id) {
  return http.delete(`/api/admin/articles/${id}`)
}

export function adminCommentsApi(params = {}) {
  return http.get('/api/admin/comments', { params })
}

export function deleteAdminCommentApi(id) {
  return http.delete(`/api/admin/comments/${id}`)
}
