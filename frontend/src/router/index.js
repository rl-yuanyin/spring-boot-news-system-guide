import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { guest: true }
  },
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('../views/Home.vue') },
      { path: 'article/:id', name: 'ArticleDetail', component: () => import('../views/ArticleDetail.vue') },
      { path: 'write', name: 'ArticleWrite', component: () => import('../views/ArticleEdit.vue'), meta: { auth: true } },
      { path: 'edit/:id', name: 'ArticleEdit', component: () => import('../views/ArticleEdit.vue'), meta: { auth: true } },
      { path: 'drafts', name: 'Drafts', component: () => import('../views/Drafts.vue'), meta: { auth: true } },
      { path: 'notifications', name: 'Notifications', component: () => import('../views/Notifications.vue'), meta: { auth: true } },
      { path: 'profile', name: 'Profile', component: () => import('../views/Profile.vue'), meta: { auth: true } },
      // 管理端
      { path: 'admin/review', name: 'AdminReview', component: () => import('../views/admin/Review.vue'), meta: { auth: true, admin: true } },
      { path: 'admin/categories', name: 'AdminCategories', component: () => import('../views/admin/Categories.vue'), meta: { auth: true, admin: true } },
      { path: 'admin/users', name: 'AdminUsers', component: () => import('../views/admin/Users.vue'), meta: { auth: true, admin: true } },
      { path: 'admin/applications', name: 'AdminApplications', component: () => import('../views/admin/Applications.vue'), meta: { auth: true, admin: true } },
    ]
  },
  {
    path: '/banned',
    name: 'Banned',
    component: () => import('../views/Banned.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = Number(localStorage.getItem('role') || 0)

  if (to.meta.auth && !token) return next('/login')
  if (to.meta.admin && role < 1) return next('/')
  if (to.meta.guest && token) return next('/')

  next()
})

export default router
