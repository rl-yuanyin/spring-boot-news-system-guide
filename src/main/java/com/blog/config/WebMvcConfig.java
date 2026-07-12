package com.blog.config;

import com.blog.interceptor.LoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    /**
     * 跨域配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 静态资源映射：让上传的文件可以通过 URL 访问
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }

    /**
     * 注册登录拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/**")   // 拦截所有 API 请求
                .excludePathPatterns(          // 排除公开接口
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/captcha",
                        "/api/health",
                        "/api/categories",     // GET 查询分类公开
                        "/api/categories/**",   // 子路径也放行，写操作由 Controller 内 checkAdmin 保护
                        "/api/articles",        // GET 查询文章公开
                        "/api/articles/**",      // 子路径也放行，写操作由 Controller 内 checkLogin 保护
                        "/api/admin/**",         // 管理端接口，由 Controller 内 checkAdmin 保护
                        "/api/comments",         // GET 公开、POST 由 Controller 内 requireLogin 保护
                        "/api/comments/**",       // DELETE 由 Controller 内 requireLogin 保护
                        "/api/files",            // GET 公开，写操作由 Controller 内 requireLogin 保护
                        "/api/files/**",          // 上传/删除 由 Controller 内 requireLogin 保护
                        "/api/notifications",    // 通知接口由 Controller 内 requireLogin 保护
                        "/api/notifications/**",
                        "/api/appeal",           // 申诉提交公开（被封禁用户无法登录）
                        "/api/appeal/**",         // 申诉查询公开
                        "/api/admin-apply",       // 管理员申请由 Controller 内 requireLogin 保护
                        "/api/admin-apply/**"
                );
    }
}
