<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="brand">
        <h1>JMIOPENATOM</h1>
        <p>社团文档平台</p>
      </div>
      <nav>
        <router-link to="/app/dashboard">首页</router-link>
        <router-link v-if="canDocRead" to="/app/documents">我的文档</router-link>
        <router-link v-if="canDocRead" to="/app/articles">全站文档</router-link>
        <router-link v-if="canSoftwareRead" to="/app/software">软件推荐</router-link>
      </nav>
      <div class="sidebar-footer" v-if="canEnterAdmin">
        <button type="button" class="secondary-btn" @click="goAdmin">进入后台管理</button>
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
  '/app/dashboard': '平台概览',
  '/app/documents': '我的文档',
  '/app/articles': '全站文档列表',
  '/app/software': '好用软件区'
}

export default {
  name: 'LayoutView',
  data() {
    return {
      user: getUserInfo(),
      theme: getTheme()
    }
  },
  computed: {
    pageTitle() {
      if (this.$route.path.startsWith('/app/articles/')) {
        return '文章详情'
      }
      return routeTitleMap[this.$route.path] || '社团平台'
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
    canDocRead() {
      return hasPermission('doc:read')
    },
    canSoftwareRead() {
      return hasPermission('software:read')
    },
    canEnterAdmin() {
      return hasPermission('doc:manage')
        || hasPermission('user:manage')
        || hasPermission('user:import')
        || hasPermission('rbac:manage')
        || hasPermission('tag:manage')
        || hasPermission('software:create')
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
    goAdmin() {
      if (hasPermission('doc:manage')) {
        this.$router.push('/admin/articles')
        return
      }
      if (hasPermission('user:manage') || hasPermission('user:import')) {
        this.$router.push('/admin/users')
        return
      }
      if (hasPermission('rbac:manage')) {
        this.$router.push('/admin/rbac')
        return
      }
      if (hasPermission('tag:manage')) {
        this.$router.push('/admin/tags')
        return
      }
      this.$router.push('/admin/software')
    },
    toggleThemeMode() {
      this.theme = toggleTheme()
    }
  }
}
</script>

<style>

</style>
