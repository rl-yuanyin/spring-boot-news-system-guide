package com.blog.common;

/**
 * 系统常量
 */
public final class Constants {

    private Constants() {
    }

    // ========== 用户角色 ==========
    /** 普通用户 */
    public static final int ROLE_USER = 0;
    /** 管理员 */
    public static final int ROLE_ADMIN = 1;
    /** 超级管理员 */
    public static final int ROLE_SUPER_ADMIN = 2;

    // ========== 用户状态 ==========
    /** 正常 */
    public static final int USER_STATUS_NORMAL = 0;
    /** 禁用 */
    public static final int USER_STATUS_DISABLED = 1;

    // ========== 文章状态 ==========
    /** 草稿 */
    public static final int ARTICLE_STATUS_DRAFT = 0;
    /** 待审核 */
    public static final int ARTICLE_STATUS_PENDING = 1;
    /** 已发布 */
    public static final int ARTICLE_STATUS_PUBLISHED = 2;
    /** 已驳回 */
    public static final int ARTICLE_STATUS_REJECTED = 3;

    // ========== 评论状态 ==========
    /** 正常 */
    public static final int COMMENT_STATUS_NORMAL = 0;
    /** 隐藏 */
    public static final int COMMENT_STATUS_HIDDEN = 1;
    /** 删除 */
    public static final int COMMENT_STATUS_DELETED = 2;

    // ========== 分类状态 ==========
    /** 启用 */
    public static final int CATEGORY_STATUS_ENABLED = 0;
    /** 禁用 */
    public static final int CATEGORY_STATUS_DISABLED = 1;

    // ========== Redis Key 前缀 ==========
    /** 验证码 key 前缀 */
    public static final String REDIS_CAPTCHA_PREFIX = "captcha:";
    /** 热门文章 key */
    public static final String REDIS_HOT_ARTICLES = "hot_articles";
    /** 文章浏览量 key 前缀 */
    public static final String REDIS_ARTICLE_VIEW_PREFIX = "article_view:";

    // ========== 其他 ==========
    /** 默认密码 */
    public static final String DEFAULT_PASSWORD = "123456";
    /** 默认头像 */
    public static final String DEFAULT_AVATAR = "/uploads/default-avatar.png";
}
