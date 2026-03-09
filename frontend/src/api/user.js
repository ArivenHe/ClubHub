import http from './http'

export function listUsersApi(params = {}) {
  return http.get('/api/admin/users', { params })
}

export function createUserApi(data, roleCode) {
  const params = roleCode ? { roleCode } : {}
  return http.post('/api/admin/users/register', data, { params })
}

export function listRolesApi() {
  return http.get('/api/admin/users/roles')
}

export function importUsersApi(formData, roleCode) {
  const params = roleCode ? { roleCode } : {}
  return http.post('/api/admin/users/import', formData, {
    params,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function updateUserStatusApi(userId, enabled) {
  return http.put(`/api/admin/users/${userId}/status`, { enabled })
}

export function updateUserRoleApi(userId, roleCode) {
  return http.put(`/api/admin/users/${userId}/role`, { roleCode })
}

export function resetUserPasswordApi(userId, newPassword) {
  return http.put(`/api/admin/users/${userId}/password`, { newPassword })
}
