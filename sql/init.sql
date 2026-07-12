-- ============================================
-- 新闻内容管理系统 - 数据库初始化脚本
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `blog_system`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `blog_system`;

-- ============================================
-- 用户表
-- ============================================
CREATE TABLE IF NOT EXISTS `user` (
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    varchar(50)  NOT NULL COMMENT '用户名',
    `password`    varchar(255) NOT NULL COMMENT '加密后的密码',
    `nickname`    varchar(50)  DEFAULT NULL COMMENT '昵称',
    `avatar`      varchar(500) DEFAULT NULL COMMENT '头像地址',
    `email`       varchar(100) DEFAULT NULL COMMENT '邮箱',
    `role`        tinyint      NOT NULL DEFAULT 0 COMMENT '角色：0-普通用户 1-管理员',
    `status`      tinyint      NOT NULL DEFAULT 0 COMMENT '状态：0-正常 1-禁用',
    `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入默认管理员账号（密码：admin123，BCrypt 加密）
-- 实际密码会在应用启动时由代码处理

-- ============================================
-- 分类表
-- ============================================
CREATE TABLE IF NOT EXISTS `category` (
    `id`          bigint      NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name`        varchar(50) NOT NULL COMMENT '分类名称',
    `sort`        int         DEFAULT 0 COMMENT '排序',
    `status`      tinyint     NOT NULL DEFAULT 0 COMMENT '是否启用：0-启用 1-禁用',
    `create_time` datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';
