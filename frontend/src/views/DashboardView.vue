<template>
  <div class="dashboard">
    <section class="hero-card">
      <h3>欢迎来到社团文档平台</h3>
      <p>
        这里用于沉淀社团成员在大学中的阶段性经验或积累的资料。
      </p>
    </section>

    <section class="stats-grid">
      <article class="stat-card">
        <h4>内容建议结构</h4>
        <p>加入初心 → 学期收获 → 下一步计划 → 对新人的建议</p>
      </article>
      <article class="stat-card">
        <h4>上传建议</h4>
        <p>正文支持 Markdown，可写标题、列表、代码、链接。</p>
      </article>
      <article class="stat-card">
        <h4>写作建议</h4>
        <p>请使用Markdown语法进行编写。</p>
      </article>
    </section>

    <section class="card">
      <div class="section-header">
        <h3>首页推荐文章</h3>
        <router-link to="/app/articles">查看全站文档列表</router-link>
      </div>
      <div v-if="recommended.length === 0" class="empty">暂无推荐文章，可在后台文章管理中设置推荐。</div>
      <article v-for="doc in recommended" :key="doc.id" class="doc-item clickable-card" @click="openArticle(doc.id)">
        <h4>{{ doc.title }}</h4>
        <p>{{ doc.summary || '无摘要' }}</p>
        <div class="doc-meta">
          <span>作者：{{ doc.authorName }}</span>
          <span>学期：{{ doc.semesterTag || '-' }}</span>
          <span>标签：{{ doc.tagName || '-' }}</span>
          <span>点击：{{ doc.viewCount || 0 }}</span>
          <span>{{ formatDate(doc.updatedAt || doc.createdAt) }}</span>
          <button type="button" class="small-btn like-btn" :class="{ liked: doc.liked }" @click.stop="toggleLike(doc)">
            {{ doc.liked ? '已赞' : '点赞' }} {{ doc.likeCount || 0 }}
          </button>
        </div>
      </article>
      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
    </section>
  </div>
</template>

<script>
import { likeDocumentApi, recommendedDocumentsApi, unlikeDocumentApi } from '../api/document'

export default {
  name: 'DashboardView',
  data() {
    return {
      recommended: [],
      errorMessage: ''
    }
  },
  mounted() {
    this.loadRecommended()
  },
  methods: {
    formatDate(value) {
      if (!value) return '-'
      return new Date(value).toLocaleString('zh-CN')
    },
    async loadRecommended() {
      try {
        const res = await recommendedDocumentsApi(5)
        this.recommended = res.data || []
      } catch (e) {
        this.errorMessage = e.message || '加载推荐文章失败'
      }
    },
    async toggleLike(doc) {
      this.errorMessage = ''
      try {
        const res = doc.liked ? await unlikeDocumentApi(doc.id) : await likeDocumentApi(doc.id)
        const updated = res.data || {}
        doc.liked = !!updated.liked
        doc.likeCount = updated.likeCount || 0
      } catch (e) {
        this.errorMessage = e.message || '点赞操作失败'
      }
    },
    openArticle(id) {
      this.$router.push(`/app/articles/${id}`)
    }
  }
}
</script>
