CREATE DATABASE IF NOT EXISTS blog_system
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE blog_system;

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    password    VARCHAR(255) NOT NULL COMMENT '加密后的密码',
    nickname    VARCHAR(50)  DEFAULT ''  COMMENT '昵称',
    avatar      VARCHAR(255) DEFAULT ''  COMMENT '头像地址',
    email       VARCHAR(100) DEFAULT ''  COMMENT '邮箱',
    role        TINYINT      DEFAULT 0   COMMENT '角色：0-普通用户 1-管理员',
    status      TINYINT      DEFAULT 0   COMMENT '状态：0-正常 1-禁用',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 分类表
CREATE TABLE IF NOT EXISTS category (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    name        VARCHAR(50) NOT NULL COMMENT '分类名称',
    sort        INT         DEFAULT 0 COMMENT '排序',
    status      TINYINT     DEFAULT 0 COMMENT '状态：0-启用 1-禁用',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- 文章表
CREATE TABLE IF NOT EXISTS article (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '文章ID',
    title         VARCHAR(100) NOT NULL              COMMENT '标题',
    summary       VARCHAR(200) DEFAULT ''            COMMENT '简介',
    content       TEXT                                COMMENT '正文',
    cover_url     VARCHAR(255) DEFAULT ''            COMMENT '封面图URL',
    category_id   BIGINT       NOT NULL              COMMENT '分类ID',
    user_id       BIGINT       NOT NULL              COMMENT '作者ID',
    status        TINYINT      DEFAULT 0             COMMENT '0-草稿 1-待审核 2-已发布 3-驳回',
    view_count    INT          DEFAULT 0             COMMENT '浏览量',
    like_count    INT          DEFAULT 0             COMMENT '点赞数',
    is_top        TINYINT      DEFAULT 0             COMMENT '是否置顶',
    reject_reason VARCHAR(255) DEFAULT ''            COMMENT '驳回原因',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_category_id (category_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- 评论表
CREATE TABLE IF NOT EXISTS comment (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评论ID',
    article_id  BIGINT       NOT NULL              COMMENT '文章ID',
    user_id     BIGINT       NOT NULL              COMMENT '评论用户ID',
    parent_id   BIGINT       DEFAULT NULL          COMMENT '父评论ID',
    content     VARCHAR(500) NOT NULL              COMMENT '评论内容',
    status      TINYINT      DEFAULT 0             COMMENT '0-正常 1-隐藏 2-删除',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_article_id (article_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 文件记录表
CREATE TABLE IF NOT EXISTS file_record (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '文件ID',
    original_name VARCHAR(255) NOT NULL COMMENT '原文件名',
    file_name     VARCHAR(255) NOT NULL COMMENT '存储文件名（UUID）',
    file_url      VARCHAR(500) NOT NULL COMMENT '文件访问地址',
    file_size     BIGINT       NOT NULL COMMENT '文件大小（字节）',
    file_type     VARCHAR(50)  DEFAULT '' COMMENT 'MIME类型',
    user_id       BIGINT       NOT NULL              COMMENT '上传者ID',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件记录表';

-- 文章点赞表
CREATE TABLE IF NOT EXISTS article_like (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    article_id  BIGINT   NOT NULL              COMMENT '文章ID',
    user_id     BIGINT   NOT NULL              COMMENT '用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    UNIQUE KEY uk_article_user (article_id, user_id),
    INDEX idx_article_id (article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章点赞表';

-- 通知表
CREATE TABLE IF NOT EXISTS notification (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '通知ID',
    user_id     BIGINT       NOT NULL              COMMENT '接收者用户ID',
    type        VARCHAR(30)  NOT NULL              COMMENT '类型：like/comment/admin_delete',
    content     VARCHAR(500) NOT NULL              COMMENT '通知内容',
    article_id  BIGINT       DEFAULT NULL          COMMENT '关联文章ID',
    is_read     TINYINT      DEFAULT 0             COMMENT '0-未读 1-已读',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '通知时间',
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 插入超级管理员账号（密码 123456 的 BCrypt 哈希，role=2 为超级管理员）
INSERT IGNORE INTO user (username, password, nickname, role, status) VALUES
('admin', '$2a$10$j.kzj3sK9im6Tq/pNi.EQuyjfqF3dLTCG5wjG7GdtQThKxlfcFivG', '超级管理员', 2, 0);

-- 插入默认分类
INSERT IGNORE INTO category (id, name, sort, status) VALUES
(1, '科技', 1, 0),
(2, '财经', 2, 0),
(3, '体育', 3, 0),
(4, '教育', 4, 0),
(5, '娱乐', 5, 0);

-- 申诉表（被封禁用户提交申诉）
CREATE TABLE IF NOT EXISTS appeal (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '申诉ID',
    username    VARCHAR(50)  NOT NULL COMMENT '被封禁的用户名',
    reason      VARCHAR(500) NOT NULL COMMENT '申诉理由',
    status      TINYINT      DEFAULT 0   COMMENT '0-待处理 1-已通过 2-已拒绝',
    reply       VARCHAR(500) DEFAULT ''  COMMENT '管理员回复',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='申诉表';

-- 管理员申请表
CREATE TABLE IF NOT EXISTS admin_apply (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '申请ID',
    user_id       BIGINT       NOT NULL              COMMENT '申请人ID',
    reason        VARCHAR(500) DEFAULT ''            COMMENT '申请理由',
    article_count INT          DEFAULT 0             COMMENT '申请时的已发布文章数',
    total_views   BIGINT       DEFAULT 0             COMMENT '申请时的总浏览量',
    status        TINYINT      DEFAULT 0             COMMENT '0-待审核 1-已通过 2-已拒绝',
    reply         VARCHAR(500) DEFAULT ''            COMMENT '管理员回复',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员申请表';
