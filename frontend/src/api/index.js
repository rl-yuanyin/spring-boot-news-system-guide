import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({ baseURL: '/api' })

// 请求拦截：自动带 token
http.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 响应拦截：统一处理错误
http.interceptors.response.use(
  res => res.data,
  err => {
    const msg = err.response?.data?.message || '请求失败'
    ElMessage.error(msg)
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

// ===== 认证 =====
export const authAPI = {
  captcha: () => http.get('/auth/captcha'),
  login: (data) => http.post('/auth/login', data),
  register: (data) => http.post('/auth/register', data),
  me: () => http.get('/auth/me'),
}

// ===== 分类 =====
export const categoryAPI = {
  list: () => http.get('/categories'),
  add: (data) => http.post('/categories', data),
  update: (id, data) => http.put(`/categories/${id}`, data),
  del: (id) => http.delete(`/categories/${id}`),
}

// ===== 文章 =====
export const articleAPI = {
  list: (params) => http.get('/articles', { params }),
  hot: (limit = 5) => http.get('/articles/hot', { params: { limit } }),
  detail: (id) => http.get(`/articles/${id}`),
  create: (data) => http.post('/articles', data),
  update: (id, data) => http.put(`/articles/${id}`, data),
  del: (id) => http.delete(`/articles/${id}`),
  like: (id) => http.post(`/articles/${id}/like`),
  submit: (id) => http.post(`/articles/${id}/submit`),
}

// ===== 管理员 =====
export const adminAPI = {
  pending: (params) => http.get('/admin/articles/pending', { params }),
  approve: (id) => http.post(`/admin/articles/${id}/approve`),
  reject: (id, reason) => http.post(`/admin/articles/${id}/reject`, { reason }),
  exportExcel: () => http.get('/admin/articles/export', { responseType: 'blob' }),
}

// ===== 评论 =====
export const commentAPI = {
  list: (articleId) => http.get(`/articles/${articleId}/comments`),
  create: (data) => http.post('/comments', data),
  del: (id) => http.delete(`/comments/${id}`),
}

// ===== 通知 =====
export const notifyAPI = {
  list: () => http.get('/notifications'),
  unreadCount: () => http.get('/notifications/unread'),
  read: (id) => http.put(`/notifications/${id}/read`),
  readAll: () => http.put('/notifications/read-all'),
}

// ===== 管理员删文 =====
export const adminArticleAPI = {
  deleteWithReason: (id, reason) => http.post(`/admin/articles/${id}/delete`, { reason }),
}

// ===== 文件 =====
export const fileAPI = {
  upload: (formData) => http.post('/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
  list: () => http.get('/files'),
  del: (id) => http.delete(`/files/${id}`),
}

export default http
