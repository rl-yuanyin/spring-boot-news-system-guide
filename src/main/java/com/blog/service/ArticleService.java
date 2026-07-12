package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.common.PageResult;
import com.blog.dto.ArticleCreateDTO;
import com.blog.dto.ArticleQueryDTO;
import com.blog.entity.Article;
import com.blog.vo.ArticleDetailVO;
import com.blog.vo.ArticleVO;

import java.util.List;

/**
 * 文章服务接口
 */
public interface ArticleService extends IService<Article> {

    /**
     * 分页查询文章列表
     *
     * @param dto    查询条件
     * @param userId 当前用户 ID（null 表示未登录）
     */
    PageResult<ArticleVO> pageQuery(ArticleQueryDTO dto, Long userId);

    /**
     * 查询文章详情（浏览量 +1）
     */
    ArticleDetailVO getDetail(Long id);

    /**
     * 创建文章（草稿或提交审核）
     *
     * @param dto    文章参数
     * @param userId 作者 ID
     */
    Article create(ArticleCreateDTO dto, Long userId);

    /**
     * 修改文章（作者本人或管理员）
     *
     * @param id     文章 ID
     * @param dto    文章参数
     * @param userId 操作者 ID
     */
    Article update(Long id, ArticleCreateDTO dto, Long userId);

    /**
     * 删除文章（作者本人或管理员）
     *
     * @param id     文章 ID
     * @param userId 操作者 ID
     */
    void delete(Long id, Long userId);

    /**
     * 提交审核（作者本人）
     *
     * @param id     文章 ID
     * @param userId 操作者 ID
     */
    void submit(Long id, Long userId);

    /**
     * 统计指定分类下的文章数
     */
    long countByCategoryId(Long categoryId);

    /**
     * 分页查询待审核文章（管理员视角）
     */
    PageResult<ArticleVO> pagePending(ArticleQueryDTO dto);

    /**
     * 审核通过（管理员）
     */
    void approve(Long id);

    /**
     * 审核驳回（管理员）
     *
     * @param id     文章 ID
     * @param reason 驳回原因
     */
    void reject(Long id, String reason);

    /**
     * 热门文章排行榜（基于 Redis ZSet 浏览量）
     *
     * @param limit 返回条数
     */
    List<ArticleVO> getHotArticles(int limit);

    /**
     * 管理员查询所有非草稿文章（status != 0），分页
     */
    PageResult<ArticleVO> pageAll(ArticleQueryDTO dto);

    /**
     * 点赞/取消点赞
     *
     * @param id     文章 ID
     * @param userId 用户 ID
     * @return true=已点赞，false=已取消
     */
    boolean toggleLike(Long id, Long userId);
}
