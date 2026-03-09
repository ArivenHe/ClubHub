<template>
  <div class="documents-page">
    <section class="card">
      <div class="section-header">
        <h3>全站文档列表</h3>
        <button type="button" @click="loadAllDocuments">刷新</button>
      </div>
      <div class="grid-two">
        <label>
          <span>关键词筛选</span>
          <input v-model.trim="keyword" placeholder="按标题/摘要筛选" />
        </label>
        <label>
          <span>标签筛选</span>
          <select v-model="selectedTag">
            <option value="">全部标签</option>
            <option v-for="tag in tags" :key="tag.id" :value="String(tag.id)">{{ tag.name }}</option>
          </select>
        </label>
      </div>
      <p class="muted">文章总数：{{ totalCount }} 篇，当前筛选：{{ filteredCount }} 篇</p>
      <div v-if="filteredDocuments.length === 0" class="empty">暂无数据</div>
      <article v-for="doc in filteredDocuments" :key="`all-${doc.id}`" class="doc-item clickable-card" @click="openArticle(doc.id)">
        <h4>
          {{ doc.title }}
          <span v-if="doc.recommended" class="badge">推荐</span>
        </h4>
        <p>{{ doc.summary || '无摘要' }}</p>
        <div class="doc-meta">
          <span>作者：{{ doc.authorName }}</span>
          <span>学期：{{ doc.semesterTag || '-' }}</span>
          <span>标签：{{ doc.tagName || '-' }}</span>
          <span>字数：{{ doc.wordCount }} 字</span>
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
import { allDocumentsApi, likeDocumentApi, unlikeDocumentApi } from '../api/document'
import { listTagsApi } from '../api/tag'

export default {
  name: 'GlobalArticlesView',
  data() {
    return {
      keyword: '',
      selectedTag: '',
      tags: [],
      documents: [],
      errorMessage: ''
    }
  },
  computed: {
    totalCount() {
      return this.documents.length
    },
    filteredCount() {
      return this.filteredDocuments.length
    },
    filteredDocuments() {
      let results = this.documents
      if (this.selectedTag !== '') {
        results = results.filter((doc) => String(doc.tagId || '') === this.selectedTag)
      }
      if (!this.keyword) {
        return results
      }
      const kw = this.keyword.toLowerCase()
      return results.filter((doc) => {
        const title = doc.title || ''
        const summary = doc.summary || ''
        return title.toLowerCase().includes(kw) || summary.toLowerCase().includes(kw)
      })
    }
  },
  mounted() {
    this.loadAllDocuments()
    this.loadTags()
  },
  methods: {
    formatDate(value) {
      if (!value) return '-'
      return new Date(value).toLocaleString('zh-CN')
    },
    calculateWordCount(markdown) {
      if (!markdown) {
        return 0
      }
      const plainText = String(markdown)
        .replace(/```[\s\S]*?```/g, ' ')
        .replace(/`[^`]*`/g, ' ')
        .replace(/!\[[^\]]*]\([^)]+\)/g, ' ')
        .replace(/\[[^\]]*]\([^)]+\)/g, ' ')
        .replace(/[#>*_~[\]()|!-]/g, ' ')
        .replace(/\r?\n/g, ' ')
        .replace(/\s+/g, '')
      return plainText.length
    },
    async loadTags() {
      try {
        const res = await listTagsApi()
        this.tags = res.data || []
      } catch (e) {
        if (!this.errorMessage) {
          this.errorMessage = e.message || '加载标签失败'
        }
      }
    },
    async loadAllDocuments() {
      this.errorMessage = ''
      try {
        const res = await allDocumentsApi()
        const docs = res.data || []
        this.documents = docs.map((doc) => ({
          ...doc,
          wordCount: this.calculateWordCount(doc.content || '')
        }))
      } catch (e) {
        this.errorMessage = e.message || '加载全站文档失败'
      }
    },
    openArticle(id) {
      this.$router.push(`/app/articles/${id}`)
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
    }
  }
}
</script>
