<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { authAPI } from '../api'

const router = useRouter()
const formRef = ref(null)
const form = reactive({ username: 'admin', password: '123456', captchaKey: '', captcha: '' })
const captchaInfo = ref(null)
const loading = ref(false)

async function getCaptcha() {
  const res = await authAPI.captcha()
  captchaInfo.value = res.data
  form.captchaKey = res.data.captchaKey
  // 不自动填入验证码，用户需手动输入
}

async function login() {
  loading.value = true
  try {
    const res = await authAPI.login({ ...form })
    const { token, user } = res.data
    localStorage.setItem('token', token)
    localStorage.setItem('nickname', user.nickname)
    localStorage.setItem('role', user.role)
    localStorage.setItem('userId', user.id)
    router.push('/')
  } finally {
    loading.value = false
  }
}

getCaptcha()
</script>

<template>
  <div style="max-width: 400px; margin: 80px auto">
    <h2 style="text-align: center; margin-bottom: 30px">用户登录</h2>
    <el-form ref="formRef" :model="form" label-width="80px" @keyup.enter="login">
      <el-form-item label="用户名">
        <el-input v-model="form.username" placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
      </el-form-item>
      <el-form-item label="验证码">
        <div style="display: flex; gap: 10px; width: 100%">
          <el-input v-model="form.captcha" placeholder="验证码" style="flex:1" />
          <el-button @click="getCaptcha" :disabled="!captchaInfo" style="min-width:80px">
            {{ captchaInfo?.captchaCode || '获取' }}
          </el-button>
        </div>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" style="width: 100%" @click="login">登 录</el-button>
      </el-form-item>
    </el-form>
    <div style="text-align: center">
      还没有账号？<el-link type="primary" @click="router.push('/register')">立即注册</el-link>
    </div>
  </div>
</template>
