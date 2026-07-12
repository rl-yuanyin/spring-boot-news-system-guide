<script setup>
import { ref, onMounted } from 'vue'
import { adminUserAPI } from '../../api'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const http = axios.create({ baseURL: '/api' })
http.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})
http.interceptors.response.use(res => res.data, err => {
  ElMessage.error(err.response?.data?.message || '请求失败')
  return Promise.reject(err)
})

const applies = ref([])
const total = ref(0)
const page = ref(1)
const activeTab = ref('admin')

const statusMap = { 0: '待审核', 1: '已通过', 2: '已拒绝' }

async function loadAdminApplies() {
  const res = await http.get('/admin/admin-applies', { params: { page: page.value, pageSize: 10 } })
  applies.value = res.data.records || []
  total.value = res.data.total || 0
}

async function loadAppeals() {
  const res = await http.get('/admin/appeals', { params: { page: page.value, pageSize: 10 } })
  applies.value = res.data.records || []
  total.value = res.data.total || 0
}

async function load() {
  if (activeTab.value === 'admin') await loadAdminApplies()
  else await loadAppeals()
}

async function approveApply(id) {
  await ElMessageBox.confirm('确认通过该申请？', '提示', { type: 'success' })
  await http.put(`/admin/admin-applies/${id}/approve`)
  ElMessage.success('已通过')
  load()
}

async function rejectApply(id) {
  try {
    const { value: reply } = await ElMessageBox.prompt('请输入驳回理由', '驳回', {
      confirmButtonText: '确认', cancelButtonText: '取消'
    })
    await http.put(`/admin/admin-applies/${id}/reject`, { reply })
    ElMessage.success('已驳回')
    load()
  } catch { /* cancelled */ }
}

async function approveAppeal(id) {
  await ElMessageBox.confirm('确认通过该申诉？将自动解封用户。', '提示', { type: 'success' })
  await http.put(`/admin/appeals/${id}/approve`)
  ElMessage.success('已通过并解封')
  load()
}

async function rejectAppeal(id) {
  try {
    const { value: reply } = await ElMessageBox.prompt('请输入驳回理由', '驳回', {
      confirmButtonText: '确认', cancelButtonText: '取消'
    })
    await http.put(`/admin/appeals/${id}/reject`, { reply })
    ElMessage.success('已驳回')
    load()
  } catch { /* cancelled */ }
}

onMounted(load)
</script>

<template>
  <div>
    <h2>审核管理</h2>

    <el-tabs v-model="activeTab" @tab-change="() => { page = 1; load() }">
      <el-tab-pane label="管理员申请" name="admin" />
      <el-tab-pane label="申诉管理" name="appeal" />
    </el-tabs>

    <el-table :data="applies" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="申请人" width="120">
        <template #default="{ row }">{{ row.username || '用户ID:' + row.userId }}</template>
      </el-table-column>
      <el-table-column prop="reason" label="理由" min-width="180" />
      <el-table-column v-if="activeTab === 'admin'" label="文章数" width="80">
        <template #default="{ row }">{{ row.articleCount || '-' }}</template>
      </el-table-column>
      <el-table-column v-if="activeTab === 'admin'" label="浏览量" width="80">
        <template #default="{ row }">{{ row.totalViews || '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'danger' : 'info'" size="small">
            {{ statusMap[row.status] || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button v-if="activeTab === 'admin'" type="success" size="small" @click="approveApply(row.id)">通过</el-button>
            <el-button v-if="activeTab === 'appeal'" type="success" size="small" @click="approveAppeal(row.id)">通过</el-button>
            <el-button v-if="activeTab === 'admin'" type="danger" size="small" @click="rejectApply(row.id)">驳回</el-button>
            <el-button v-if="activeTab === 'appeal'" type="danger" size="small" @click="rejectAppeal(row.id)">驳回</el-button>
          </template>
          <span v-else style="color: #999">已处理</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
