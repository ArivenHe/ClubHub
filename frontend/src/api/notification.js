import http from './http'

export function listNotificationsApi(limit = 20) {
  return http.get('/api/notifications', { params: { limit } })
}

export function unreadNotificationCountApi() {
  return http.get('/api/notifications/unread-count')
}

export function markNotificationReadApi(id) {
  return http.put(`/api/notifications/${id}/read`)
}

export function markAllNotificationsReadApi() {
  return http.put('/api/notifications/read-all')
}
