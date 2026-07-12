<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authAPI, userAPI, applyAPI } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const user = ref({})
const saving = ref(false)

// 个人信息编辑
const editForm = reactive({ nickname: '', email: '' })

// 修改密码
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const changingPwd = ref(false)

// 管理员申请
const stats = ref({ articleCount: 0, totalViews: 0 })
const applyReason = ref('')
const applying = ref(false)
const myApply = ref(null)
const APPLY_MIN_ARTICLES = 5
const APPLY_MIN_VIEWS = 1000
const canApply = computed(() => {
  return stats.value.articleCount >= APPLY_MIN_ARTICLES && stats.value.totalViews >= APPLY_MIN_VIEWS
})
async function load() {
  try {
    const res = await authAPI.me()
    user.value = res.data || {}
    editForm.nickname = user.value.nickname || ''
    editForm.email = user.value.email || ''
  } catch { /* 401 handled by interceptor */ }
}

async function loadStats() {
  try {
    const res = await userAPI.getStats()
    stats.value = res.data || { articleCount: 0, totalViews: 0 }
  } catch { /* ignore */ }
}

async function loadMyApply() {
  try {
    const res = await applyAPI.my()
    myApply.value = res.data
  } catch { /* ignore */ }
}

async function saveProfile() {
  saving.value = true
  try {
    await userAPI.updateProfile({ nickname: editForm.nickname, email: editForm.email })
    user.value.nickname = editForm.nickname
    user.value.email = editForm.email
    localStorage.setItem('nickname', editForm.nickname)
    ElMessage.success('个人信息已更新')
  } finally {
    saving.value = false
  }
}

async function changePassword() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) { ElMessage.warning('请填写完整'); return }
  if (pwdForm.newPassword.length < 6) { ElMessage.warning('新密码至少6位'); return }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) { ElMessage.warning('两次输入的新密码不一致'); return }
  changingPwd.value = true
  try {
    await userAPI.changePassword(pwdForm.oldPassword, pwdForm.newPassword)
    ElMessage.success('密码修改成功，请重新登录')
    localStorage.clear()
    router.push('/login')
  } finally {
    changingPwd.value = false
  }
}

async function submitApply() {
  if (!applyReason.value.trim()) { ElMessage.warning('请填写申请理由'); return }
  applying.value = true
  try {
    await applyAPI.submit(applyReason.value)
    ElMessage.success('申请已提交，请等待审核')
    loadMyApply()
  } finally {
    applying.value = false
  }
}

const roleMap = { 0: '普通用户', 1: '管理员', 2: '超级管理员' }
const applyStatusMap = { 0: '待审核', 1: '已通过', 2: '已拒绝' }

onMounted(() => { load(); loadStats(); loadMyApply() })
</script>

<template>
  <div style="max-width: 600px; margin: 0 auto">
    <h2>个人中心</h2>

    <!-- 基本信息 -->
    <el-card style="margin-bottom: 20px">
      <template #header><span style="font-weight: bold">基本信息</span></template>
      <el-form label-width="80px">
        <el-form-item label="用户名">
          <el-input :model-value="user.username" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="角色">
          <el-tag :type="user.role === 2 ? 'danger' : user.role === 1 ? 'warning' : 'success'">
            {{ roleMap[user.role] || user.role }}
          </el-tag>
        </el-form-item>
        <el-form-item label="注册时间">
          <span>{{ user.createTime?.substring(0, 16) }}</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="saveProfile">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 修改密码 -->
    <el-card style="margin-bottom: 20px">
      <template #header><span style="font-weight: bold">修改密码</span></template>
      <el-form :model="pwdForm" label-width="100px">
        <el-form-item label="原密码">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少6位" />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="changingPwd" @click="changePassword">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 申请成为管理员（仅普通用户可见） -->
    <el-card v-if="user.role === 0" style="margin-bottom: 20px">
      <template #header><span style="font-weight: bold">申请成为管理员</span></template>
      <div style="margin-bottom: 12px; color: #666">
        <p>申请条件：</p>
        <p>✅ 已发布文章 ≥ {{ APPLY_MIN_ARTICLES }} 篇（当前：{{ stats.articleCount }} 篇）</p>
        <p>✅ 累计浏览量 ≥ {{ APPLY_MIN_VIEWS }} 次（当前：{{ stats.totalViews }} 次）</p>
      </div>

      <template v-if="myApply">
        <el-alert :title="'申请状态：' + applyStatusMap[myApply.status]" :type="myApply.status === 1 ? 'success' : myApply.status === 2 ? 'error' : 'info'" show-icon :closable="false" />
        <div v-if="myApply.status === 2 && myApply.reply" style="margin-top: 8px; color: #999">驳回理由：{{ myApply.reply }}</div>
      </template>

      <template v-else>
        <el-input v-model="applyReason" type="textarea" :rows="2" placeholder="请简述申请理由..." :disabled="!canApply" />
        <el-button type="primary" style="margin-top: 8px" :disabled="!canApply" :loading="applying" @click="submitApply">
          {{ canApply ? '提交申请' : '暂不满足申请条件' }}
        </el-button>
      </template>
    </el-card>
  </div>
</template>
