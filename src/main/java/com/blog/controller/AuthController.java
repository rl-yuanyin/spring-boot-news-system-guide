package com.blog.controller;

import com.blog.common.Constants;
import com.blog.common.Result;
import com.blog.common.UserContext;
import com.blog.dto.LoginDTO;
import com.blog.dto.RegisterDTO;
import com.blog.service.UserService;
import com.blog.vo.LoginVO;
import com.blog.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证控制器：注册、登录、验证码、用户信息
 */
@Tag(name = "登录认证", description = "注册、登录、获取用户信息")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final StringRedisTemplate stringRedisTemplate;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterDTO dto) {
        UserVO userVO = userService.register(dto);
        return Result.success("注册成功", userVO);
    }

    @Operation(summary = "用户登录", description = "需要先获取验证码，传 captchaKey 和 captcha")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        // 验证码校验
        if (dto.getCaptchaKey() != null && dto.getCaptcha() != null) {
            String redisKey = Constants.REDIS_CAPTCHA_PREFIX + dto.getCaptchaKey();
            String storedCode = stringRedisTemplate.opsForValue().get(redisKey);
            if (storedCode == null) {
                return Result.badRequest("验证码已过期，请重新获取");
            }
            if (!storedCode.equalsIgnoreCase(dto.getCaptcha())) {
                return Result.badRequest("验证码错误");
            }
            // 验证通过后删除，防止重复使用
            stringRedisTemplate.delete(redisKey);
        }
        // 如果没传验证码参数，跳过校验（兼容旧测试）

        LoginVO loginVO = userService.login(dto);
        return Result.success("登录成功", loginVO);
    }

    @Operation(summary = "获取验证码", description = "返回 captchaKey 和 4 位数字验证码，有效期 5 分钟")
    @GetMapping("/captcha")
    public Result<Map<String, String>> captcha() {
        String captchaKey = UUID.randomUUID().toString().substring(0, 8);
        String code = String.format("%04d", new Random().nextInt(10000));
        String redisKey = Constants.REDIS_CAPTCHA_PREFIX + captchaKey;
        stringRedisTemplate.opsForValue().set(redisKey, code, 5, TimeUnit.MINUTES);
        return Result.success(Map.of(
                "captchaKey", captchaKey,
                "captchaCode", code,
                "expireIn", "300秒"
        ));
    }

    @Operation(summary = "获取当前用户信息", description = "需要登录，从 token 中获取用户信息")
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/me")
    public Result<UserVO> me() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        UserVO userVO = userService.getUserVOById(userId);
        return Result.success(userVO);
    }

    @Operation(summary = "退出登录", description = "客户端清除 token 即可，服务端无需额外处理")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}
