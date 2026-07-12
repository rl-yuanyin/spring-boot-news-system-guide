<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { articleAPI, commentAPI, adminArticleAPI } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const article = ref({})
const comments = ref([])
const commentText = ref('')
const replyTo = ref(null)  // { id, username } 正在回复的评论
const replyText = ref('')
const userId = ref(Number(localStorage.getItem('userId') || 0))
const isAdmin = ref(Number(localStorage.getItem('role') || 0) >= 1)

const statusMap = { 0: '草稿', 1: '待审核', 2: '已发布', 3: '已驳回' }

async function load() {
  const res = await articleAPI.detail(route.params.id)
  article.value = res.data
}

async function loadComments() {
  const res = await commentAPI.list(route.params.id)
  comments.value = res.data || []
}

async function submitComment() {
  if (!commentText.value.trim()) return
  await commentAPI.create({ articleId: Number(route.params.id), content: commentText.value })
  commentText.value = ''
  ElMessage.success('评论成功')
  loadComments()
}

async function deleteComment(id) {
  await ElMessageBox.confirm('确定删除这条评论？', '提示', { type: 'warning' })
  await commentAPI.del(id)
  ElMessage.success('已删除')
  loadComments()
}

function canDelete(commentUserId) {
  return isAdmin.value || userId.value === commentUserId
}

async function handleLike() {
  if (!userId.value) { ElMessage.warning('请先登录'); return }
  const res = await articleAPI.like(article.value.id)
  article.value.likeCount += res.data ? 1 : -1
  ElMessage.success(res.message || (res.data ? '已点赞' : '已取消'))
}

// 管理员删文
const deleteReasons = ['不符合社区要求', '不实信息', '敏感内容', '侵权内容', '其他违规']
async function adminDelete() {
  try {
    const { value: reason } = await ElMessageBox.prompt('请选择或输入删除原因', '管理员删文', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '选择或输入原因...\n' + deleteReasons.join('\n'),
      inputValidator: (v) => v ? true : '删除原因不能为空'
    })
    await adminArticleAPI.deleteWithReason(article.value.id, reason)
    ElMessage.success('已删除并通知作者')
    router.push('/')
  } catch { /* cancelled */ }
}

function startReply(comment) {
  replyTo.value = comment
  replyText.value = ''
}

function cancelReply() {
  replyTo.value = null
  replyText.value = ''
}

async function submitReply() {
  if (!replyText.value.trim()) return
  await commentAPI.create({ articleId: Number(route.params.id), parentId: replyTo.value.id, content: replyText.value })
  ElMessage.success('回复成功')
  cancelReply()
  loadComments()
}

// 获取某条评论的所有回复
function getReplies(parentId) {
  return comments.value.filter(c => c.parentId === parentId)
}

onMounted(() => { load(); loadComments() })
</script>

<template>
  <el-row :gutter="20">
    <el-col :span="17">
      <el-card>
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: flex-start">
            <div style="flex:1">
              <h2 style="margin: 0 0 10px 0">{{ article.title }}</h2>
              <div style="display: flex; gap: 15px; color: #999; font-size: 13px">
                <span>✍ {{ article.authorName }}</span>
                <span>📂 {{ article.categoryName }}</span>
                <span>👁 {{ article.viewCount }}</span>
                <span>🕐 {{ article.createTime?.substring(0, 16) }}</span>
                <el-tag v-if="article.status !== 2" size="small">{{ statusMap[article.status] || article.status }}</el-tag>
              </div>
            </div>
            <el-dropdown v-if="isAdmin" trigger="click" @command="(cmd) => cmd === 'delete' && adminDelete()">
              <el-button style="border:none; font-size:22px; padding:4px 8px; line-height:1">⋯</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="delete" style="color:#f56c6c">⚠ 管理员删文</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <div v-if="article.status === 3 && article.rejectReason" style="margin-top: 10px">
            <el-alert :title="'驳回原因：' + article.rejectReason" type="warning" show-icon :closable="false" />
          </div>
        </template>
        <div v-if="article.coverUrl" style="text-align: center; margin-bottom: 20px">
          <img :src="article.coverUrl" style="max-width: 100%; max-height: 300px" alt="封面" />
        </div>
        <div class="article-body" v-html="article.content" style="line-height: 1.8; min-height: 200px"></div>
        <div style="margin-top: 20px; padding-top: 15px; border-top: 1px solid #eee; display: flex; align-items: center; justify-content: center; gap: 6px">
          <span @click="handleLike" style="cursor:pointer; font-size:28px; user-select:none; transition: transform 0.2s"
                :style="{ color: userId ? '#f56c6c' : '#ccc' }"
                @mouseenter="$event.target.style.transform='scale(1.2)'"
                @mouseleave="$event.target.style.transform='scale(1)'">
            {{ userId ? '❤️' : '🤍' }}
          </span>
          <span style="font-size:18px; font-weight:600; color:#333">{{ article.likeCount || 0 }}</span>
          <span v-if="!userId" style="color:#999; font-size:12px">登录后即可点赞</span>
        </div>
      </el-card>

      <!-- 评论区 -->
      <el-card style="margin-top: 20px">
        <template #header><span style="font-weight: bold">💬 评论 ({{ comments.length }})</span></template>
        <div v-if="!userId" style="text-align: center; padding: 20px; color: #999">
          请先<a @click="router.push('/login')" style="color: #409EFF; cursor: pointer">登录</a>后发表评论
        </div>
        <div v-else style="margin-bottom: 15px">
          <el-input v-model="commentText" type="textarea" :rows="3" placeholder="写下你的评论..." />
          <el-button type="primary" style="margin-top: 8px" @click="submitComment">发表评论</el-button>
        </div>
        <div v-for="c in comments.filter(x => !x.parentId)" :key="c.id" style="padding: 12px 0; border-bottom: 1px solid #f0f0f0">
          <div style="display: flex; justify-content: space-between; margin-bottom: 6px">
            <span style="font-weight: bold">{{ c.username }}</span>
            <span style="color: #999; font-size: 12px">{{ c.createTime?.substring(0, 16) }}</span>
          </div>
          <p style="margin: 0; color: #333">{{ c.content }}</p>
          <div style="margin-top: 4px; display: flex; gap: 8px">
            <el-button v-if="userId" size="small" text type="primary" @click="startReply(c)">回复</el-button>
            <el-button v-if="canDelete(c.userId)" size="small" text type="danger" @click="deleteComment(c.id)">删除</el-button>
          </div>

          <!-- 回复列表 -->
          <div v-for="r in getReplies(c.id)" :key="r.id" style="margin: 8px 0 0 30px; padding: 8px 12px; background: #f9f9f9; border-radius: 4px">
            <span style="font-weight: bold">{{ r.username }}</span>
            <span v-if="r.parentId"> 回复 </span>
            <span style="color: #999; font-size: 12px; float: right">{{ r.createTime?.substring(0, 16) }}</span>
            <p style="margin: 4px 0; color: #333">{{ r.content }}</p>
            <el-button v-if="canDelete(r.userId)" size="small" text type="danger" @click="deleteComment(r.id)">删除</el-button>
          </div>

          <!-- 回复输入框 -->
          <div v-if="replyTo?.id === c.id" style="margin: 8px 0 0 30px">
            <el-input v-model="replyText" type="textarea" :rows="2" :placeholder="'回复 ' + replyTo.username + '...'" />
            <el-button size="small" type="primary" style="margin-top: 4px" @click="submitReply">回复</el-button>
            <el-button size="small" style="margin-top: 4px" @click="cancelReply">取消</el-button>
          </div>
        </div>
      </el-card>
    </el-col>

    <el-col :span="7">
      <el-card>
        <template #header>关于作者</template>
        <p style="margin-bottom:8px">{{ article.authorName }}</p>
        <el-button type="primary" @click="router.back()">返回列表</el-button>
      </el-card>
    </el-col>
  </el-row>
</template>

<style scoped>
.article-body img {
  max-width: 100%;
  height: auto;
}
</style>
