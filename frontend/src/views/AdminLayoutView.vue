<template>
  <div class="layout admin-layout">
    <aside class="sidebar admin-sidebar">
      <div class="brand">
        <h1>JMIOPENATOM</h1>
        <p>后台管理中心</p>
      </div>
      <nav>
        <router-link v-if="canDocManage" to="/admin/articles">文章管理</router-link>
        <router-link v-if="canDocManage" to="/admin/comments">评论管理</router-link>
        <router-link v-if="canUserAdmin" to="/admin/users">成员管理</router-link>
        <router-link v-if="canRbacManage" to="/admin/rbac">RBAC管理</router-link>
        <router-link v-if="canTagManage" to="/admin/tags">标签管理</router-link>
        <router-link v-if="canSoftwareCreate" to="/admin/software">软件管理</router-link>
      </nav>
      <div class="sidebar-footer">
        <button type="button" class="secondary-btn" @click="$router.push('/app/dashboard')">返回前台</button>
      </div>
    </aside>
    <main class="content">
      <header class="topbar">
        <div class="topbar-main">
          <h2>{{ pageTitle }}</h2>
          <p>{{ currentDate }}</p>
        </div>
        <div class="user-tools topbar-actions">
          <span>{{ userDisplay }}</span>
          <button type="button" class="small-btn secondary-btn-inline" @click="changePassword">改密</button>
          <button type="button" class="small-btn theme-btn" @click="toggleThemeMode">{{ themeSwitchText }}</button>
          <button type="button" @click="logout">退出</button>
        </div>
      </header>
      <section class="page-wrapper">
        <router-view />
      </section>
    </main>
  </div>
</template>

<script>
import { changePasswordApi, logoutApi } from '../api/auth'
import { getUserInfo, hasPermission, logoutLocal } from '../utils/auth'
import { showAlert, showPrompt } from '../utils/dialog'
import { getTheme, toggleTheme } from '../utils/theme'

const routeTitleMap = {
  '/admin/articles': '文章管理',
  '/admin/comments': '评论管理',
  '/admin/users': '成员管理',
  '/admin/rbac': 'RBAC管理',
  '/admin/tags': '标签管理',
  '/admin/software': '软件管理'
}

export default {
  name: 'AdminLayoutView',
  data() {
    return {
      user: getUserInfo(),
      theme: getTheme()
    }
  },
  computed: {
    pageTitle() {
      return routeTitleMap[this.$route.path] || '后台管理'
    },
    currentDate() {
      return new Date().toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    },
    userDisplay() {
      return this.user?.realName || this.user?.username || '未命名用户'
    },
    canDocManage() {
      return hasPermission('doc:manage')
    },
    canUserAdmin() {
      return hasPermission('user:manage') || hasPermission('user:import')
    },
    canRbacManage() {
      return hasPermission('rbac:manage')
    },
    canTagManage() {
      return hasPermission('tag:manage')
    },
    canSoftwareCreate() {
      return hasPermission('software:create')
    },
    themeSwitchText() {
      return this.theme === 'dark' ? '浅色' : '深色'
    }
  },
  methods: {
    async logout() {
      try {
        await logoutApi()
      } catch (e) {
        // ignore server-side logout errors
      } finally {
        logoutLocal()
        this.$router.replace('/login')
      }
    },
    async changePassword() {
      const oldPassword = await showPrompt({
        title: '修改密码',
        message: '请输入当前密码',
        placeholder: '当前密码',
        inputType: 'password'
      })
      if (oldPassword === null) {
        return
      }
      const newPassword = await showPrompt({
        title: '修改密码',
        message: '请输入新密码（至少6位）',
        placeholder: '新密码',
        inputType: 'password',
        validator: (value) => {
          if (value.length < 6) {
            return '新密码至少6位'
          }
          return ''
        }
      })
      if (newPassword === null) {
        return
      }
      const confirmPassword = await showPrompt({
        title: '修改密码',
        message: '请再次输入新密码',
        placeholder: '再次输入新密码',
        inputType: 'password',
        validator: (value) => {
          if (value !== newPassword) {
            return '两次输入的新密码不一致'
          }
          return ''
        }
      })
      if (confirmPassword === null) {
        return
      }
      try {
        await changePasswordApi({
          oldPassword,
          newPassword
        })
        await showAlert({
          title: '提示',
          message: '密码修改成功'
        })
      } catch (e) {
        await showAlert({
          title: '提示',
          message: e.message || '密码修改失败'
        })
      }
    },
    toggleThemeMode() {
      this.theme = toggleTheme()
    }
  }
}
</script>
