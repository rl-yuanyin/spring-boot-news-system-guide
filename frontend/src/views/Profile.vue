<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authAPI } from '../api'

const router = useRouter()
const user = ref({})

async function load() {
  try {
    const res = await authAPI.me()
    user.value = res.data || {}
  } catch { /* 401 handled by interceptor */ }
}

onMounted(load)
</script>

<template>
  <div style="max-width: 500px; margin: 0 auto">
    <h2>个人中心</h2>
    <el-card>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="用户ID">{{ user.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ user.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ user.nickname }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag :type="user.role === 1 ? 'danger' : 'success'">{{ user.role === 1 ? '管理员' : '普通用户' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ user.email || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ user.createTime?.substring(0, 16) }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>
