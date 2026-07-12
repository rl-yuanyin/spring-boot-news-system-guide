<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import { articleAPI, categoryAPI, fileAPI } from '../api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const isEdit = ref(!!route.params.id)
const categories = ref([])
const loading = ref(false)
const form = reactive({ title: '', summary: '', content: '', coverUrl: '', categoryId: null, draft: false })
const uploading = ref(false)
const quillRef = ref(null)

// Quill 编辑器工具栏配置
const editorOptions = {
  modules: {
    toolbar: {
      container: [
        [{ header: [1, 2, false] }],
        ['bold', 'italic', 'underline', 'strike'],
        [{ color: [] }, { background: [] }],
        [{ list: 'ordered' }, { list: 'bullet' }],
        ['blockquote', 'code-block'],
        [{ align: [] }],
        ['link', 'image'],
        ['clean']
      ],
      handlers: {
        image: null  // 在 onMounted 中绑定自定义 handler
      }
    }
  },
  placeholder: '写下你的文章内容...',
  theme: 'snow'
}

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

// 封面上传（el-upload）
async function handleCoverUpload(uploadFile) {
  const file = uploadFile.raw
  if (!file) { ElMessage.error('文件读取失败，请重试'); return }
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

// Quill 编辑器内的图片上传
function createImageHandler(quillInstance) {
  const input = document.createElement('input')
  input.setAttribute('type', 'file')
  input.setAttribute('accept', 'image/*')
  input.addEventListener('change', async () => {
    const file = input.files?.[0]
    if (!file) return
    const fd = new FormData()
    fd.append('file', file)
    try {
      const range = quillInstance.getSelection(true)
      // 先显示加载占位
      quillInstance.insertText(range.index, '上传中...', { color: '#999' }, 'user')
      const res = await fileAPI.upload(fd)
      // 删除占位文本
      quillInstance.deleteText(range.index, 4, 'user')
      // 插入上传后的图片
      quillInstance.insertEmbed(range.index, 'image', res.data.fileUrl, 'user')
      quillInstance.setSelection(range.index + 1)
    } catch {
      ElMessage.error('图片上传失败')
    }
  })
  return function () {
    input.click()
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

// 编辑器就绪后绑定图片上传 handler
function onEditorReady(quillInstance) {
  const toolbar = quillInstance.getModule('toolbar')
  if (toolbar) {
    toolbar.addHandler('image', createImageHandler(quillInstance))
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
            @change="handleCoverUpload"
          >
            <el-button :loading="uploading">上传封面图片</el-button>
          </el-upload>
          <span style="color: #999; font-size: 12px">支持 jpg/png/gif，最大 10MB</span>
        </div>
        <img v-if="form.coverUrl" :src="form.coverUrl" style="margin-top: 8px; max-height: 150px; border-radius: 4px; display: block" />
      </el-form-item>
      <el-form-item label="正文" class="editor-form-item">
        <div class="editor-wrapper">
          <QuillEditor
            ref="quillRef"
            v-model:content="form.content"
            :options="editorOptions"
            content-type="html"
            @ready="onEditorReady"
          />
        </div>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="save(false)">提交审核</el-button>
        <el-button :loading="loading" @click="save(true)">保存草稿</el-button>
        <el-button @click="router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
.editor-form-item :deep(.el-form-item__content) {
  display: block;
}

.editor-wrapper {
  width: 100%;
}

.editor-wrapper :deep(.ql-toolbar) {
  border-radius: 4px 4px 0 0;
  border: 1px solid #ccc;
}

.editor-wrapper :deep(.ql-container) {
  min-height: 400px;
  border-radius: 0 0 4px 4px;
  border: 1px solid #ccc;
  border-top: 0;
  font-size: 15px;
}

.editor-wrapper :deep(.ql-editor) {
  min-height: 380px;
  line-height: 1.8;
}
</style>
