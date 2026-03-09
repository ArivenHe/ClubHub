<template>
  <div class="documents-page">
    <section class="card form-card" v-if="canUpload">
      <h3>{{ editingId ? '编辑我的文档（支持 Markdown）' : '上传文档（支持 Markdown）' }}</h3>
      <form @submit.prevent="submitDocument">
        <div class="grid-three">
          <label>
            <span>标题</span>
            <input v-model.trim="form.title" maxlength="120" required placeholder="例如：大一上社团阶段总结" />
          </label>
          <label>
            <span>学期标记</span>
            <input v-model.trim="form.semesterTag" maxlength="50" placeholder="例如：2025-2026-1" />
          </label>
          <label>
            <span>标签（可选）</span>
            <select v-model="form.tagId">
              <option :value="null">不设置标签</option>
              <option v-for="tag in tags" :key="tag.id" :value="tag.id">{{ tag.name }}</option>
            </select>
            <div class="actions-cell" v-if="canCreateTag">
              <button type="button" class="small-btn" @click="quickCreateTag">新建标签</button>
            </div>
          </label>
        </div>
        <label>
          <span>摘要</span>
          <input v-model.trim="form.summary" maxlength="500" placeholder="一句话总结这一学期的变化" />
        </label>
        <label>
          <span>正文（Markdown）</span>
          <markdown-editor
            v-model="form.content"
            :rows="14"
            placeholder="# 标题\n\n- 列出收获\n- 规划下学期\n\n可以用 **加粗**、`代码`、[链接](https://example.com)"
          />
        </label>
        <label>
          <span>从本地导入 Markdown 文件</span>
          <input type="file" accept=".md,.markdown,text/markdown,text/plain" @change="onMarkdownFileChange" />
        </label>
        <div class="actions-cell">
          <button :disabled="submitting" type="submit">
            {{ submitting ? (editingId ? '更新中...' : '提交中...') : (editingId ? '更新文档' : '提交文档') }}
          </button>
          <button v-if="editingId" type="button" class="small-btn" @click="cancelEdit">取消编辑</button>
        </div>
      </form>
      <p v-if="message" class="success">{{ message }}</p>
      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
    </section>

    <section class="card">
      <div class="section-header">
        <h3>我的文档</h3>
        <button type="button" @click="loadMyDocuments">刷新</button>
      </div>
      <div v-if="myDocuments.length === 0" class="empty">暂无数据</div>
      <article v-for="doc in myDocuments" :key="doc.id" class="doc-item clickable-card" @click="openArticle(doc.id)">
        <h4>
          {{ doc.title }}
          <span v-if="doc.recommended" class="badge">推荐</span>
        </h4>
        <p>{{ doc.summary || '无摘要' }}</p>
        <div class="doc-meta">
          <span>学期：{{ doc.semesterTag || '-' }}</span>
          <span>标签：{{ doc.tagName || '-' }}</span>
          <span>状态：{{ doc.status }}</span>
          <span>点击：{{ doc.viewCount || 0 }}</span>
          <span>{{ formatDate(doc.updatedAt || doc.createdAt) }}</span>
          <button type="button" class="small-btn like-btn" :class="{ liked: doc.liked }" @click.stop="toggleLike(doc)">
            {{ doc.liked ? '已赞' : '点赞' }} {{ doc.likeCount || 0 }}
          </button>
          <button v-if="canUpload" type="button" class="small-btn" @click.stop="startEdit(doc)">编辑</button>
          <button type="button" class="small-btn danger-btn" @click.stop="deleteMyArticle(doc)">删除</button>
        </div>
      </article>
    </section>
  </div>
</template>

<script>
import MarkdownEditor from '../components/MarkdownEditor.vue'
import {
  createDocumentApi,
  deleteMyDocumentApi,
  likeDocumentApi,
  myDocumentsApi,
  unlikeDocumentApi,
  updateMyDocumentApi
} from '../api/document'
import { createTagApi, listTagsApi } from '../api/tag'
import { extractMarkdownHeadings } from '../utils/markdown'
import { hasPermission } from '../utils/auth'
import { showConfirm, showPrompt } from '../utils/dialog'

export default {
  name: 'DocumentsView',
  components: {
    MarkdownEditor
  },
  data() {
    return {
      submitting: false,
      editingId: null,
      message: '',
      errorMessage: '',
      myDocuments: [],
      tags: [],
      form: {
        title: '',
        summary: '',
        content: '',
        semesterTag: '',
        tagId: null,
        status: 'PUBLISHED'
      }
    }
  },
  computed: {
    canUpload() {
      return hasPermission('doc:upload')
    },
    canCreateTag() {
      return hasPermission('tag:create')
    }
  },
  mounted() {
    this.loadMyDocuments()
    this.loadTags()
  },
  methods: {
    formatDate(value) {
      if (!value) return '-'
      return new Date(value).toLocaleString('zh-CN')
    },
    async onMarkdownFileChange(event) {
      const files = event.target.files || []
      const file = files[0]
      if (!file) {
        return
      }
      const fileName = file.name || ''
      if (!/\.md$|\.markdown$/i.test(fileName)) {
        this.errorMessage = '请上传 .md 或 .markdown 文件'
        return
      }
      try {
        const text = await file.text()
        this.form.content = text
        if (!this.form.title) {
          const headings = extractMarkdownHeadings(text)
          if (headings.length > 0 && headings[0].text) {
            this.form.title = headings[0].text
          } else {
            this.form.title = fileName.replace(/\.[^.]+$/, '')
          }
        }
        this.message = `已导入 ${fileName}`
        this.errorMessage = ''
      } catch (e) {
        this.errorMessage = '读取 Markdown 文件失败'
      } finally {
        event.target.value = ''
      }
    },
    async submitDocument() {
      if (!this.form.title) {
        this.errorMessage = '标题不能为空'
        return
      }

      this.submitting = true
      this.message = ''
      this.errorMessage = ''

      try {
        const payload = {
          title: this.form.title,
          summary: this.form.summary,
          content: this.form.content,
          semesterTag: this.form.semesterTag,
          tagId: this.form.tagId,
          status: this.form.status
        }
        if (this.editingId) {
          await updateMyDocumentApi(this.editingId, payload)
          this.message = '文档更新成功'
        } else {
          await createDocumentApi(payload)
          this.message = '文档提交成功'
        }
        this.resetForm()
        await this.loadMyDocuments()
      } catch (e) {
        this.errorMessage = e.message || (this.editingId ? '更新失败' : '提交失败')
      } finally {
        this.submitting = false
      }
    },
    startEdit(doc) {
      this.editingId = doc.id
      this.form.title = doc.title || ''
      this.form.summary = doc.summary || ''
      this.form.content = doc.content || ''
      this.form.semesterTag = doc.semesterTag || ''
      this.form.tagId = doc.tagId || null
      this.form.status = doc.status || 'PUBLISHED'
      this.message = `正在编辑：${doc.title}`
      this.errorMessage = ''
    },
    cancelEdit() {
      this.resetForm()
      this.message = '已取消编辑'
      this.errorMessage = ''
    },
    resetForm() {
      this.editingId = null
      this.form.title = ''
      this.form.summary = ''
      this.form.content = ''
      this.form.semesterTag = ''
      this.form.tagId = null
      this.form.status = 'PUBLISHED'
    },
    async loadTags() {
      try {
        const res = await listTagsApi()
        this.tags = res.data || []
      } catch (e) {
        this.errorMessage = e.message || '加载标签失败'
      }
    },
    async quickCreateTag() {
      const name = await showPrompt({
        title: '新建标签',
        message: '请输入新标签名称（最多50字）',
        placeholder: '标签名称',
        validator: (value) => {
          const trimmed = value.trim()
          if (!trimmed) {
            return '标签名称不能为空'
          }
          if (trimmed.length > 50) {
            return '标签名称不能超过50字'
          }
          return ''
        }
      })
      if (name === null) {
        return
      }
      const description = await showPrompt({
        title: '新建标签',
        message: '请输入标签描述（可选）',
        placeholder: '标签描述',
        initialValue: ''
      })
      this.errorMessage = ''
      this.message = ''
      try {
        const res = await createTagApi({
          name: name.trim(),
          description: description && description.trim() ? description.trim() : null
        })
        const created = res.data || null
        await this.loadTags()
        if (created && created.id) {
          this.form.tagId = created.id
        }
        this.message = '标签创建成功'
      } catch (e) {
        this.errorMessage = e.message || '标签创建失败'
      }
    },
    async loadMyDocuments() {
      try {
        const res = await myDocumentsApi()
        this.myDocuments = res.data || []
      } catch (e) {
        this.errorMessage = e.message || '加载我的文档失败'
      }
    },
    openArticle(id) {
      this.$router.push(`/app/articles/${id}`)
    },
    async deleteMyArticle(doc) {
      const confirmed = await showConfirm({
        title: '确认删除',
        message: `确认删除文章「${doc.title}」吗？`,
        confirmText: '删除',
        cancelText: '取消'
      })
      if (!confirmed) {
        return
      }
      this.errorMessage = ''
      this.message = ''
      try {
        await deleteMyDocumentApi(doc.id)
        this.message = '删除成功'
        if (this.editingId === doc.id) {
          this.resetForm()
        }
        await this.loadMyDocuments()
      } catch (e) {
        this.errorMessage = e.message || '删除失败'
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
    }
  }
}
</script>
