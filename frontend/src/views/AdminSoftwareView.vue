<template>
  <div class="tools-page">
    <section class="card form-card">
      <h3>新增好用软件（后台直发）</h3>
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
          <input v-model.trim="form.recommendedBy" maxlength="80" placeholder="默认自动带入当前账号姓名+学号" />
        </label>
        <button :disabled="submitting" type="submit">{{ submitting ? '提交中...' : '提交推荐' }}</button>
      </form>
      <p v-if="message" class="success">{{ message }}</p>
      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
    </section>

    <section class="card">
      <div class="section-header">
        <h3>软件推荐审核列表</h3>
        <span class="muted">待审核：{{ pendingCount }} 条</span>
        <button type="button" @click="loadTools">刷新</button>
      </div>
      <div v-if="tools.length === 0" class="empty">暂无推荐</div>
      <article v-for="tool in tools" :key="tool.id" class="doc-item">
        <h4>{{ tool.name }} <small>{{ tool.category || '' }}</small></h4>
        <p>{{ tool.description || '暂无描述' }}</p>
        <div class="doc-meta">
          <span>推荐人：{{ tool.recommendedBy || '-' }}</span>
          <span>状态：{{ statusText(tool.status) }}</span>
          <span v-if="tool.reviewRemark">审核备注：{{ tool.reviewRemark }}</span>
          <span>{{ formatDate(tool.createdAt) }}</span>
          <span v-if="tool.reviewedAt">审核时间：{{ formatDate(tool.reviewedAt) }}</span>
          <a v-if="tool.downloadUrl" :href="tool.downloadUrl" target="_blank">下载/官网</a>
          <button
            v-if="tool.status === 'PENDING'"
            type="button"
            class="small-btn"
            @click="reviewTool(tool, 'APPROVED')"
          >
            通过
          </button>
          <button
            v-if="tool.status === 'PENDING'"
            type="button"
            class="small-btn danger-btn"
            @click="reviewTool(tool, 'REJECTED')"
          >
            驳回
          </button>
          <button
            v-if="tool.status === 'REJECTED'"
            type="button"
            class="small-btn"
            @click="reviewTool(tool, 'APPROVED')"
          >
            改为通过
          </button>
          <button
            v-if="tool.status === 'APPROVED'"
            type="button"
            class="small-btn danger-btn"
            @click="reviewTool(tool, 'REJECTED')"
          >
            改为驳回
          </button>
          <button type="button" class="small-btn danger-btn" @click="removeTool(tool)">删除</button>
        </div>
      </article>
      <p v-if="reviewMessage" class="success">{{ reviewMessage }}</p>
      <p v-if="reviewErrorMessage" class="error">{{ reviewErrorMessage }}</p>
    </section>
  </div>
</template>

<script>
import { adminListToolsApi, createToolApi, deleteToolApi, reviewToolApi } from '../api/software'

export default {
  name: 'AdminSoftwareView',
  data() {
    return {
      submitting: false,
      message: '',
      errorMessage: '',
      reviewMessage: '',
      reviewErrorMessage: '',
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
  computed: {
    pendingCount() {
      return this.tools.filter((item) => item.status === 'PENDING').length
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
    statusText(status) {
      if (status === 'APPROVED') return '已通过'
      if (status === 'REJECTED') return '已驳回'
      return '待审核'
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
        await this.loadTools()
      } catch (e) {
        this.errorMessage = e.message || '提交失败'
      } finally {
        this.submitting = false
      }
    },
    async reviewTool(tool, status) {
      this.reviewMessage = ''
      this.reviewErrorMessage = ''
      try {
        await reviewToolApi(tool.id, {
          status,
          reviewRemark: status === 'REJECTED' ? '后台调整为驳回' : (status === 'APPROVED' ? '后台调整为通过' : '后台调整为待审核')
        })
        this.reviewMessage = '审核状态更新成功'
        await this.loadTools()
      } catch (e) {
        this.reviewErrorMessage = e.message || '审核失败'
      }
    },
    async removeTool(tool) {
      const confirmed = window.confirm(`确认删除软件推荐「${tool.name}」吗？`)
      if (!confirmed) {
        return
      }
      this.reviewMessage = ''
      this.reviewErrorMessage = ''
      try {
        await deleteToolApi(tool.id)
        this.reviewMessage = '删除成功'
        await this.loadTools()
      } catch (e) {
        this.reviewErrorMessage = e.message || '删除失败'
      }
    },
    async loadTools() {
      try {
        const res = await adminListToolsApi()
        this.tools = res.data || []
      } catch (e) {
        this.errorMessage = e.message || '加载推荐失败'
      }
    }
  }
}
</script>
