<template>
  <div class="tools-page">
    <section class="card form-card">
      <h3>软件推荐申请</h3>
      <p class="muted">提交后由后台审核，审核通过后会展示在推荐列表。</p>
      <form @submit.prevent="submitApplication">
        <div class="grid-two">
          <label>
            <span>软件名称</span>
            <input v-model.trim="form.name" maxlength="80" required placeholder="例如：Notion" />
          </label>
          <label>
            <span>分类</span>
            <input v-model.trim="form.category" maxlength="80" placeholder="例如：文档协作" />
          </label>
        </div>
        <label>
          <span>下载地址</span>
          <input v-model.trim="form.downloadUrl" maxlength="255" placeholder="https://..." />
        </label>
        <label>
          <span>推荐理由</span>
          <textarea v-model.trim="form.description" rows="4" maxlength="500" placeholder="写一下适合场景、使用体验"></textarea>
        </label>
        <button :disabled="submitting" type="submit">{{ submitting ? '提交中...' : '提交申请' }}</button>
      </form>
      <p v-if="message" class="success">{{ message }}</p>
      <p v-if="submitErrorMessage" class="error">{{ submitErrorMessage }}</p>
    </section>

    <section class="card">
      <div class="section-header">
        <h3>软件推荐列表</h3>
        <button type="button" @click="loadTools">刷新</button>
      </div>
      <p class="muted">仅展示审核通过的推荐。</p>
      <div v-if="tools.length === 0" class="empty">暂无推荐</div>
      <article v-for="tool in tools" :key="tool.id" class="doc-item">
        <h4>{{ tool.name }} <small>{{ tool.category || '' }}</small></h4>
        <p>{{ tool.description || '暂无描述' }}</p>
        <div class="doc-meta">
          <span>推荐人：{{ tool.recommendedBy || '-' }}</span>
          <span>{{ formatDate(tool.createdAt) }}</span>
          <a v-if="tool.downloadUrl" :href="tool.downloadUrl" target="_blank">下载/官网</a>
        </div>
      </article>
      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
    </section>
  </div>
</template>

<script>
import { listToolsApi, submitToolApplicationApi } from '../api/software'

export default {
  name: 'SoftwareToolsView',
  data() {
    return {
      submitting: false,
      message: '',
      submitErrorMessage: '',
      tools: [],
      form: {
        name: '',
        category: '',
        downloadUrl: '',
        description: ''
      },
      errorMessage: ''
    }
  },
  mounted() {
    this.loadTools()
  },
  methods: {
    formatDate(value) {
      if (!value) return '-'
      return new Date(value).toLocaleString('zh-CN')
    },
    async submitApplication() {
      if (!this.form.name) {
        this.submitErrorMessage = '软件名称不能为空'
        return
      }
      this.submitting = true
      this.submitErrorMessage = ''
      this.message = ''
      try {
        await submitToolApplicationApi(this.form)
        this.message = '申请已提交，等待后台审核'
        this.form = {
          name: '',
          category: '',
          downloadUrl: '',
          description: ''
        }
      } catch (e) {
        this.submitErrorMessage = e.message || '提交申请失败'
      } finally {
        this.submitting = false
      }
    },
    async loadTools() {
      try {
        const res = await listToolsApi()
        this.tools = res.data || []
      } catch (e) {
        this.errorMessage = e.message || '加载推荐失败'
      }
    }
  }
}
</script>
