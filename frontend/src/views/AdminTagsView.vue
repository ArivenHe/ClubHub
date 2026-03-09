<template>
  <div class="users-page">
    <section class="card form-card">
      <h3>{{ editingId ? '编辑标签' : '新增标签' }}</h3>
      <form @submit.prevent="submitTag">
        <div class="grid-two">
          <label>
            <span>标签名</span>
            <input v-model.trim="form.name" maxlength="50" required placeholder="例如：新生指南" />
          </label>
          <label>
            <span>描述</span>
            <input v-model.trim="form.description" maxlength="255" placeholder="可选，简要说明标签用途" />
          </label>
        </div>
        <div class="actions-cell">
          <button type="submit" :disabled="submitting">{{ submitting ? '提交中...' : (editingId ? '更新标签' : '新增标签') }}</button>
          <button v-if="editingId" type="button" class="small-btn" @click="resetForm">取消编辑</button>
        </div>
      </form>
      <p v-if="message" class="success">{{ message }}</p>
      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
    </section>

    <section class="card">
      <div class="section-header">
        <h3>标签管理</h3>
        <button type="button" @click="loadTags">刷新</button>
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>标签名</th>
            <th>描述</th>
            <th>创建人</th>
            <th>创建时间</th>
            <th>更新时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in tags" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.name }}</td>
            <td>{{ item.description || '-' }}</td>
            <td>{{ item.creatorName || '-' }}</td>
            <td>{{ formatDate(item.createdAt) }}</td>
            <td>{{ formatDate(item.updatedAt) }}</td>
            <td class="actions-cell">
              <button type="button" class="small-btn" @click="beginEdit(item)">编辑</button>
              <button type="button" class="small-btn danger-btn" @click="removeTag(item)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="tags.length === 0" class="empty">暂无标签</div>
    </section>
  </div>
</template>

<script>
import {
  adminCreateTagApi,
  adminDeleteTagApi,
  adminListTagsApi,
  adminUpdateTagApi
} from '../api/tag'

export default {
  name: 'AdminTagsView',
  data() {
    return {
      tags: [],
      editingId: null,
      submitting: false,
      message: '',
      errorMessage: '',
      form: {
        name: '',
        description: ''
      }
    }
  },
  mounted() {
    this.loadTags()
  },
  methods: {
    formatDate(value) {
      if (!value) return '-'
      return new Date(value).toLocaleString('zh-CN')
    },
    resetForm() {
      this.editingId = null
      this.form = {
        name: '',
        description: ''
      }
    },
    beginEdit(tag) {
      this.editingId = tag.id
      this.form.name = tag.name || ''
      this.form.description = tag.description || ''
      this.message = `正在编辑标签：${tag.name}`
      this.errorMessage = ''
    },
    async submitTag() {
      if (!this.form.name) {
        this.errorMessage = '标签名不能为空'
        return
      }
      this.submitting = true
      this.message = ''
      this.errorMessage = ''
      try {
        const payload = {
          name: this.form.name,
          description: this.form.description || null
        }
        if (this.editingId) {
          await adminUpdateTagApi(this.editingId, payload)
          this.message = '标签更新成功'
        } else {
          await adminCreateTagApi(payload)
          this.message = '标签新增成功'
        }
        this.resetForm()
        await this.loadTags()
      } catch (e) {
        this.errorMessage = e.message || '提交失败'
      } finally {
        this.submitting = false
      }
    },
    async loadTags() {
      try {
        const res = await adminListTagsApi()
        this.tags = res.data || []
      } catch (e) {
        this.errorMessage = e.message || '加载标签失败'
      }
    },
    async removeTag(tag) {
      const confirmed = window.confirm(`确认删除标签「${tag.name}」吗？`)
      if (!confirmed) {
        return
      }
      this.message = ''
      this.errorMessage = ''
      try {
        await adminDeleteTagApi(tag.id)
        this.message = '删除成功'
        if (this.editingId === tag.id) {
          this.resetForm()
        }
        await this.loadTags()
      } catch (e) {
        this.errorMessage = e.message || '删除失败'
      }
    }
  }
}
</script>
