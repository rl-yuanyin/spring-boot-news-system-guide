package com.blog.interceptor;

import com.blog.common.Result;
import com.blog.common.UserContext;
import com.blog.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器
 * <p>
 * 从 Authorization header 中提取 JWT token，校验并设置用户上下文。
 * 公开接口在白名单中配置，不校验 token。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        // OPTIONS 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 从 Authorization header 获取 token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "未登录，请先登录");
            return false;
        }

        String token = authHeader.substring(7);

        // 校验 token
        if (!jwtUtils.validateToken(token)) {
            writeUnauthorized(response, "token 无效或已过期");
            return false;
        }

        // 解析用户信息并存入 ThreadLocal
        try {
            Long userId = jwtUtils.getUserId(token);
            String username = jwtUtils.getUsername(token);
            int role = jwtUtils.getRole(token);
            UserContext.set(new UserContext.UserInfo(userId, username, role));
        } catch (Exception e) {
            log.error("解析 token 失败", e);
            writeUnauthorized(response, "token 解析失败");
            return false;
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        // 请求结束后清除 ThreadLocal，防止内存泄漏
        UserContext.remove();
    }

    /**
     * 返回 401 未授权响应
     */
    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = Result.unauthorized(message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
