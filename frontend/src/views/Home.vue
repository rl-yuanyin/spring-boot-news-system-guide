<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { articleAPI, categoryAPI } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const token = ref(localStorage.getItem('token'))
const articles = ref([])
const hotArticles = ref([])
const categories = ref([])
const total = ref(0)
const hotSortBy = ref('views')  // views | likes
const query = ref({ page: 1, pageSize: 10, keyword: '', categoryId: null })

const statusMap = { 0: '草稿', 1: '待审核', 2: '已发布', 3: '已驳回' }

async function loadArticles() {
  const params = { ...query.value }
  if (!params.categoryId) delete params.categoryId
  if (!params.keyword) delete params.keyword
  const res = await articleAPI.list(params)
  articles.value = res.data.list
  total.value = res.data.total
}

async function loadHot() {
  const res = await articleAPI.hot(10)
  let list = res.data || []
  if (hotSortBy.value === 'likes') {
    list = list.sort((a, b) => (b.likeCount || 0) - (a.likeCount || 0))
  }
  hotArticles.value = list.slice(0, 5)
}

async function loadCategories() {
  const res = await categoryAPI.list()
  categories.value = res.data || []
}

function selectCategory(id) {
  query.value.categoryId = query.value.categoryId === id ? null : id
  search()
}

function search() {
  query.value.page = 1
  loadArticles()
}

function goDetail(id) {
  router.push(`/article/${id}`)
}

async function handleLike(article) {
  if (!token.value) { ElMessage.warning('请先登录'); return }
  const res = await articleAPI.like(article.id)
  article.likeCount += res.data ? 1 : -1
}

function toggleHotSort() {
  hotSortBy.value = hotSortBy.value === 'views' ? 'likes' : 'views'
  loadHot()
}

onMounted(() => {
  loadArticles()
  loadHot()
  loadCategories()
})
</script>

<template>
  <el-row :gutter="20">
    <el-col :span="17">
      <!-- 分类按钮 -->
      <el-card style="margin-bottom: 15px">
        <div style="display: flex; gap: 8px; flex-wrap: wrap; align-items: center">
          <el-button
            :type="!query.categoryId ? 'primary' : ''"
            size="small"
            @click="selectCategory(null)"
          >全部</el-button>
          <el-button
            v-for="c in categories" :key="c.id"
            :type="query.categoryId === c.id ? 'primary' : ''"
            size="small"
            @click="selectCategory(c.id)"
          >{{ c.name }}</el-button>
        </div>
      </el-card>

      <!-- 搜索栏 -->
      <el-card style="margin-bottom: 15px">
        <div style="display: flex; gap: 10px">
          <el-input v-model="query.keyword" placeholder="搜索文章标题或作者..." clearable @clear="search" @keyup.enter="search" style="width: 300px" />
          <el-button type="primary" @click="search">搜索</el-button>
        </div>
      </el-card>

      <!-- 文章列表 -->
      <el-card v-for="a in articles" :key="a.id" style="margin-bottom: 12px; cursor: pointer" @click="goDetail(a.id)" shadow="hover">
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center">
            <span style="font-size: 18px; font-weight: bold">{{ a.title }}</span>
            <el-tag v-if="a.isTop" type="danger" size="small">置顶</el-tag>
          </div>
        </template>
        <p style="color: #666; margin-bottom: 12px">{{ a.summary || '（暂无简介）' }}</p>
        <div style="display: flex; gap: 20px; color: #999; font-size: 13px; align-items: center">
          <span>✍ {{ a.authorName }}</span>
          <span>📂 {{ a.categoryName }}</span>
          <span>👁 {{ a.viewCount }}</span>
          <span style="cursor:pointer; user-select:none" @click.stop="handleLike(a)">{{ token ? '❤️' : '🤍' }} {{ a.likeCount }}</span>
          <span>🕐 {{ a.createTime?.substring(0, 10) }}</span>
          <el-tag v-if="a.status !== 2" :type="a.status === 0 ? 'info' : a.status === 1 ? 'warning' : 'danger'" size="small">{{ statusMap[a.status] }}</el-tag>
        </div>
      </el-card>

      <el-empty v-if="articles.length === 0" description="暂无文章" />
      <el-pagination
        v-if="total > query.pageSize"
        v-model:current-page="query.page"
        :page-size="query.pageSize"
        :total="total"
        layout="prev, pager, next"
        style="margin-top: 15px; justify-content: center"
        @current-change="loadArticles"
      />
    </el-col>

    <!-- 右侧：热门排行 -->
    <el-col :span="7">
      <el-card>
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center">
            <span style="font-weight: bold">🔥 热门文章</span>
            <el-button size="small" text @click="toggleHotSort">
              按{{ hotSortBy === 'views' ? '👁观看' : '❤点赞' }} ▾
            </el-button>
          </div>
        </template>
        <div v-if="hotArticles.length === 0" style="color: #999">暂无数据</div>
        <div v-for="(a, i) in hotArticles" :key="a.id" style="padding: 8px 0; border-bottom: 1px solid #eee; cursor: pointer" @click="goDetail(a.id)">
          <span :style="{ color: i < 3 ? '#f56c6c' : '#999', fontWeight: i < 3 ? 'bold' : 'normal' }">{{ i + 1 }}.</span>
          {{ a.title }}
          <span style="color: #999; font-size: 12px; float: right">
            {{ hotSortBy === 'views' ? '👁' + a.viewCount : '❤' + a.likeCount }}
          </span>
        </div>
      </el-card>
    </el-col>
  </el-row>
</template>
