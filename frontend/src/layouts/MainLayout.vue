<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { notifyAPI } from '../api'

const router = useRouter()
const route = useRoute()
const token = ref(localStorage.getItem('token'))
const nickname = ref(localStorage.getItem('nickname') || '用户')
const role = ref(Number(localStorage.getItem('role') || 0))
const unreadCount = ref(0)

const isLoggedIn = computed(() => !!token.value)
const isAdmin = computed(() => role.value === 1)

async function loadUnread() {
  if (!token.value) return
  try {
    const res = await notifyAPI.unreadCount()
    unreadCount.value = res.data?.count || 0
  } catch { /* ignore */ }
}

function logout() {
  localStorage.clear()
  token.value = ''
  router.push('/login')
}

const activeMenu = computed(() => {
  if (route.path.startsWith('/admin')) return route.path
  return route.path === '/' ? '/' : '/' + (route.path.split('/')[1] || '')
})

onMounted(() => {
  loadUnread()
  window.addEventListener('notify-refresh', loadUnread)
})
setInterval(loadUnread, 30000)
</script>

<template>
  <el-container style="min-height: 100vh">
    <el-header style="background: #409EFF; display: flex; align-items: center; justify-content: space-between; padding: 0 20px">
      <div style="display: flex; align-items: center; gap: 30px">
        <h2 style="color: white; margin: 0; cursor: pointer" @click="router.push('/')">📰 新闻管理系统</h2>
        <el-menu
          :default-active="activeMenu"
          mode="horizontal"
          :ellipsis="false"
          style="background: transparent; border: none"
          text-color="rgba(255,255,255,0.85)"
          active-text-color="#fff"
          @select="(key) => router.push(key)"
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item v-if="isAdmin" index="/admin/review">文章审核</el-menu-item>
          <el-menu-item v-if="isAdmin" index="/admin/categories">分类管理</el-menu-item>
        </el-menu>
      </div>
      <div style="display: flex; align-items: center; gap: 15px">
        <template v-if="isLoggedIn">
          <el-button :icon="'Edit'" type="warning" size="small" @click="router.push('/write')">写文章</el-button>
          <el-button size="small" @click="router.push('/drafts')">📝 草稿箱</el-button>
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
            <el-button size="small" circle @click="router.push('/notifications')">🔔</el-button>
          </el-badge>
          <el-dropdown @command="(cmd) => cmd === 'profile' ? router.push('/profile') : logout()">
            <span style="color: white; cursor: pointer">{{ nickname }} ▾</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button size="small" @click="router.push('/login')">登录</el-button>
          <el-button size="small" type="success" @click="router.push('/register')">注册</el-button>
        </template>
      </div>
    </el-header>

    <el-main style="background: #f5f7fa; padding: 20px">
      <router-view />
    </el-main>
  </el-container>
</template>
