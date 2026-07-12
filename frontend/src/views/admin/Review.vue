<script setup>
import { ref, onMounted } from 'vue'
import { adminAPI, articleAPI } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const articles = ref([])
const total = ref(0)
const page = ref(1)
const activeTab = ref('pending')

// 预览对话框
const previewVisible = ref(false)
const previewArticle = ref({})

const statusTabMap = { pending: 1, approved: 2, rejected: 3, all: undefined }

const statusMap = { 0: '草稿', 1: '待审核', 2: '已通过', 3: '已驳回' }

async function load() {
  const params = { page: page.value, pageSize: 10 }
  let res
  if (activeTab.value === 'pending') {
    res = await adminAPI.pending(params)
  } else if (activeTab.value === 'all') {
    res = await adminAPI.all(params)
  } else {
    // approved (status=2) / rejected (status=3)
    params.status = statusTabMap[activeTab.value]
    res = await articleAPI.list(params)
  }
  articles.value = res.data.list
  total.value = res.data.total
}

// 预览文章详情
async function preview(article) {
  try {
    const res = await articleAPI.detail(article.id)
    previewArticle.value = res.data
    previewVisible.value = true
  } catch {
    ElMessage.error('加载文章失败')
  }
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
  const url = window.URL.createObjectURL(res)
  const a = document.createElement('a')
  a.href = url
  a.download = `文章数据_${new Date().toISOString().substring(0, 10)}.xlsx`
  a.click()
  window.URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

function handleTabChange() {
  page.value = 1
  load()
}

onMounted(load)
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 15px">
      <h2 style="margin: 0">文章管理</h2>
      <el-button type="success" @click="exportExcel">📥 导出 Excel</el-button>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="待审核" name="pending" />
      <el-tab-pane label="已通过" name="approved" />
      <el-tab-pane label="已驳回" name="rejected" />
      <el-tab-pane label="全部文章" name="all" />
    </el-tabs>

    <el-table :data="articles" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" min-width="200">
        <template #default="{ row }">
          <el-link type="primary" @click="preview(row)">{{ row.title }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="authorName" label="作者" width="100" />
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 2 ? 'success' : row.status === 3 ? 'danger' : 'warning'" size="small">
            {{ statusMap[row.status] || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" width="160">
        <template #default="{ row }">{{ row.createTime?.substring(0, 16) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="preview(row)">查看</el-button>
          <template v-if="row.status === 1">
            <el-button type="success" size="small" @click="approve(row.id)">通过</el-button>
            <el-button type="danger" size="small" @click="reject(row.id)">驳回</el-button>
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

    <!-- 文章预览对话框 -->
    <el-dialog v-model="previewVisible" title="文章预览" width="800px" top="5vh">
      <template v-if="previewArticle.id">
        <h2 style="margin-top: 0">{{ previewArticle.title }}</h2>
        <div style="color: #999; margin-bottom: 16px">
          <span>✍ {{ previewArticle.authorName }}</span>
          <span style="margin-left: 16px">📂 {{ previewArticle.categoryName }}</span>
          <span style="margin-left: 16px">
            <el-tag :type="previewArticle.status === 2 ? 'success' : previewArticle.status === 3 ? 'danger' : 'warning'" size="small">
              {{ statusMap[previewArticle.status] || previewArticle.status }}
            </el-tag>
          </span>
          <span style="margin-left: 16px">🕐 {{ previewArticle.createTime?.substring(0, 16) }}</span>
        </div>
        <div v-if="previewArticle.status === 3 && previewArticle.rejectReason" style="margin-bottom: 16px">
          <el-alert :title="'驳回原因：' + previewArticle.rejectReason" type="warning" show-icon :closable="false" />
        </div>
        <div v-if="previewArticle.coverUrl" style="text-align: center; margin-bottom: 16px">
          <img :src="previewArticle.coverUrl" style="max-width: 100%; max-height: 250px" alt="封面" />
        </div>
        <div style="border-top: 1px solid #eee; padding-top: 16px">
          <div v-html="previewArticle.content" style="line-height: 1.8; max-height: 500px; overflow-y: auto"></div>
        </div>
      </template>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
        <template v-if="previewArticle.status === 1">
          <el-button type="success" @click="previewVisible = false; approve(previewArticle.id)">审核通过</el-button>
          <el-button type="danger" @click="previewVisible = false; reject(previewArticle.id)">驳回</el-button>
        </template>
      </template>
    </el-dialog>
  </div>
</template>
