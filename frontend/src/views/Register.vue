<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { authAPI } from '../api'

const router = useRouter()
const form = reactive({ username: '', password: '', nickname: '' })
const loading = ref(false)

async function register() {
  if (!form.username || !form.password) return
  loading.value = true
  try {
    await authAPI.register({ ...form })
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div style="max-width: 400px; margin: 80px auto">
    <h2 style="text-align: center; margin-bottom: 30px">用户注册</h2>
    <el-form :model="form" label-width="80px" @keyup.enter="register">
      <el-form-item label="用户名">
        <el-input v-model="form.username" placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model="form.nickname" placeholder="请输入昵称" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" style="width: 100%" @click="register">注 册</el-button>
      </el-form-item>
    </el-form>
    <div style="text-align: center">
      已有账号？<el-link type="primary" @click="router.push('/login')">返回登录</el-link>
    </div>
  </div>
</template>
