const TOKEN_KEY = 'satoken'
const USER_KEY = 'clubhub_user'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function getUserInfo() {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch (e) {
    return null
  }
}

export function setUserInfo(userInfo) {
  localStorage.setItem(USER_KEY, JSON.stringify(userInfo))
}

export function clearUserInfo() {
  localStorage.removeItem(USER_KEY)
}

export function hasPermission(permissionCode) {
  const user = getUserInfo()
  if (!user || !Array.isArray(user.permissions)) {
    return false
  }
  return user.permissions.includes(permissionCode)
}

export function logoutLocal() {
  clearToken()
  clearUserInfo()
}
