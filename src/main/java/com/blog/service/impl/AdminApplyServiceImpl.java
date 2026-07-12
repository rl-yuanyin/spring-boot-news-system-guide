package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.Constants;
import com.blog.entity.AdminApply;
import com.blog.entity.Article;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.mapper.AdminApplyMapper;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.AdminApplyService;
import com.blog.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminApplyServiceImpl extends ServiceImpl<AdminApplyMapper, AdminApply> implements AdminApplyService {

    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    private static final int MIN_ARTICLES = 5;
    private static final long MIN_VIEWS = 1000;

    @Override
    public void submit(Long userId, String reason) {
        // 检查是否已有待处理申请
        AdminApply existing = getOne(new LambdaQueryWrapper<AdminApply>()
                .eq(AdminApply::getUserId, userId)
                .eq(AdminApply::getStatus, 0));
        if (existing != null) throw BusinessException.badRequest("已有待审核的申请");

        // 统计文章数和浏览量
        long articleCount = articleMapper.selectCount(new LambdaQueryWrapper<Article>()
                .eq(Article::getUserId, userId)
                .eq(Article::getStatus, Constants.ARTICLE_STATUS_PUBLISHED));
        long totalViews = articleMapper.selectList(new LambdaQueryWrapper<Article>()
                .eq(Article::getUserId, userId)
                .eq(Article::getStatus, Constants.ARTICLE_STATUS_PUBLISHED))
                .stream().mapToLong(a -> a.getViewCount() != null ? a.getViewCount() : 0).sum();

        if (articleCount < MIN_ARTICLES || totalViews < MIN_VIEWS) {
            throw BusinessException.badRequest(
                String.format("申请条件不满足：需发表%d篇以上文章且累计%d次以上浏览量", MIN_ARTICLES, MIN_VIEWS));
        }

        AdminApply apply = AdminApply.builder()
                .userId(userId).reason(reason)
                .articleCount((int) articleCount).totalViews(totalViews)
                .status(0).reply("").build();
        save(apply);
    }

    @Override
    public AdminApply getByUserId(Long userId) {
        return getOne(new LambdaQueryWrapper<AdminApply>()
                .eq(AdminApply::getUserId, userId)
                .orderByDesc(AdminApply::getCreateTime)
                .last("LIMIT 1"));
    }

    @Override
    public Page<AdminApply> listApplies(int pageNum, int pageSize) {
        return page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<AdminApply>().orderByDesc(AdminApply::getCreateTime));
    }

    @Override
    public void approve(Long id) {
        AdminApply apply = getById(id);
        if (apply == null) throw BusinessException.notFound("申请不存在");
        apply.setStatus(1);
        updateById(apply);
        // 提升用户为管理员
        User user = userMapper.selectById(apply.getUserId());
        if (user != null && user.getRole() == 0) {
            user.setRole(Constants.ROLE_ADMIN);
            userMapper.updateById(user);
            notificationService.send(user.getId(), "admin_delete",
                    "你的管理员申请已通过，现在你已成为管理员", null);
        }
    }

    @Override
    public void reject(Long id, String reply) {
        AdminApply apply = getById(id);
        if (apply == null) throw BusinessException.notFound("申请不存在");
        apply.setStatus(2);
        apply.setReply(reply);
        updateById(apply);
    }
}
