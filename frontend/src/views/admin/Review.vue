<script setup>
import { ref, onMounted } from 'vue'
import { adminAPI } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const articles = ref([])
const total = ref(0)
const page = ref(1)

async function load() {
  const res = await adminAPI.pending({ page: page.value, pageSize: 10 })
  articles.value = res.data.list
  total.value = res.data.total
}

async function approve(id) {
  await ElMessageBox.confirm('确认审核通过？', '提示', { type: 'success' })
  await adminAPI.approve(id)
  ElMessage.success('已通过')
  load()
}

async function reject(id) {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入驳回原因', '驳回', {
      confirmButtonText: '确认驳回',
      cancelButtonText: '取消',
      inputValidator: (v) => v ? true : '驳回原因不能为空'
    })
    await adminAPI.reject(id, reason)
    ElMessage.success('已驳回')
    load()
  } catch { /* cancelled */ }
}

async function exportExcel() {
  const res = await adminAPI.exportExcel()
  const url = window.URL.createObjectURL(new Blob([res.data]))
  const a = document.createElement('a')
  a.href = url
  a.download = `文章数据_${new Date().toISOString().substring(0, 10)}.xlsx`
  a.click()
  window.URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

onMounted(load)
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 15px">
      <h2 style="margin: 0">文章审核</h2>
      <el-button type="success" @click="exportExcel">📥 导出 Excel</el-button>
    </div>

    <el-table :data="articles" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="authorName" label="作者" width="100" />
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column prop="createTime" label="提交时间" width="160">
        <template #default="{ row }">{{ row.createTime?.substring(0, 16) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="success" size="small" @click="approve(row.id)">通过</el-button>
          <el-button type="danger" size="small" @click="reject(row.id)">驳回</el-button>
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
