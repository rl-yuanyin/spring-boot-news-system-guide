# 新闻内容管理系统

基于 Spring Boot 3 + Vue 3 的新闻/博客内容管理平台，支持用户注册登录、文章发布审核、评论互动、文件上传、Redis 缓存和 Excel 导出。

## 快速开始

### 环境要求

- JDK 21+  |  Maven 3.9+  |  Node.js 18+
- MySQL 8.0  |  Redis

### 1. 初始化数据库

```bash
mysql -u root -p < db/init.sql
```

脚本会创建 `blog_system` 数据库、全部 7 张表，并插入管理员账号和默认分类。

### 2. 修改配置

编辑 `src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    username: root
    password: 你的密码
```

### 3. 启动后端

```bash
mvn spring-boot:run
```

后端运行在 `http://localhost:8080`，接口文档 `http://localhost:8080/doc.html`。

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端运行在 `http://localhost:3000`。

### 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 普通用户 | testuser | 123456 |

---

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.3.5 | 后端框架 |
| MyBatis-Plus | 3.5.7 | ORM |
| MySQL | 8.0 | 关系数据库 |
| Redis | — | 验证码 / 热门排行 |
| JWT (jjwt) | 0.12.6 | 身份认证 |
| Knife4j | 4.5.0 | 接口文档 |
| Apache POI | — | Excel 导出 |
| Vue 3 | — | 前端框架 |
| Element Plus | — | UI 组件库 |
| Vite | — | 构建工具 |

---

## 项目结构

```
spring-boot-news-system-guide/
├── README.md
├── pom.xml
├── api-test.http                    # HTTP Client 测试文件
├── 需求汇总.md
├── db/
│   └── init.sql                     # 数据库初始化脚本
│
├── src/main/java/com/blog/
│   ├── BlogApplication.java         # 启动类
│   ├── common/                      # Result, PageResult, Constants, UserContext
│   ├── config/                      # WebMvc, Security, MyBatisPlus, Redis, Knife4j
│   ├── entity/                      # User, Category, Article, Comment, FileRecord, ArticleLike, Notification
│   ├── dto/                         # 入参对象 (7 个)
│   ├── vo/                          # 出参对象 (5 个)
│   ├── mapper/                      # MyBatis-Plus BaseMapper (7 个)
│   ├── service/                     # 业务接口 + impl (6 组)
│   ├── controller/                  # REST 控制器 (8 个)
│   ├── interceptor/                 # JWT 登录拦截器
│   ├── exception/                   # 全局异常处理
│   └── utils/                       # JwtUtils
│
├── src/main/resources/
│   └── application.yml              # 全部配置
│
└── frontend/                        # Vue 3 前端项目
    └── src/
        ├── api/index.js             # Axios API 封装 (含拦截器)
        ├── router/index.js          # Vue Router + 路由守卫
        ├── layouts/MainLayout.vue   # 主布局 (导航栏)
        └── views/                   # 9 个页面组件
            ├── Login.vue
            ├── Register.vue
            ├── Home.vue
            ├── ArticleDetail.vue
            ├── ArticleEdit.vue
            ├── Drafts.vue
            ├── Profile.vue
            ├── Notifications.vue
            └── admin/
                ├── Review.vue
                └── Categories.vue
```

---

## 数据库设计

```
user ──┐                    article_like
       ├── article ───────── article_id, user_id (unique)
       │     ├── comment ─── article_id, user_id, parent_id
       │     └── (category_id → category)
       ├── file_record
       └── notification
```

7 张表：`user`, `category`, `article`, `comment`, `file_record`, `article_like`, `notification`

---

## 功能清单

### 认证

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/auth/register` | POST | 注册 |
| `/api/auth/login` | POST | 登录 (含 Redis 验证码) |
| `/api/auth/captcha` | GET | 获取验证码 |
| `/api/auth/me` | GET | 当前用户信息 |
| `/api/auth/logout` | POST | 退出 |

### 文章

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/articles` | GET | 分页列表 (关键词/分类/作者搜索) |
| `/api/articles/hot` | GET | 热门排行 (Redis ZSet) |
| `/api/articles/{id}` | GET | 详情 (浏览量+1) |
| `/api/articles` | POST | 发布 (草稿/提交审核) |
| `/api/articles/{id}` | PUT | 修改 (作者/管理员) |
| `/api/articles/{id}` | DELETE | 删除 (作者/管理员) |
| `/api/articles/{id}/like` | POST | 点赞/取消 |
| `/api/articles/{id}/submit` | POST | 提交审核 |

### 管理员

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/admin/articles/pending` | GET | 待审核列表 |
| `/api/admin/articles/{id}/approve` | POST | 审核通过 |
| `/api/admin/articles/{id}/reject` | POST | 驳回 (含原因) |
| `/api/admin/articles/{id}/delete` | POST | 删文 (含原因+通知) |
| `/api/admin/articles/export` | GET | 导出 Excel |

### 分类 / 评论 / 文件 / 通知

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/categories` | GET/POST | 分类列表/新增 |
| `/api/categories/{id}` | PUT/DELETE | 分类修改/删除 |
| `/api/articles/{id}/comments` | GET | 文章评论 |
| `/api/comments` | POST | 发表评论 |
| `/api/comments/{id}` | DELETE | 删除评论 |
| `/api/files/upload` | POST | 上传文件 |
| `/api/files` | GET | 文件列表 |
| `/api/files/{id}` | DELETE | 删除文件 |
| `/api/notifications` | GET | 通知列表 |
| `/api/notifications/unread` | GET | 未读数 |
| `/api/notifications/{id}/read` | PUT | 标记已读 |

---

## 鉴权架构

```
请求 → LoginInterceptor (拦截 /api/**)
         │
         ├─ 白名单路径 → Controller 手动解析 JWT
         │   └─ 公开读 / 登录写 混合权限接口
         │
         └─ 其余路径 → 强制校验 token → 存入 ThreadLocal
```

---

## 文章状态流转

```
创建 → 草稿(0) → 提交 → 待审核(1) → 通过 → 已发布(2)
                         ↓
                       驳回(3) → 修改重提 → 待审核(1)
```

---

## 测试

### HTTP Client（推荐）

在 IntelliJ IDEA 中打开 `api-test.http`，按编号顺序执行：

1. 先执行"获取验证码"和"登录" — token 自动保存到全局变量
2. 之后所有接口自动携带 token
3. 创建资源后 ID 自动捕获到变量（如 `{{publishedArticleId}}`）

### 接口文档

启动后端后访问 `http://localhost:8080/doc.html`

### 命令行

```bash
# 登录获取 token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 查询文章列表
curl http://localhost:8080/api/articles?page=1&pageSize=10
```
