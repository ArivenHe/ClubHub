<template>
  <div class="users-page">
    <section class="card form-card">
      <h3>文章筛选</h3>
      <div class="grid-three">
        <label>
          <span>关键词</span>
          <input v-model.trim="filters.keyword" placeholder="标题/摘要/正文" />
        </label>
        <label>
          <span>状态</span>
          <select v-model="filters.status">
            <option value="">全部</option>
            <option value="PUBLISHED">PUBLISHED</option>
            <option value="DRAFT">DRAFT</option>
          </select>
        </label>
        <label>
          <span>推荐</span>
          <select v-model="filters.recommended">
            <option value="">全部</option>
            <option value="true">已推荐</option>
            <option value="false">未推荐</option>
          </select>
        </label>
      </div>
      <div class="actions-row">
        <button type="button" @click="loadArticles">查询</button>
      </div>
      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
      <p v-if="message" class="success">{{ message }}</p>
    </section>

    <section class="card">
      <div class="section-header">
        <h3>文章管理列表</h3>
        <button type="button" @click="loadArticles">刷新</button>
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>标题</th>
            <th>作者</th>
            <th>标签</th>
            <th>状态</th>
            <th>推荐</th>
            <th>点击</th>
            <th>点赞</th>
            <th>更新时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in articles" :key="item.id">
            <td>{{ item.id }}</td>
            <td>
              {{ item.title }}
            </td>
            <td>{{ item.authorName }}</td>
            <td>{{ item.tagName || '-' }}</td>
            <td>{{ item.status }}</td>
            <td>{{ item.recommended ? '是' : '否' }}</td>
            <td>{{ item.viewCount || 0 }}</td>
            <td>{{ item.likeCount || 0 }}</td>
            <td>{{ formatDate(item.updatedAt || item.createdAt) }}</td>
            <td class="actions-cell">
              <button type="button" class="small-btn" @click="toggleStatus(item)">
                {{ item.status === 'PUBLISHED' ? '转草稿' : '发布' }}
              </button>
              <button type="button" class="small-btn" @click="toggleRecommended(item)">
                {{ item.recommended ? '取消推荐' : '设为推荐' }}
              </button>
              <button type="button" class="small-btn danger-btn" @click="removeArticle(item)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="articles.length === 0" class="empty">暂无数据</div>
    </section>
  </div>
</template>

<script>
import {
  adminArticlesApi,
  deleteArticleApi,
  updateArticleRecommendedApi,
  updateArticleStatusApi
} from '../api/document'

export default {
  name: 'AdminArticlesView',
  data() {
    return {
      filters: {
        keyword: '',
        status: '',
        recommended: ''
      },
      articles: [],
      message: '',
      errorMessage: ''
    }
  },
  mounted() {
    this.loadArticles()
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
      if (this.filters.status) {
        query.status = this.filters.status
      }
      if (this.filters.recommended !== '') {
        query.recommended = this.filters.recommended === 'true'
      }
      return query
    },
    async loadArticles() {
      this.message = ''
      this.errorMessage = ''
      try {
        const res = await adminArticlesApi(this.buildQuery())
        this.articles = res.data || []
      } catch (e) {
        this.errorMessage = e.message || '加载文章失败'
      }
    },
    async toggleStatus(item) {
      const targetStatus = item.status === 'PUBLISHED' ? 'DRAFT' : 'PUBLISHED'
      this.errorMessage = ''
      try {
        await updateArticleStatusApi(item.id, targetStatus)
        this.message = '状态更新成功'
        this.loadArticles()
      } catch (e) {
        this.errorMessage = e.message || '状态更新失败'
      }
    },
    async toggleRecommended(item) {
      this.errorMessage = ''
      try {
        await updateArticleRecommendedApi(item.id, !item.recommended)
        this.message = '推荐状态更新成功'
        this.loadArticles()
      } catch (e) {
        this.errorMessage = e.message || '推荐状态更新失败'
      }
    },
    async removeArticle(item) {
      const confirmed = window.confirm(`确认删除文章「${item.title}」吗？`)
      if (!confirmed) {
        return
      }
      this.errorMessage = ''
      try {
        await deleteArticleApi(item.id)
        this.message = '删除成功'
        this.loadArticles()
      } catch (e) {
        this.errorMessage = e.message || '删除失败'
      }
    }
  }
}
</script>
