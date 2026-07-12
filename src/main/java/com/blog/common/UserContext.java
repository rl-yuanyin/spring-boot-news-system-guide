package com.blog.common;

/**
 * 用户上下文（ThreadLocal）
 * <p>
 * 在请求处理过程中，存储当前登录用户的信息。
 * 请求结束后务必调用 remove() 清除，防止内存泄漏。
 */
public final class UserContext {

    private UserContext() {
    }

    private static final ThreadLocal<UserInfo> USER_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前用户
     */
    public static void set(UserInfo userInfo) {
        USER_HOLDER.set(userInfo);
    }

    /**
     * 获取当前用户
     */
    public static UserInfo get() {
        return USER_HOLDER.get();
    }

    /**
     * 获取当前用户 ID
     */
    public static Long getCurrentUserId() {
        UserInfo user = get();
        return user != null ? user.userId() : null;
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        UserInfo user = get();
        return user != null ? user.username() : null;
    }

    /**
     * 获取当前用户角色
     */
    public static Integer getCurrentRole() {
        UserInfo user = get();
        return user != null ? user.role() : null;
    }

    /**
     * 是否管理员
     */
    public static boolean isAdmin() {
        return getCurrentRole() != null && getCurrentRole() == 1;
    }

    /**
     * 清除（请求结束后调用）
     */
    public static void remove() {
        USER_HOLDER.remove();
    }

    /**
     * 用户简要信息（存入 ThreadLocal）
     */
    public record UserInfo(Long userId, String username, Integer role) {
    }
}
