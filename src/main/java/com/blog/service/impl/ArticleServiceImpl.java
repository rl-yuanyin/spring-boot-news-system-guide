package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.Constants;
import com.blog.common.PageResult;
import com.blog.dto.ArticleCreateDTO;
import com.blog.dto.ArticleQueryDTO;
import com.blog.entity.Article;
import com.blog.entity.ArticleLike;
import com.blog.entity.Category;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.mapper.ArticleLikeMapper;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CategoryMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.ArticleService;
import com.blog.service.NotificationService;
import com.blog.vo.ArticleDetailVO;
import com.blog.vo.ArticleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文章服务实现
 */
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;
    private final ArticleLikeMapper articleLikeMapper;
    private final NotificationService notificationService;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public PageResult<ArticleVO> pageQuery(ArticleQueryDTO dto, Long userId) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();

        // 默认查已发布文章；如果传了 status 参数则按传参查询
        if (dto.getStatus() != null) {
            wrapper.eq(Article::getStatus, dto.getStatus());
            // 草稿箱私有：查草稿时只查自己的（包括管理员）
            if (dto.getStatus() == Constants.ARTICLE_STATUS_DRAFT && userId != null) {
                wrapper.eq(Article::getUserId, userId);
            }
        } else {
            wrapper.eq(Article::getStatus, Constants.ARTICLE_STATUS_PUBLISHED);
        }

        // 关键词搜索（标题模糊匹配 + 作者名匹配）
        if (StringUtils.hasText(dto.getKeyword())) {
            // 先查匹配关键词的用户 ID
            List<Long> matchedUserIds = userMapper.selectList(
                new LambdaQueryWrapper<User>().like(User::getNickname, dto.getKeyword())
            ).stream().map(User::getId).toList();
            // 标题或作者匹配
            wrapper.and(w -> w
                .like(Article::getTitle, dto.getKeyword())
                .or(!matchedUserIds.isEmpty(), w2 -> w2.in(Article::getUserId, matchedUserIds)));
        }

        // 分类筛选
        if (dto.getCategoryId() != null) {
            wrapper.eq(Article::getCategoryId, dto.getCategoryId());
        }

        // 置顶筛选
        if (dto.getIsTop() != null) {
            wrapper.eq(Article::getIsTop, dto.getIsTop());
        }

        // 按置顶降序 + 创建时间降序
        wrapper.orderByDesc(Article::getIsTop)
                .orderByDesc(Article::getCreateTime);

        Page<Article> page = new Page<>(dto.getPage(), dto.getPageSize());
        page = page(page, wrapper);

        // 如果没有数据，直接返回空结果
        if (page.getRecords().isEmpty()) {
            return PageResult.of(page, List.of());
        }

        // 批量查询分类名称和作者昵称
        Set<Long> categoryIds = page.getRecords().stream()
                .map(Article::getCategoryId).collect(Collectors.toSet());
        Set<Long> userIds = page.getRecords().stream()
                .map(Article::getUserId).collect(Collectors.toSet());

        Map<Long, String> categoryNameMap = categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        Map<Long, String> userNameMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname, (a, b) -> a));

        // 转换为 VO
        var voList = page.getRecords().stream().map(a -> ArticleVO.builder()
                .id(a.getId())
                .title(a.getTitle())
                .summary(a.getSummary())
                .coverUrl(a.getCoverUrl())
                .categoryId(a.getCategoryId())
                .categoryName(categoryNameMap.getOrDefault(a.getCategoryId(), "未知"))
                .userId(a.getUserId())
                .authorName(userNameMap.getOrDefault(a.getUserId(), "未知"))
                .status(a.getStatus())
                .viewCount(a.getViewCount())
                .likeCount(a.getLikeCount())
                .isTop(a.getIsTop())
                .createTime(a.getCreateTime())
                .updateTime(a.getUpdateTime())
                .build()).collect(Collectors.toList());

        return PageResult.of(page, voList);
    }

    @Override
    @Transactional
    public ArticleDetailVO getDetail(Long id) {
        Article article = getById(id);
        if (article == null) {
            throw BusinessException.notFound("文章不存在");
        }

        // 浏览量 +1（DB）
        article.setViewCount(article.getViewCount() + 1);
        updateById(article);

        // 同步到 Redis：浏览量计数 + ZSet 热门排行
        String viewKey = Constants.REDIS_ARTICLE_VIEW_PREFIX + id;
        stringRedisTemplate.opsForValue().increment(viewKey);
        stringRedisTemplate.opsForZSet().incrementScore(
                Constants.REDIS_HOT_ARTICLES, String.valueOf(id), 1);

        // 查询分类名称
        Category category = categoryMapper.selectById(article.getCategoryId());
        String categoryName = category != null ? category.getName() : "未知";

        // 查询作者昵称
        User author = userMapper.selectById(article.getUserId());
        String authorName = author != null ? author.getNickname() : "未知";

        return ArticleDetailVO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .summary(article.getSummary())
                .content(article.getContent())
                .coverUrl(article.getCoverUrl())
                .categoryId(article.getCategoryId())
                .categoryName(categoryName)
                .userId(article.getUserId())
                .authorName(authorName)
                .status(article.getStatus())
                .viewCount(article.getViewCount())
                .likeCount(article.getLikeCount())
                .isTop(article.getIsTop())
                .rejectReason(article.getRejectReason())
                .createTime(article.getCreateTime())
                .updateTime(article.getUpdateTime())
                .build();
    }

    @Override
    public Article create(ArticleCreateDTO dto, Long userId) {
        // 校验分类存在
        Category category = categoryMapper.selectById(dto.getCategoryId());
        if (category == null || category.getStatus() != Constants.CATEGORY_STATUS_ENABLED) {
            throw BusinessException.badRequest("分类不存在或已禁用");
        }

        // draft=true → 草稿，否则提交审核
        int status = Boolean.TRUE.equals(dto.getDraft())
                ? Constants.ARTICLE_STATUS_DRAFT
                : Constants.ARTICLE_STATUS_PENDING;

        Article article = Article.builder()
                .title(dto.getTitle())
                .summary(dto.getSummary() != null ? dto.getSummary() : "")
                .content(dto.getContent() != null ? dto.getContent() : "")
                .coverUrl(dto.getCoverUrl() != null ? dto.getCoverUrl() : "")
                .categoryId(dto.getCategoryId())
                .userId(userId)
                .status(status)
                .viewCount(0)
                .likeCount(0)
                .isTop(0)
                .rejectReason("")
                .build();
        save(article);
        return article;
    }

    @Override
    public Article update(Long id, ArticleCreateDTO dto, Long userId) {
        Article article = getById(id);
        if (article == null) {
            throw BusinessException.notFound("文章不存在");
        }

        // 权限校验：只有作者本人或管理员可以修改
        checkOwnerOrAdmin(article, userId);

        // 校验分类存在
        Category category = categoryMapper.selectById(dto.getCategoryId());
        if (category == null || category.getStatus() != Constants.CATEGORY_STATUS_ENABLED) {
            throw BusinessException.badRequest("分类不存在或已禁用");
        }

        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary() != null ? dto.getSummary() : "");
        article.setContent(dto.getContent() != null ? dto.getContent() : "");
        article.setCoverUrl(dto.getCoverUrl() != null ? dto.getCoverUrl() : "");
        article.setCategoryId(dto.getCategoryId());

        // 如果传了 draft 参数，更新状态
        if (dto.getDraft() != null) {
            article.setStatus(Boolean.TRUE.equals(dto.getDraft())
                    ? Constants.ARTICLE_STATUS_DRAFT
                    : Constants.ARTICLE_STATUS_PENDING);
        }

        updateById(article);
        return article;
    }

    @Override
    public void delete(Long id, Long userId) {
        Article article = getById(id);
        if (article == null) {
            throw BusinessException.notFound("文章不存在");
        }

        // 权限校验：只有作者本人或管理员可以删除
        checkOwnerOrAdmin(article, userId);

        removeById(id);
    }

    @Override
    public void submit(Long id, Long userId) {
        Article article = getById(id);
        if (article == null) {
            throw BusinessException.notFound("文章不存在");
        }

        // 权限校验：只有作者本人可以提交审核
        if (!article.getUserId().equals(userId)) {
            throw BusinessException.forbidden("只能提交自己的文章");
        }

        // 只有草稿和驳回状态可以提交审核
        if (article.getStatus() != Constants.ARTICLE_STATUS_DRAFT
                && article.getStatus() != Constants.ARTICLE_STATUS_REJECTED) {
            throw BusinessException.badRequest("当前状态不可提交审核");
        }

        article.setStatus(Constants.ARTICLE_STATUS_PENDING);
        article.setRejectReason(""); // 清除旧的驳回原因
        updateById(article);
    }

    @Override
    public long countByCategoryId(Long categoryId) {
        return count(new LambdaQueryWrapper<Article>()
                .eq(Article::getCategoryId, categoryId));
    }

    @Override
    public PageResult<ArticleVO> pagePending(ArticleQueryDTO dto) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();

        // 只查待审核状态
        wrapper.eq(Article::getStatus, Constants.ARTICLE_STATUS_PENDING);

        // 关键词搜索
        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.like(Article::getTitle, dto.getKeyword());
        }

        // 按创建时间升序（早提交的优先审核）
        wrapper.orderByAsc(Article::getCreateTime);

        Page<Article> page = new Page<>(dto.getPage(), dto.getPageSize());
        page = page(page, wrapper);

        if (page.getRecords().isEmpty()) {
            return PageResult.of(page, List.of());
        }

        // 批量查询分类和作者名称
        Set<Long> categoryIds = page.getRecords().stream()
                .map(Article::getCategoryId).collect(Collectors.toSet());
        Set<Long> userIds = page.getRecords().stream()
                .map(Article::getUserId).collect(Collectors.toSet());

        Map<Long, String> categoryNameMap = categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        Map<Long, String> userNameMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname, (a, b) -> a));

        var voList = page.getRecords().stream().map(a -> ArticleVO.builder()
                .id(a.getId())
                .title(a.getTitle())
                .summary(a.getSummary())
                .coverUrl(a.getCoverUrl())
                .categoryId(a.getCategoryId())
                .categoryName(categoryNameMap.getOrDefault(a.getCategoryId(), "未知"))
                .userId(a.getUserId())
                .authorName(userNameMap.getOrDefault(a.getUserId(), "未知"))
                .status(a.getStatus())
                .viewCount(a.getViewCount())
                .likeCount(a.getLikeCount())
                .isTop(a.getIsTop())
                .createTime(a.getCreateTime())
                .updateTime(a.getUpdateTime())
                .build()).collect(Collectors.toList());

        return PageResult.of(page, voList);
    }

    @Override
    public void approve(Long id) {
        Article article = getById(id);
        if (article == null) {
            throw BusinessException.notFound("文章不存在");
        }
        if (article.getStatus() != Constants.ARTICLE_STATUS_PENDING) {
            throw BusinessException.badRequest("只有待审核状态的文章才能审核通过");
        }
        article.setStatus(Constants.ARTICLE_STATUS_PUBLISHED);
        updateById(article);
    }

    @Override
    public void reject(Long id, String reason) {
        Article article = getById(id);
        if (article == null) {
            throw BusinessException.notFound("文章不存在");
        }
        if (article.getStatus() != Constants.ARTICLE_STATUS_PENDING) {
            throw BusinessException.badRequest("只有待审核状态的文章才能驳回");
        }
        article.setStatus(Constants.ARTICLE_STATUS_REJECTED);
        article.setRejectReason(reason);
        updateById(article);
    }

    @Override
    public List<ArticleVO> getHotArticles(int limit) {
        // 从 Redis ZSet 获取浏览量最高的文章 ID
        Set<ZSetOperations.TypedTuple<String>> topSet =
                stringRedisTemplate.opsForZSet()
                        .reverseRangeWithScores(Constants.REDIS_HOT_ARTICLES, 0, limit - 1);

        if (topSet == null || topSet.isEmpty()) {
            return List.of();
        }

        // 提取文章 ID 列表
        List<Long> articleIds = topSet.stream()
                .map(t -> Long.valueOf(t.getValue()))
                .collect(Collectors.toList());

        // 从 DB 批量查询文章
        List<Article> articles = listByIds(articleIds);

        // 批量查分类名和作者名
        Set<Long> categoryIds = articles.stream()
                .map(Article::getCategoryId).collect(Collectors.toSet());
        Set<Long> userIds = articles.stream()
                .map(Article::getUserId).collect(Collectors.toSet());
        Map<Long, String> categoryNameMap = categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        Map<Long, String> userNameMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname, (a, b) -> a));

        // 按 ZSet 分数排序
        Map<Long, Double> scoreMap = topSet.stream()
                .collect(Collectors.toMap(
                        t -> Long.valueOf(t.getValue()),
                        t -> t.getScore() != null ? t.getScore() : 0.0));

        return articles.stream()
                .sorted((a, b) -> Double.compare(
                        scoreMap.getOrDefault(b.getId(), 0.0),
                        scoreMap.getOrDefault(a.getId(), 0.0)))
                .map(a -> ArticleVO.builder()
                        .id(a.getId())
                        .title(a.getTitle())
                        .summary(a.getSummary())
                        .coverUrl(a.getCoverUrl())
                        .categoryId(a.getCategoryId())
                        .categoryName(categoryNameMap.getOrDefault(a.getCategoryId(), "未知"))
                        .userId(a.getUserId())
                        .authorName(userNameMap.getOrDefault(a.getUserId(), "未知"))
                        .status(a.getStatus())
                        .viewCount(a.getViewCount())
                        .likeCount(a.getLikeCount())
                        .isTop(a.getIsTop())
                        .createTime(a.getCreateTime())
                        .updateTime(a.getUpdateTime())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<ArticleVO> pageAll(ArticleQueryDTO dto) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();

        // 排除草稿（状态 0），其余全部显示
        wrapper.ne(Article::getStatus, Constants.ARTICLE_STATUS_DRAFT);

        // 关键词搜索
        if (StringUtils.hasText(dto.getKeyword())) {
            List<Long> matchedUserIds = userMapper.selectList(
                new LambdaQueryWrapper<User>().like(User::getNickname, dto.getKeyword())
            ).stream().map(User::getId).toList();
            wrapper.and(w -> w
                .like(Article::getTitle, dto.getKeyword())
                .or(!matchedUserIds.isEmpty(), w2 -> w2.in(Article::getUserId, matchedUserIds)));
        }

        if (dto.getCategoryId() != null) {
            wrapper.eq(Article::getCategoryId, dto.getCategoryId());
        }

        wrapper.orderByDesc(Article::getCreateTime);

        Page<Article> page = new Page<>(dto.getPage(), dto.getPageSize());
        page = page(page, wrapper);

        if (page.getRecords().isEmpty()) {
            return PageResult.of(page, List.of());
        }

        return buildVOPage(page);
    }

    /**
     * 将 Article 分页结果批量转换为 VO（复用代码）
     */
    private PageResult<ArticleVO> buildVOPage(Page<Article> page) {
        Set<Long> categoryIds = page.getRecords().stream()
                .map(Article::getCategoryId).collect(Collectors.toSet());
        Set<Long> userIds = page.getRecords().stream()
                .map(Article::getUserId).collect(Collectors.toSet());

        Map<Long, String> categoryNameMap = categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        Map<Long, String> userNameMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname, (a, b) -> a));

        var voList = page.getRecords().stream().map(a -> ArticleVO.builder()
                .id(a.getId())
                .title(a.getTitle())
                .summary(a.getSummary())
                .coverUrl(a.getCoverUrl())
                .categoryId(a.getCategoryId())
                .categoryName(categoryNameMap.getOrDefault(a.getCategoryId(), "未知"))
                .userId(a.getUserId())
                .authorName(userNameMap.getOrDefault(a.getUserId(), "未知"))
                .status(a.getStatus())
                .viewCount(a.getViewCount())
                .likeCount(a.getLikeCount())
                .isTop(a.getIsTop())
                .createTime(a.getCreateTime())
                .updateTime(a.getUpdateTime())
                .build()).collect(Collectors.toList());

        return PageResult.of(page, voList);
    }

    @Override
    public boolean toggleLike(Long id, Long userId) {
        Article article = getById(id);
        if (article == null) {
            throw BusinessException.notFound("文章不存在");
        }

        // 检查是否已点赞
        var wrapper = new LambdaQueryWrapper<ArticleLike>()
                .eq(ArticleLike::getArticleId, id)
                .eq(ArticleLike::getUserId, userId);
        ArticleLike existing = articleLikeMapper.selectOne(wrapper);

        if (existing != null) {
            // 已点赞 → 取消
            articleLikeMapper.deleteById(existing.getId());
            article.setLikeCount(Math.max(0, article.getLikeCount() - 1));
            updateById(article);
            return false;
        } else {
            // 未点赞 → 点赞
            articleLikeMapper.insert(ArticleLike.builder()
                    .articleId(id).userId(userId).build());
            article.setLikeCount(article.getLikeCount() + 1);
            updateById(article);
            // 通知文章作者
            User liker = userMapper.selectById(userId);
            String likerName = liker != null ? liker.getNickname() : "用户";
            notificationService.send(article.getUserId(), "like",
                    likerName + " 赞了你的文章《" + article.getTitle() + "》", id);
            return true;
        }
    }

    /**
     * 校验操作者是作者本人或管理员
     */
    private void checkOwnerOrAdmin(Article article, Long userId) {
        if (userId == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        if (!article.getUserId().equals(userId) && !isAdmin(userId)) {
            throw BusinessException.forbidden("只能操作自己的文章");
        }
    }

    /**
     * 判断用户是否管理员角色
     */
    private boolean isAdmin(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null && user.getRole() == Constants.ROLE_ADMIN;
    }
}
