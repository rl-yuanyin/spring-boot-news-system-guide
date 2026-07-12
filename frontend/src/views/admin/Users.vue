<script setup>
import { ref, onMounted } from 'vue'
import { adminUserAPI } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const users = ref([])
const total = ref(0)
const page = ref(1)
const currentRole = ref(Number(localStorage.getItem('role') || 0))
const isSuperAdmin = currentRole.value === 2

const roleMap = { 0: '普通用户', 1: '管理员', 2: '超级管理员' }
const statusMap = { 0: '正常', 1: '已禁用' }

async function load() {
  const res = await adminUserAPI.list({ page: page.value, pageSize: 10 })
  users.value = res.data.records || []
  total.value = res.data.total || 0
}

async function toggleStatus(user) {
  const action = user.status === 0 ? '禁用' : '启用'
  await ElMessageBox.confirm(`确定要${action}用户「${user.nickname}」吗？`, '提示', { type: 'warning' })
  await adminUserAPI.toggleStatus(user.id)
  ElMessage.success(`已${action}`)
  load()
}

async function promoteUser(user) {
  await ElMessageBox.confirm(`确定要任命「${user.nickname}」为管理员吗？`, '任命管理员', { type: 'info' })
  await adminUserAPI.promote(user.id)
  ElMessage.success('已任命为管理员')
  load()
}

async function demoteUser(user) {
  await ElMessageBox.confirm(`确定要撤职管理员「${user.nickname}」吗？`, '撤职管理员', { type: 'warning' })
  await adminUserAPI.demote(user.id)
  ElMessage.success('已撤职')
  load()
}

onMounted(load)
</script>

<template>
  <div>
    <h2>用户管理</h2>

    <el-table :data="users" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="email" label="邮箱" min-width="180">
        <template #default="{ row }">{{ row.email || '未设置' }}</template>
      </el-table-column>
      <el-table-column label="角色" width="110">
        <template #default="{ row }">
          <el-tag :type="row.role === 2 ? 'danger' : row.role === 1 ? 'warning' : ''" size="small">
            {{ roleMap[row.role] || row.role }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">{{ statusMap[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="160">
        <template #default="{ row }">{{ row.createTime?.substring(0, 16) }}</template>
      </el-table-column>
      <el-table-column label="操作" min-width="200" fixed="right">
        <template #default="{ row }">
          <!-- 超管对普通管理员的任命/撤职 -->
          <template v-if="isSuperAdmin">
            <el-button v-if="row.role === 0" type="primary" size="small" @click="promoteUser(row)">任命管理员</el-button>
            <el-button v-if="row.role === 1" type="warning" size="small" @click="demoteUser(row)">撤职</el-button>
            <el-button v-if="row.role !== 2" :type="row.status === 0 ? 'danger' : 'success'" size="small" @click="toggleStatus(row)">
              {{ row.status === 0 ? '禁用' : '启用' }}
            </el-button>
          </template>
          <!-- 普通管理员：只能管理普通用户 -->
          <template v-else>
            <el-button v-if="row.role === 0" :type="row.status === 0 ? 'danger' : 'success'" size="small" @click="toggleStatus(row)">
              {{ row.status === 0 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="total > 10"
      v-model:current-page="page"
      :page-size="10"
      :total="total"
      layout="prev, pager, next"
      style="margin-top: 15px; justify-content: center"
      @current-change="load"
    />
  </div>
</template>
