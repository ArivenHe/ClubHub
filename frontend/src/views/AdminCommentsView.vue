<template>
  <div class="users-page">
    <section class="card form-card">
      <h3>评论筛选</h3>
      <div class="grid-two">
        <label>
          <span>关键词</span>
          <input v-model.trim="filters.keyword" placeholder="评论内容/评论人/文章标题/作者" />
        </label>
        <label>
          <span>文章 ID</span>
          <input v-model.trim="filters.documentId" placeholder="可选，输入数字" />
        </label>
      </div>
      <div class="actions-row">
        <button type="button" @click="loadComments">查询</button>
      </div>
      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
      <p v-if="message" class="success">{{ message }}</p>
    </section>

    <section class="card">
      <div class="section-header">
        <h3>评论管理列表</h3>
        <button type="button" @click="loadComments">刷新</button>
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>文章</th>
            <th>文章作者</th>
            <th>评论人</th>
            <th>评论内容</th>
            <th>更新时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in comments" :key="item.id">
            <td>{{ item.id }}</td>
            <td>
              <div>{{ item.documentTitle || '-' }}</div>
              <div class="muted">#{{ item.documentId || '-' }}</div>
            </td>
            <td>{{ item.documentAuthorName || '-' }}</td>
            <td>{{ item.authorName || '-' }}</td>
            <td class="comment-content-cell">{{ item.content || '-' }}</td>
            <td>{{ formatDate(item.updatedAt || item.createdAt) }}</td>
            <td class="actions-cell">
              <button type="button" class="small-btn danger-btn" @click="removeComment(item)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="comments.length === 0" class="empty">暂无评论数据</div>
    </section>
  </div>
</template>

<script>
import { adminCommentsApi, deleteAdminCommentApi } from '../api/document'

export default {
  name: 'AdminCommentsView',
  data() {
    return {
      filters: {
        keyword: '',
        documentId: ''
      },
      comments: [],
      message: '',
      errorMessage: ''
    }
  },
  mounted() {
    this.loadComments()
  },
  methods: {
    formatDate(value) {
      if (!value) return '-'
      return new Date(value).toLocaleString('zh-CN')
    },
    buildQuery() {
      const query = {}
      if (this.filters.keyword) {
        query.keyword = this.filters.keyword
      }
      if (this.filters.documentId) {
        const id = Number(this.filters.documentId)
        if (!Number.isInteger(id) || id <= 0) {
          throw new Error('文章 ID 必须是正整数')
        }
        query.documentId = id
      }
      return query
    },
    async loadComments() {
      this.message = ''
      this.errorMessage = ''
      try {
        const res = await adminCommentsApi(this.buildQuery())
        this.comments = res.data || []
      } catch (e) {
        this.errorMessage = e.message || '加载评论失败'
      }
    },
    async removeComment(item) {
      const confirmed = window.confirm('确认删除这条评论吗？')
      if (!confirmed) {
        return
      }
      this.errorMessage = ''
      try {
        await deleteAdminCommentApi(item.id)
        this.message = '评论删除成功'
        await this.loadComments()
      } catch (e) {
        this.errorMessage = e.message || '评论删除失败'
      }
    }
  }
}
</script>
