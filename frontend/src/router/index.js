import { createRouter, createWebHistory } from 'vue-router'
import { getToken, hasPermission } from '../utils/auth'

import LoginView from '../views/LoginView.vue'
import LayoutView from '../views/LayoutView.vue'
import AdminLayoutView from '../views/AdminLayoutView.vue'
import DashboardView from '../views/DashboardView.vue'
import DocumentsView from '../views/DocumentsView.vue'
import GlobalArticlesView from '../views/GlobalArticlesView.vue'
import ArticleDetailView from '../views/ArticleDetailView.vue'
import SoftwareToolsView from '../views/SoftwareToolsView.vue'
import AdminUsersView from '../views/AdminUsersView.vue'
import AdminArticlesView from '../views/AdminArticlesView.vue'
import AdminCommentsView from '../views/AdminCommentsView.vue'
import AdminSoftwareView from '../views/AdminSoftwareView.vue'
import AdminTagsView from '../views/AdminTagsView.vue'
import AdminRbacView from '../views/AdminRbacView.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { public: true }
  },
  {
    path: '/',
    redirect: '/app/dashboard'
  },
  {
    path: '/app',
    component: LayoutView,
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/app/dashboard' },
      { path: 'dashboard', name: 'dashboard', component: DashboardView, meta: { requiresAuth: true } },
      { path: 'documents', name: 'documents', component: DocumentsView, meta: { requiresAuth: true, permission: 'doc:read' } },
      { path: 'articles', name: 'articles', component: GlobalArticlesView, meta: { requiresAuth: true, permission: 'doc:read' } },
      { path: 'articles/:id', name: 'article-detail', component: ArticleDetailView, meta: { requiresAuth: true, permission: 'doc:read' } },
      { path: 'software', name: 'software', component: SoftwareToolsView, meta: { requiresAuth: true, permission: 'software:read' } }
    ]
  },
  {
    path: '/admin',
    component: AdminLayoutView,
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/admin/articles' },
      { path: 'articles', name: 'admin-articles', component: AdminArticlesView, meta: { requiresAuth: true, permission: 'doc:manage' } },
      { path: 'comments', name: 'admin-comments', component: AdminCommentsView, meta: { requiresAuth: true, permission: 'doc:manage' } },
      { path: 'users', name: 'admin-users', component: AdminUsersView, meta: { requiresAuth: true, permissionAny: ['user:manage', 'user:import'] } },
      { path: 'rbac', name: 'admin-rbac', component: AdminRbacView, meta: { requiresAuth: true, permission: 'rbac:manage' } },
      { path: 'tags', name: 'admin-tags', component: AdminTagsView, meta: { requiresAuth: true, permission: 'tag:manage' } },
      { path: 'software', name: 'admin-software', component: AdminSoftwareView, meta: { requiresAuth: true, permission: 'software:create' } }
    ]
  },
  {
    path: '/dashboard',
    redirect: '/app/dashboard'
  },
  {
    path: '/documents',
    redirect: '/app/documents'
  },
  {
    path: '/software',
    redirect: '/app/software'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = getToken()

  if (to.meta.public) {
    if (token && to.path === '/login') {
      next('/app/dashboard')
      return
    }
    next()
    return
  }

  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }

  const requiredPermission = to.meta.permission
  if (requiredPermission && !hasPermission(requiredPermission)) {
    next('/app/dashboard')
    return
  }

  const requiredPermissionAny = to.meta.permissionAny
  if (
    Array.isArray(requiredPermissionAny)
    && requiredPermissionAny.length > 0
    && !requiredPermissionAny.some((code) => hasPermission(code))
  ) {
    next('/app/dashboard')
    return
  }

  next()
})

export default router
