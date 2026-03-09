<template>
  <div class="article-detail-page">
    <section class="card article-main">
      <div class="section-header">
        <h3>{{ article.title || '文章详情' }}</h3>
        <router-link to="/app/articles">返回列表</router-link>
      </div>

      <div v-if="loading" class="empty">加载中...</div>
      <template v-else>
        <p class="muted">{{ article.summary || '无摘要' }}</p>
        <div class="doc-meta">
          <span>作者：{{ article.authorName || '-' }}</span>
          <span>学期：{{ article.semesterTag || '-' }}</span>
          <span>标签：{{ article.tagName || '-' }}</span>
          <span>状态：{{ article.status || '-' }}</span>
          <span>字数：{{ articleWordCount }} 字</span>
          <span>点击：{{ article.viewCount || 0 }}</span>
          <span>{{ formatDate(article.updatedAt || article.createdAt) }}</span>
          <button type="button" class="small-btn like-btn" :class="{ liked: article.liked }" @click="toggleLike">
            {{ article.liked ? '已赞' : '点赞' }} {{ article.likeCount || 0 }}
          </button>
        </div>
        <div class="markdown-body article-body" v-html="renderMd(article.content)"></div>

        <section class="comment-section">
          <div class="section-header">
            <h4>评论区</h4>
            <span class="muted">共 {{ comments.length }} 条</span>
          </div>
          <label class="comment-editor">
            <span>发表评论</span>
            <markdown-editor
              v-model="commentContent"
              :rows="6"
              :maxlength="2000"
              placeholder="写下你的想法（支持 Markdown）"
            />
          </label>
          <div class="actions-row">
            <button type="button" :disabled="commentSubmitting" @click="submitComment">
              {{ commentSubmitting ? '提交中...' : '发表评论' }}
            </button>
          </div>

          <p v-if="commentMessage" class="success">{{ commentMessage }}</p>
          <p v-if="commentErrorMessage" class="error">{{ commentErrorMessage }}</p>

          <div v-if="commentsLoading" class="empty">评论加载中...</div>
          <div v-else-if="comments.length === 0" class="empty">还没有评论，来抢沙发吧。</div>
          <article v-for="item in comments" :key="`comment-${item.id}`" class="comment-item">
            <div class="comment-head">
              <strong>{{ item.authorName || '未知用户' }}</strong>
              <span class="muted">{{ formatDate(item.createdAt) }}</span>
            </div>
            <div class="markdown-body comment-body" v-html="renderMd(item.content)"></div>
            <div class="actions-cell" v-if="item.canDelete">
              <button type="button" class="small-btn danger-btn" @click="removeComment(item)">删除</button>
            </div>
          </article>
        </section>
      </template>

      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
    </section>

    <aside class="card article-outline">
      <h3>文章大纲</h3>
      <div v-if="headings.length === 0" class="empty">当前文章没有 Markdown 标题。</div>
      <ul v-else class="outline-list">
        <li v-for="(item, index) in headings" :key="`${item.id}-${index}`" :style="{ paddingLeft: `${(item.level - 1) * 12}px` }">
          <a href="#" @click.prevent="scrollToHeading(item.id)">{{ item.text || `标题 ${index + 1}` }}</a>
        </li>
      </ul>
    </aside>
  </div>
</template>

<script>
import {
  createDocumentCommentApi,
  deleteDocumentCommentApi,
  documentDetailApi,
  likeDocumentApi,
  listDocumentCommentsApi,
  unlikeDocumentApi
} from '../api/document'
import MarkdownEditor from '../components/MarkdownEditor.vue'
import { extractMarkdownHeadings, renderMarkdown } from '../utils/markdown'

export default {
  name: 'ArticleDetailView',
  components: {
    MarkdownEditor
  },
  data() {
    return {
      loading: false,
      commentsLoading: false,
      commentSubmitting: false,
      errorMessage: '',
      commentErrorMessage: '',
      commentMessage: '',
      article: {},
      headings: [],
      comments: [],
      commentContent: ''
    }
  },
  computed: {
    articleWordCount() {
      return this.calculateWordCount(this.article.content || '')
    }
  },
  mounted() {
    this.loadDetail()
  },
  watch: {
    '$route.params.id'() {
      this.loadDetail()
    }
  },
  methods: {
    renderMd(value) {
      return renderMarkdown(value)
    },
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
    async loadDetail() {
      const id = this.$route.params.id
      if (!id) {
        this.errorMessage = '文章 ID 不存在'
        return
      }

      this.loading = true
      this.errorMessage = ''
      this.commentErrorMessage = ''
      this.commentMessage = ''
      this.comments = []
      try {
        const detailRes = await documentDetailApi(id)
        this.article = detailRes.data || {}
        this.headings = extractMarkdownHeadings(this.article.content || '')
        await this.loadComments(id)
      } catch (e) {
        this.errorMessage = e.message || '加载文章详情失败'
      } finally {
        this.loading = false
      }
    },
    scrollToHeading(id) {
      const target = document.getElementById(id)
      if (!target) {
        return
      }
      target.scrollIntoView({ behavior: 'smooth', block: 'start' })
    },
    async toggleLike() {
      if (!this.article.id) {
        return
      }
      this.errorMessage = ''
      try {
        const res = this.article.liked
          ? await unlikeDocumentApi(this.article.id)
          : await likeDocumentApi(this.article.id)
        const updated = res.data || {}
        this.article.liked = !!updated.liked
        this.article.likeCount = updated.likeCount || 0
      } catch (e) {
        this.errorMessage = e.message || '点赞操作失败'
      }
    },
    async loadComments(documentId = this.article.id) {
      if (!documentId) {
        return
      }
      this.commentsLoading = true
      this.commentErrorMessage = ''
      try {
        const res = await listDocumentCommentsApi(documentId)
        this.comments = res.data || []
      } catch (e) {
        this.commentErrorMessage = e.message || '评论加载失败'
      } finally {
        this.commentsLoading = false
      }
    },
    async submitComment() {
      if (!this.article.id) {
        return
      }
      const content = (this.commentContent || '').trim()
      if (!content) {
        this.commentErrorMessage = '评论内容不能为空'
        return
      }
      this.commentSubmitting = true
      this.commentErrorMessage = ''
      this.commentMessage = ''
      try {
        await createDocumentCommentApi(this.article.id, {
          content
        })
        this.commentContent = ''
        this.commentMessage = '评论发布成功'
        await this.loadComments()
      } catch (e) {
        this.commentErrorMessage = e.message || '评论发布失败'
      } finally {
        this.commentSubmitting = false
      }
    },
    async removeComment(comment) {
      const confirmed = window.confirm('确认删除这条评论吗？')
      if (!confirmed) {
        return
      }
      this.commentErrorMessage = ''
      this.commentMessage = ''
      try {
        await deleteDocumentCommentApi(comment.id)
        this.commentMessage = '评论删除成功'
        await this.loadComments()
      } catch (e) {
        this.commentErrorMessage = e.message || '评论删除失败'
      }
    }
  }
}
</script>
