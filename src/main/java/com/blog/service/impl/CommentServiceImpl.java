package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.Constants;
import com.blog.dto.CommentDTO;
import com.blog.entity.Article;
import com.blog.entity.Comment;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.CommentService;
import com.blog.service.NotificationService;
import com.blog.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 评论服务实现
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    @Override
    public List<CommentVO> listByArticleId(Long articleId) {
        // 校验文章存在
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw BusinessException.notFound("文章不存在");
        }

        // 查询正常状态的评论，按时间倒序
        List<Comment> comments = list(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getArticleId, articleId)
                .eq(Comment::getStatus, Constants.COMMENT_STATUS_NORMAL)
                .orderByDesc(Comment::getCreateTime));

        if (comments.isEmpty()) {
            return List.of();
        }

        // 批量查询用户昵称和头像
        Set<Long> userIds = comments.stream()
                .map(Comment::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return comments.stream().map(c -> {
            User u = userMap.get(c.getUserId());
            return CommentVO.builder()
                    .id(c.getId())
                    .articleId(c.getArticleId())
                    .userId(c.getUserId())
                    .username(u != null ? u.getNickname() : "未知")
                    .avatar(u != null ? u.getAvatar() : null)
                    .parentId(c.getParentId())
                    .content(c.getContent())
                    .createTime(c.getCreateTime())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public CommentVO create(CommentDTO dto, Long userId) {
        // 校验文章存在
        Article article = articleMapper.selectById(dto.getArticleId());
        if (article == null) {
            throw BusinessException.notFound("文章不存在");
        }

        Comment comment = Comment.builder()
                .articleId(dto.getArticleId())
                .userId(userId)
                .parentId(dto.getParentId())
                .content(dto.getContent())
                .status(Constants.COMMENT_STATUS_NORMAL)
                .build();
        save(comment);

        // 通知文章作者（自己评论自己的文章不通知）
        if (!article.getUserId().equals(userId)) {
            User commenter = userMapper.selectById(userId);
            String name = commenter != null ? commenter.getNickname() : "用户";
            notificationService.send(article.getUserId(), "comment",
                    name + " 评论了你的文章《" + article.getTitle() + "》", article.getId());
        }

        // 返回 VO（含用户昵称）
        User user = userMapper.selectById(userId);
        return CommentVO.builder()
                .id(comment.getId())
                .articleId(comment.getArticleId())
                .userId(userId)
                .username(user != null ? user.getNickname() : "未知")
                .avatar(user != null ? user.getAvatar() : null)
                .parentId(comment.getParentId())
                .content(comment.getContent())
                .createTime(comment.getCreateTime())
                .build();
    }

    @Override
    public void delete(Long id, Long userId) {
        Comment comment = getById(id);
        if (comment == null) {
            throw BusinessException.notFound("评论不存在");
        }

        // 权限：用户删自己的，管理员删任意
        User user = userMapper.selectById(userId);
        boolean isAdmin = user != null && user.getRole() == Constants.ROLE_ADMIN;
        if (!comment.getUserId().equals(userId) && !isAdmin) {
            throw BusinessException.forbidden("只能删除自己的评论");
        }

        // 逻辑删除：标记为已删除
        comment.setStatus(Constants.COMMENT_STATUS_DELETED);
        updateById(comment);
    }
}
