<script setup>
import { ref, onMounted } from 'vue'
import { categoryAPI } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const categories = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({ id: null, name: '', sort: 0 })
const loading = ref(false)

async function load() {
  const res = await categoryAPI.list()
  categories.value = res.data || []
}

function openAdd() {
  isEdit.value = false
  form.value = { id: null, name: '', sort: 0 }
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

async function save() {
  if (!form.value.name) {
    ElMessage.warning('分类名不能为空')
    return
  }
  loading.value = true
  try {
    if (isEdit.value) {
      await categoryAPI.update(form.value.id, { name: form.value.name, sort: form.value.sort })
      ElMessage.success('修改成功')
    } else {
      await categoryAPI.add({ name: form.value.name, sort: form.value.sort })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    load()
  } finally {
    loading.value = false
  }
}

async function del(row) {
  await ElMessageBox.confirm(`确定删除分类"${row.name}"？`, '提示', { type: 'warning' })
  await categoryAPI.del(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 15px">
      <h2 style="margin: 0">分类管理</h2>
      <el-button type="primary" @click="openAdd">新增分类</el-button>
    </div>

    <el-table :data="categories" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" width="200" />
      <el-table-column prop="sort" label="排序" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'">{{ row.status === 0 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="openEdit(row)">编辑</el-button>
          <el-button type="danger" size="small" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" width="400px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="分类名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
