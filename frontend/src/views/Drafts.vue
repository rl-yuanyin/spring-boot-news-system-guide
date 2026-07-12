<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { articleAPI } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const articles = ref([])
const total = ref(0)
const page = ref(1)

async function load() {
  const res = await articleAPI.list({ page: page.value, pageSize: 10, status: 0 })
  articles.value = res.data.list
  total.value = res.data.total
}

function goEdit(id) { router.push(`/edit/${id}`) }

async function del(id) {
  await articleAPI.del(id)
  ElMessage.success('已删除')
  load()
}

async function submit(id) {
  await articleAPI.submit(id)
  ElMessage.success('已提交审核')
  load()
}

onMounted(load)
</script>

<template>
  <div>
    <h2>📝 草稿箱</h2>
    <p style="color: #999">这里是您保存的草稿，可以继续编辑或提交审核</p>

    <el-table :data="articles" border stripe>
      <el-table-column prop="title" label="标题" min-width="250" />
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column prop="createTime" label="保存时间" width="170">
        <template #default="{ row }">{{ row.createTime?.substring(0, 16) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="280">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="goEdit(row.id)">编辑</el-button>
          <el-button type="success" size="small" @click="submit(row.id)">提交审核</el-button>
          <el-button type="danger" size="small" @click="del(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="articles.length === 0" description="暂无草稿" />
    <el-pagination v-if="total > 10" v-model:current-page="page" :page-size="10" :total="total" layout="prev, pager, next" style="margin-top:15px;justify-content:center" @current-change="load" />
  </div>
</template>
