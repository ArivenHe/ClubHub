import http from './http'

export function listRbacRolesApi(params = {}) {
  return http.get('/api/admin/rbac/roles', { params })
}

export function createRbacRoleApi(data) {
  return http.post('/api/admin/rbac/roles', data)
}

export function updateRbacRoleApi(id, data) {
  return http.put(`/api/admin/rbac/roles/${id}`, data)
}

export function deleteRbacRoleApi(id) {
  return http.delete(`/api/admin/rbac/roles/${id}`)
}

export function listRbacPermissionsApi(params = {}) {
  return http.get('/api/admin/rbac/permissions', { params })
}

export function createRbacPermissionApi(data) {
  return http.post('/api/admin/rbac/permissions', data)
}

export function updateRbacPermissionApi(id, data) {
  return http.put(`/api/admin/rbac/permissions/${id}`, data)
}

export function deleteRbacPermissionApi(id) {
  return http.delete(`/api/admin/rbac/permissions/${id}`)
}
