<template>
  <div class="users-page">
    <section class="card form-card" v-if="canManage">
      <h3>后台注册账号</h3>
      <p class="muted">前台不开放注册。请在后台创建账号并分配角色。</p>
      <div class="grid-three">
        <label>
          <span>用户名</span>
          <input v-model.trim="createForm.username" placeholder="4-50位，字母数字下划线" />
        </label>
        <label>
          <span>姓名</span>
          <input v-model.trim="createForm.realName" placeholder="请输入真实姓名" />
        </label>
        <label>
          <span>学号（可选）</span>
          <input v-model.trim="createForm.studentNo" placeholder="例如：20250001" />
        </label>
      </div>
      <div class="grid-two">
        <label>
          <span>初始密码</span>
          <input v-model.trim="createForm.password" type="password" placeholder="至少6位" />
        </label>
        <label>
          <span>角色</span>
          <select v-model="createForm.roleCode">
            <option value="">MEMBER（默认）</option>
            <option v-for="role in roles" :key="role.id" :value="role.code">{{ role.code }}</option>
          </select>
        </label>
      </div>
      <button type="button" :disabled="submittingCreate" @click="submitCreate">
        {{ submittingCreate ? '创建中...' : '创建账号' }}
      </button>
    </section>

    <section class="card form-card" v-if="canImport">
      <h3>Excel 批量注册账号</h3>
      <p class="muted">Excel 第一行是表头，数据从第二行开始，列顺序为：username、realName、studentNo。</p>
      <div class="grid-two">
        <label>
          <span>默认角色编码</span>
          <input v-model.trim="importRoleCode" placeholder="MEMBER" />
        </label>
        <label>
          <span>选择 Excel 文件</span>
          <input type="file" accept=".xlsx,.xls" @change="onFileChange" />
        </label>
      </div>
      <button :disabled="submitting || !file" @click="submitImport">{{ submitting ? '导入中...' : '开始导入' }}</button>
      <div v-if="importResult" class="import-result">
        <p>成功：{{ importResult.successCount }}，跳过：{{ importResult.skipCount }}</p>
        <ul>
          <li v-for="(reason, idx) in importResult.skipReasons" :key="idx">{{ reason }}</li>
        </ul>
      </div>
    </section>

    <section class="card" v-if="canManage">
      <div class="section-header">
        <h3>用户管理</h3>
        <button type="button" @click="loadData">刷新</button>
      </div>
      <div class="grid-three">
        <label>
          <span>关键词</span>
          <input v-model.trim="filters.keyword" placeholder="用户名/姓名/学号" />
        </label>
        <label>
          <span>状态</span>
          <select v-model="filters.enabled">
            <option value="">全部</option>
            <option value="true">启用</option>
            <option value="false">禁用</option>
          </select>
        </label>
        <label>
          <span>角色</span>
          <select v-model="filters.roleCode">
            <option value="">全部</option>
            <option v-for="role in roles" :key="`filter-${role.id}`" :value="role.code">{{ role.code }}</option>
          </select>
        </label>
      </div>
      <div class="actions-cell">
        <button type="button" class="small-btn" @click="loadUsers">筛选</button>
        <button type="button" class="small-btn" @click="resetFilters">重置</button>
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>姓名</th>
            <th>学号</th>
            <th>角色</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in users" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.username }}</td>
            <td>{{ item.realName }}</td>
            <td>{{ item.studentNo || '-' }}</td>
            <td>{{ formatRoles(item.roles) }}</td>
            <td>{{ item.enabled ? '启用' : '禁用' }}</td>
            <td class="actions-cell">
              <button type="button" class="small-btn" @click="toggleStatus(item)">
                {{ item.enabled ? '禁用' : '启用' }}
              </button>
              <select v-model="roleDraft[item.id]" class="role-select">
                <option value="">选择角色</option>
                <option v-for="role in roles" :key="role.id" :value="role.code">{{ role.code }}</option>
              </select>
              <button type="button" class="small-btn" @click="changeRole(item)">改角色</button>
              <button type="button" class="small-btn" @click="resetPwd(item)">重置密码</button>
            </td>
          </tr>
        </tbody>
      </table>
    </section>

    <section class="card" v-else-if="canImport">
      <h3>用户管理</h3>
      <p class="muted">你当前只有批量导入权限，无法查看或修改用户列表。</p>
    </section>

    <p v-if="message" class="success">{{ message }}</p>
    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
  </div>
</template>

<script>
import { hasPermission } from '../utils/auth'
import {
  createUserApi,
  importUsersApi,
  listRolesApi,
  listUsersApi,
  resetUserPasswordApi,
  updateUserRoleApi,
  updateUserStatusApi
} from '../api/user'

export default {
  name: 'AdminUsersView',
  data() {
    return {
      file: null,
      importRoleCode: 'MEMBER',
      users: [],
      roles: [],
      roleDraft: {},
      filters: {
        keyword: '',
        enabled: '',
        roleCode: ''
      },
      createForm: {
        username: '',
        realName: '',
        studentNo: '',
        password: '',
        roleCode: 'MEMBER'
      },
      submitting: false,
      submittingCreate: false,
      message: '',
      errorMessage: '',
      importResult: null
    }
  },
  computed: {
    canImport() {
      return hasPermission('user:import')
    },
    canManage() {
      return hasPermission('user:manage')
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    resetMessages() {
      this.message = ''
      this.errorMessage = ''
    },
    formatRoles(roles) {
      if (!roles || roles.length === 0) {
        return '-'
      }
      return roles.join(', ')
    },
    onFileChange(event) {
      const files = event.target.files || []
      this.file = files[0] || null
    },
    async submitImport() {
      if (!this.file) {
        this.errorMessage = '请选择 Excel 文件'
        return
      }

      this.submitting = true
      this.resetMessages()
      this.importResult = null

      const formData = new FormData()
      formData.append('file', this.file)

      try {
        const res = await importUsersApi(formData, this.importRoleCode)
        this.importResult = res.data
        this.message = '导入完成'
        if (this.canManage) {
          await this.loadUsers()
        }
      } catch (e) {
        this.errorMessage = e.message || '导入失败'
      } finally {
        this.submitting = false
      }
    },
    async submitCreate() {
      if (!this.createForm.username || !this.createForm.realName || !this.createForm.password) {
        this.errorMessage = '请补全用户名、姓名、密码'
        return
      }
      if (this.createForm.password.length < 6) {
        this.errorMessage = '密码至少6位'
        return
      }

      this.submittingCreate = true
      this.resetMessages()
      try {
        await createUserApi(
          {
            username: this.createForm.username,
            realName: this.createForm.realName,
            studentNo: this.createForm.studentNo || null,
            password: this.createForm.password
          },
          this.createForm.roleCode || 'MEMBER'
        )
        this.message = '创建账号成功'
        this.createForm = {
          username: '',
          realName: '',
          studentNo: '',
          password: '',
          roleCode: 'MEMBER'
        }
        await this.loadUsers()
      } catch (e) {
        this.errorMessage = e.message || '创建账号失败'
      } finally {
        this.submittingCreate = false
      }
    },
    async loadData() {
      this.resetMessages()
      const tasks = []
      if (this.canManage) {
        tasks.push(this.loadUsers())
        tasks.push(this.loadRoles())
      }
      if (tasks.length > 0) {
        await Promise.all(tasks)
      }
    },
    async loadUsers() {
      if (!this.canManage) {
        return
      }
      try {
        const params = {}
        if (this.filters.keyword) {
          params.keyword = this.filters.keyword
        }
        if (this.filters.enabled !== '') {
          params.enabled = this.filters.enabled === 'true'
        }
        if (this.filters.roleCode) {
          params.roleCode = this.filters.roleCode
        }
        const res = await listUsersApi(params)
        this.users = res.data || []
        const drafts = {}
        this.users.forEach((user) => {
          drafts[user.id] = (user.roles && user.roles[0]) || ''
        })
        this.roleDraft = drafts
      } catch (e) {
        this.errorMessage = e.message || '加载用户失败'
      }
    },
    async resetFilters() {
      this.filters = {
        keyword: '',
        enabled: '',
        roleCode: ''
      }
      await this.loadUsers()
    },
    async loadRoles() {
      if (!this.canManage) {
        return
      }
      try {
        const res = await listRolesApi()
        this.roles = res.data || []
      } catch (e) {
        this.errorMessage = e.message || '加载角色失败'
      }
    },
    async toggleStatus(user) {
      this.resetMessages()
      try {
        await updateUserStatusApi(user.id, !user.enabled)
        this.message = '状态更新成功'
        this.loadUsers()
      } catch (e) {
        this.errorMessage = e.message || '状态更新失败'
      }
    },
    async changeRole(user) {
      const roleCode = this.roleDraft[user.id]
      if (!roleCode) {
        this.errorMessage = '请先选择角色'
        return
      }
      this.resetMessages()
      try {
        await updateUserRoleApi(user.id, roleCode)
        this.message = '角色更新成功'
        this.loadUsers()
      } catch (e) {
        this.errorMessage = e.message || '角色更新失败'
      }
    },
    async resetPwd(user) {
      const newPassword = window.prompt(`请输入用户 ${user.username} 的新密码（至少6位）：`, '123456')
      if (!newPassword) {
        return
      }
      this.resetMessages()
      try {
        await resetUserPasswordApi(user.id, newPassword)
        this.message = '密码重置成功'
      } catch (e) {
        this.errorMessage = e.message || '密码重置失败'
      }
    }
  }
}
</script>
