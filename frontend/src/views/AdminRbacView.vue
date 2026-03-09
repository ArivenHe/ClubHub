<template>
  <div class="users-page">
    <section class="card form-card">
      <div class="section-header">
        <h3>权限管理</h3>
        <button type="button" @click="loadPermissions">刷新</button>
      </div>
      <div class="grid-two">
        <label>
          <span>筛选权限</span>
          <input v-model.trim="permissionKeyword" placeholder="按编码/名称筛选" />
        </label>
        <label>
          <span>&nbsp;</span>
          <button type="button" class="small-btn" @click="loadPermissions">查询</button>
        </label>
      </div>
      <form @submit.prevent="submitPermission">
        <div class="grid-two">
          <label>
            <span>权限编码</span>
            <input v-model.trim="permissionForm.code" maxlength="80" required placeholder="例如：doc:read" />
          </label>
          <label>
            <span>权限名称</span>
            <input v-model.trim="permissionForm.name" maxlength="80" required placeholder="例如：查看文档" />
          </label>
        </div>
        <div class="actions-cell">
          <button type="submit" :disabled="submittingPermission">
            {{ submittingPermission ? '提交中...' : (editingPermissionId ? '更新权限' : '新增权限') }}
          </button>
          <button v-if="editingPermissionId" type="button" class="small-btn" @click="resetPermissionForm">取消编辑</button>
        </div>
      </form>
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>编码</th>
            <th>名称</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in permissions" :key="`perm-${item.id}`">
            <td>{{ item.id }}</td>
            <td>{{ item.code }}</td>
            <td>{{ item.name }}</td>
            <td class="actions-cell">
              <button type="button" class="small-btn" @click="editPermission(item)">编辑</button>
              <button type="button" class="small-btn danger-btn" @click="removePermission(item)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="permissions.length === 0" class="empty">暂无权限数据</div>
    </section>

    <section class="card form-card">
      <div class="section-header">
        <h3>角色管理</h3>
        <button type="button" @click="loadRoles">刷新</button>
      </div>
      <div class="grid-two">
        <label>
          <span>筛选角色</span>
          <input v-model.trim="roleKeyword" placeholder="按编码/名称筛选" />
        </label>
        <label>
          <span>&nbsp;</span>
          <button type="button" class="small-btn" @click="loadRoles">查询</button>
        </label>
      </div>
      <form @submit.prevent="submitRole">
        <div class="grid-three">
          <label>
            <span>角色编码</span>
            <input v-model.trim="roleForm.code" maxlength="50" required placeholder="例如：EDITOR" />
          </label>
          <label>
            <span>角色名称</span>
            <input v-model.trim="roleForm.name" maxlength="50" required placeholder="例如：编辑" />
          </label>
          <label>
            <span>描述</span>
            <input v-model.trim="roleForm.description" maxlength="255" placeholder="可选" />
          </label>
        </div>
        <div class="permission-checklist">
          <label v-for="perm in permissions" :key="`role-check-${perm.id}`" class="check-item">
            <input type="checkbox" :checked="hasPermission(perm.id)" @change="togglePermission(perm.id)" />
            <span>{{ perm.code }}</span>
          </label>
        </div>
        <div class="actions-cell">
          <button type="submit" :disabled="submittingRole">
            {{ submittingRole ? '提交中...' : (editingRoleId ? '更新角色' : '新增角色') }}
          </button>
          <button v-if="editingRoleId" type="button" class="small-btn" @click="resetRoleForm">取消编辑</button>
        </div>
      </form>

      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>编码</th>
            <th>名称</th>
            <th>描述</th>
            <th>权限</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in roles" :key="`role-${item.id}`">
            <td>{{ item.id }}</td>
            <td>{{ item.code }}</td>
            <td>{{ item.name }}</td>
            <td>{{ item.description || '-' }}</td>
            <td>{{ formatPermissionCodes(item.permissionCodes) }}</td>
            <td class="actions-cell">
              <button type="button" class="small-btn" @click="editRole(item)">编辑</button>
              <button type="button" class="small-btn danger-btn" @click="removeRole(item)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="roles.length === 0" class="empty">暂无角色数据</div>
    </section>

    <p v-if="message" class="success">{{ message }}</p>
    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
  </div>
</template>

<script>
import {
  createRbacPermissionApi,
  createRbacRoleApi,
  deleteRbacPermissionApi,
  deleteRbacRoleApi,
  listRbacPermissionsApi,
  listRbacRolesApi,
  updateRbacPermissionApi,
  updateRbacRoleApi
} from '../api/rbac'

export default {
  name: 'AdminRbacView',
  data() {
    return {
      permissions: [],
      roles: [],
      permissionKeyword: '',
      roleKeyword: '',
      editingPermissionId: null,
      editingRoleId: null,
      submittingPermission: false,
      submittingRole: false,
      message: '',
      errorMessage: '',
      permissionForm: {
        code: '',
        name: ''
      },
      roleForm: {
        code: '',
        name: '',
        description: '',
        permissionIds: []
      }
    }
  },
  mounted() {
    this.loadAll()
  },
  methods: {
    resetMessages() {
      this.message = ''
      this.errorMessage = ''
    },
    formatPermissionCodes(codes) {
      if (!codes || codes.length === 0) {
        return '-'
      }
      return codes.join(', ')
    },
    hasPermission(permissionId) {
      return this.roleForm.permissionIds.includes(permissionId)
    },
    togglePermission(permissionId) {
      if (this.hasPermission(permissionId)) {
        this.roleForm.permissionIds = this.roleForm.permissionIds.filter((id) => id !== permissionId)
      } else {
        this.roleForm.permissionIds = [...this.roleForm.permissionIds, permissionId]
      }
    },
    async loadAll() {
      this.resetMessages()
      await Promise.all([this.loadPermissions(), this.loadRoles()])
    },
    async loadPermissions() {
      try {
        const params = this.permissionKeyword ? { keyword: this.permissionKeyword } : {}
        const res = await listRbacPermissionsApi(params)
        this.permissions = res.data || []
      } catch (e) {
        this.errorMessage = e.message || '加载权限失败'
      }
    },
    async loadRoles() {
      try {
        const params = this.roleKeyword ? { keyword: this.roleKeyword } : {}
        const res = await listRbacRolesApi(params)
        this.roles = res.data || []
      } catch (e) {
        this.errorMessage = e.message || '加载角色失败'
      }
    },
    editPermission(item) {
      this.editingPermissionId = item.id
      this.permissionForm = {
        code: item.code,
        name: item.name
      }
      this.message = `正在编辑权限：${item.code}`
      this.errorMessage = ''
    },
    resetPermissionForm() {
      this.editingPermissionId = null
      this.permissionForm = {
        code: '',
        name: ''
      }
    },
    async submitPermission() {
      if (!this.permissionForm.code || !this.permissionForm.name) {
        this.errorMessage = '请填写权限编码和名称'
        return
      }
      this.submittingPermission = true
      this.resetMessages()
      try {
        const payload = {
          code: this.permissionForm.code,
          name: this.permissionForm.name
        }
        if (this.editingPermissionId) {
          await updateRbacPermissionApi(this.editingPermissionId, payload)
          this.message = '权限更新成功'
        } else {
          await createRbacPermissionApi(payload)
          this.message = '权限新增成功'
        }
        this.resetPermissionForm()
        await this.loadAll()
      } catch (e) {
        this.errorMessage = e.message || '权限提交失败'
      } finally {
        this.submittingPermission = false
      }
    },
    async removePermission(item) {
      const confirmed = window.confirm(`确认删除权限「${item.code}」吗？`)
      if (!confirmed) {
        return
      }
      this.resetMessages()
      try {
        await deleteRbacPermissionApi(item.id)
        this.message = '权限删除成功'
        if (this.editingPermissionId === item.id) {
          this.resetPermissionForm()
        }
        await this.loadAll()
      } catch (e) {
        this.errorMessage = e.message || '权限删除失败'
      }
    },
    editRole(item) {
      this.editingRoleId = item.id
      this.roleForm = {
        code: item.code || '',
        name: item.name || '',
        description: item.description || '',
        permissionIds: (item.permissionIds || []).slice()
      }
      this.message = `正在编辑角色：${item.code}`
      this.errorMessage = ''
    },
    resetRoleForm() {
      this.editingRoleId = null
      this.roleForm = {
        code: '',
        name: '',
        description: '',
        permissionIds: []
      }
    },
    async submitRole() {
      if (!this.roleForm.code || !this.roleForm.name) {
        this.errorMessage = '请填写角色编码和名称'
        return
      }
      this.submittingRole = true
      this.resetMessages()
      try {
        const payload = {
          code: this.roleForm.code,
          name: this.roleForm.name,
          description: this.roleForm.description || null,
          permissionIds: this.roleForm.permissionIds
        }
        if (this.editingRoleId) {
          await updateRbacRoleApi(this.editingRoleId, payload)
          this.message = '角色更新成功'
        } else {
          await createRbacRoleApi(payload)
          this.message = '角色新增成功'
        }
        this.resetRoleForm()
        await this.loadRoles()
      } catch (e) {
        this.errorMessage = e.message || '角色提交失败'
      } finally {
        this.submittingRole = false
      }
    },
    async removeRole(item) {
      const confirmed = window.confirm(`确认删除角色「${item.code}」吗？`)
      if (!confirmed) {
        return
      }
      this.resetMessages()
      try {
        await deleteRbacRoleApi(item.id)
        this.message = '角色删除成功'
        if (this.editingRoleId === item.id) {
          this.resetRoleForm()
        }
        await this.loadRoles()
      } catch (e) {
        this.errorMessage = e.message || '角色删除失败'
      }
    }
  }
}
</script>
