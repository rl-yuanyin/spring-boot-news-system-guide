<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { appealAPI } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const username = ref(sessionStorage.getItem('bannedUser') || '')
const reason = ref('')
const submitting = ref(false)
const myAppeal = ref(null)
const loading = ref(true)

const statusMap = { 0: '待处理', 1: '已通过', 2: '已拒绝' }

async function loadAppeal() {
  if (!username.value) { loading.value = false; return }
  try {
    const res = await appealAPI.my(username.value)
    myAppeal.value = res.data
  } catch { /* no appeal yet */ }
  loading.value = false
}

async function submitAppeal() {
  if (!reason.value.trim()) { ElMessage.warning('请填写申诉理由'); return }
  submitting.value = true
  try {
    await appealAPI.submit(username.value, reason.value)
    ElMessage.success('申诉已提交')
    loadAppeal()
  } finally {
    submitting.value = false
  }
}

onMounted(loadAppeal)
</script>

<template>
  <div style="max-width: 500px; margin: 60px auto; text-align: center">
    <h2 style="color: #f56c6c">⚠ 账号已被封禁</h2>
    <p style="color: #666; margin-bottom: 30px">
      您的账号 <strong>{{ username }}</strong> 因违规行为被管理员封禁。
    </p>

    <el-card v-if="!username" style="text-align: left">
      <p style="color: #999">无法获取被封禁账号信息，请返回登录页重新尝试。</p>
      <el-button type="primary" @click="router.push('/login')">返回登录</el-button>
    </el-card>

    <el-card v-else-if="loading" style="text-align: center">
      <p>加载中...</p>
    </el-card>

    <!-- 申诉已通过 -->
    <el-card v-else-if="myAppeal && myAppeal.status === 1">
      <el-result icon="success" title="申诉已通过" sub-title="您的账号已解封，请返回登录">
        <template #extra>
          <el-button type="primary" @click="router.push('/login')">返回登录</el-button>
        </template>
      </el-result>
    </el-card>

    <!-- 申诉被驳回 -->
    <el-card v-else-if="myAppeal && myAppeal.status === 2" style="text-align: left">
      <el-alert title="申诉已被驳回" type="error" show-icon :closable="false" />
      <div v-if="myAppeal.reply" style="margin: 12px 0; padding: 10px; background: #f5f5f5; border-radius: 4px">
        <strong>管理员回复：</strong>{{ myAppeal.reply }}
      </div>
      <p style="color: #666; margin-top: 16px">你可以重新提交申诉：</p>
      <el-input v-model="reason" type="textarea" :rows="3" placeholder="请说明申诉理由..." />
      <el-button type="primary" :loading="submitting" style="margin-top: 8px" @click="submitAppeal">重新提交申诉</el-button>
    </el-card>

    <!-- 申诉待处理 -->
    <el-card v-else-if="myAppeal && myAppeal.status === 0">
      <el-result icon="info" title="申诉处理中" sub-title="您的申诉已提交，请耐心等待管理员审核" />
    </el-card>

    <!-- 无申诉记录 -->
    <el-card v-else style="text-align: left">
      <p style="color: #666; margin-bottom: 12px">如果你认为封禁有误，可以提交申诉：</p>
      <el-input v-model="reason" type="textarea" :rows="3" placeholder="请说明申诉理由..." />
      <el-button type="primary" :loading="submitting" style="margin-top: 8px" @click="submitAppeal">提交申诉</el-button>
    </el-card>
  </div>
</template>
