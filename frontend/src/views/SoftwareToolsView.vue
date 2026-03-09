<template>
  <div class="tools-page">
    <section class="card">
      <div class="section-header">
        <h3>软件推荐列表</h3>
        <button type="button" @click="loadTools">刷新</button>
      </div>
      <p class="muted">新增和维护推荐请到后台「软件管理」。</p>
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
import { listToolsApi } from '../api/software'

export default {
  name: 'SoftwareToolsView',
  data() {
    return {
      tools: [],
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
