<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { articleAPI, categoryAPI, fileAPI } from '../api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const isEdit = ref(!!route.params.id)
const categories = ref([])
const loading = ref(false)
const form = reactive({ title: '', summary: '', content: '', coverUrl: '', categoryId: null, draft: false })
const uploading = ref(false)

async function loadCategories() {
  const res = await categoryAPI.list()
  categories.value = res.data || []
}

async function loadArticle() {
  if (!isEdit.value) return
  const res = await articleAPI.detail(route.params.id)
  const a = res.data
  Object.assign(form, {
    title: a.title, summary: a.summary, content: a.content,
    coverUrl: a.coverUrl, categoryId: a.categoryId, draft: false
  })
}

async function handleUpload(e) {
  const file = e.target.files?.[0] || e.raw
  if (!file) return
  const fd = new FormData()
  fd.append('file', file)
  uploading.value = true
  try {
    const res = await fileAPI.upload(fd)
    form.coverUrl = res.data.fileUrl
    ElMessage.success('封面上传成功')
  } finally {
    uploading.value = false
  }
}

async function save(draft = false) {
  if (!form.title || !form.categoryId) {
    ElMessage.warning('标题和分类不能为空')
    return
  }
  form.draft = draft
  loading.value = true
  try {
    if (isEdit.value) {
      await articleAPI.update(route.params.id, { ...form })
      ElMessage.success('修改成功')
    } else {
      await articleAPI.create({ ...form })
      ElMessage.success(draft ? '草稿已保存' : '已提交审核')
    }
    router.push('/')
  } finally {
    loading.value = false
  }
}

onMounted(() => { loadCategories(); loadArticle() })
</script>

<template>
  <div style="max-width: 900px; margin: 0 auto">
    <h2>{{ isEdit ? '编辑文章' : '写文章' }}</h2>
    <el-form :model="form" label-width="80px">
      <el-form-item label="标题">
        <el-input v-model="form.title" placeholder="请输入文章标题" maxlength="100" show-word-limit />
      </el-form-item>
      <el-form-item label="分类">
        <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 200px">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="简介">
        <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="简要介绍文章内容（选填）" maxlength="200" show-word-limit />
      </el-form-item>
      <el-form-item label="封面">
        <div style="display: flex; gap: 10px; align-items: center">
          <el-upload
            :auto-upload="false"
            :show-file-list="false"
            accept="image/*"
            @change="handleUpload"
          >
            <el-button :loading="uploading">上传封面图片</el-button>
          </el-upload>
          <span style="color: #999; font-size: 12px">支持 jpg/png/gif，最大 10MB</span>
        </div>
        <img v-if="form.coverUrl" :src="form.coverUrl" style="margin-top: 8px; max-height: 150px; border-radius: 4px; display: block" />
      </el-form-item>
      <el-form-item label="正文">
        <el-input v-model="form.content" type="textarea" :rows="15" placeholder="文章正文（支持 HTML）" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="save(false)">提交审核</el-button>
        <el-button :loading="loading" @click="save(true)">保存草稿</el-button>
        <el-button @click="router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>
