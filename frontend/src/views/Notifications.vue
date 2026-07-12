<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { notifyAPI } from '../api'

const router = useRouter()
const notifications = ref([])

const typeMap = { like: '❤️ 点赞', comment: '💬 评论', admin_delete: '⚠️ 系统通知' }

async function load() {
  const res = await notifyAPI.list()
  notifications.value = res.data || []
}

async function readAll() {
  await notifyAPI.readAll()
  load()
  window.dispatchEvent(new Event('notify-refresh'))
}

function goArticle(id) {
  router.push(`/article/${id}`)
}

onMounted(load)
</script>

<template>
  <div style="max-width: 700px; margin: 0 auto">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px">
      <h2 style="margin:0">🔔 消息通知</h2>
      <el-button size="small" @click="readAll">全部已读</el-button>
    </div>

    <div v-if="notifications.length === 0" style="text-align: center; padding: 40px; color: #999">
      暂无通知
    </div>

    <div v-for="n in notifications" :key="n.id"
         style="padding: 14px; margin-bottom: 8px; border-radius: 8px; cursor: pointer"
         :style="{ background: n.isRead ? '#fff' : '#ecf5ff', border: '1px solid ' + (n.isRead ? '#eee' : '#b3d8ff') }"
         @click="goArticle(n.articleId)">
      <span style="margin-right: 8px">{{ typeMap[n.type] || n.type }}</span>
      <span>{{ n.content }}</span>
      <span style="float: right; color: #999; font-size: 12px">{{ n.createTime?.substring(0, 16) }}</span>
      <el-tag v-if="!n.isRead" size="small" type="danger" style="margin-left:8px">NEW</el-tag>
    </div>
  </div>
</template>
