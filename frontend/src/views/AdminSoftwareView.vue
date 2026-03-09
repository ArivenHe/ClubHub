<template>
  <div class="tools-page">
    <section class="card form-card">
      <h3>新增好用软件</h3>
      <form @submit.prevent="submitTool">
        <div class="grid-two">
          <label>
            <span>名称</span>
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
          <textarea v-model.trim="form.description" rows="4" maxlength="500" placeholder="写一下怎么用、适合谁"></textarea>
        </label>
        <label>
          <span>推荐人</span>
          <input v-model.trim="form.recommendedBy" maxlength="80" placeholder="你的名字" />
        </label>
        <button :disabled="submitting" type="submit">{{ submitting ? '提交中...' : '提交推荐' }}</button>
      </form>
      <p v-if="message" class="success">{{ message }}</p>
      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
    </section>

    <section class="card">
      <div class="section-header">
        <h3>软件推荐列表</h3>
        <button type="button" @click="loadTools">刷新</button>
      </div>
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
    </section>
  </div>
</template>

<script>
import { createToolApi, listToolsApi } from '../api/software'

export default {
  name: 'AdminSoftwareView',
  data() {
    return {
      submitting: false,
      message: '',
      errorMessage: '',
      tools: [],
      form: {
        name: '',
        category: '',
        downloadUrl: '',
        description: '',
        recommendedBy: ''
      }
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
    async submitTool() {
      if (!this.form.name) {
        this.errorMessage = '名称不能为空'
        return
      }
      this.submitting = true
      this.message = ''
      this.errorMessage = ''
      try {
        await createToolApi(this.form)
        this.message = '推荐提交成功'
        this.form = {
          name: '',
          category: '',
          downloadUrl: '',
          description: '',
          recommendedBy: ''
        }
        this.loadTools()
      } catch (e) {
        this.errorMessage = e.message || '提交失败'
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
