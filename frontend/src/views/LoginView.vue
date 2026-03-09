<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-actions">
        <button type="button" class="small-btn theme-btn" @click="toggleThemeMode">{{ themeSwitchText }}</button>
      </div>
      <h1>社团成长文档平台</h1>
      <p>记录每个学期的成长，沉淀给下一届的实用经验。</p>
      <form @submit.prevent="onSubmit">
        <label>
          <span>用户名 / 学号</span>
          <input v-model.trim="form.username" type="text" placeholder="请输入用户名或学号" required />
        </label>
        <label>
          <span>密码</span>
          <input v-model.trim="form.password" type="password" placeholder="请输入密码" required />
        </label>
        <button type="submit" :disabled="loading">{{ loading ? '登录中...' : '登录' }}</button>
      </form>
      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
    </div>
  </div>
</template>

<script>
import { loginApi } from '../api/auth'
import { setToken, setUserInfo } from '../utils/auth'
import { getTheme, toggleTheme } from '../utils/theme'

export default {
  name: 'LoginView',
  data() {
    return {
      loading: false,
      theme: getTheme(),
      errorMessage: '',
      form: {
        username: '',
        password: ''
      }
    }
  },
  methods: {
    toggleThemeMode() {
      this.theme = toggleTheme()
    },
    async onSubmit() {
      if (!this.form.username || !this.form.password) {
        this.errorMessage = '请填写账号和密码'
        return
      }

      this.loading = true
      this.errorMessage = ''
      try {
        const res = await loginApi(this.form)
        setToken(res.data.token)
        setUserInfo(res.data.userInfo)
        this.$router.replace('/app/dashboard')
      } catch (e) {
        this.errorMessage = e.message || '登录失败'
      } finally {
        this.loading = false
      }
    }
  },
  computed: {
    themeSwitchText() {
      return this.theme === 'dark' ? '浅色' : '深色'
    }
  }
}
</script>
