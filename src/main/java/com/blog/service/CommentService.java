package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.dto.CommentDTO;
import com.blog.entity.Comment;
import com.blog.vo.CommentVO;

import java.util.List;

/**
 * 评论服务接口
 */
public interface CommentService extends IService<Comment> {

    /**
     * 查询文章评论列表（仅正常状态）
     */
    List<CommentVO> listByArticleId(Long articleId);

    /**
     * 发布评论
     *
     * @param dto    评论参数
     * @param userId 评论用户 ID
     */
    CommentVO create(CommentDTO dto, Long userId);

    /**
     * 删除评论（用户删除自己的，管理员删除任意）
     *
     * @param id     评论 ID
     * @param userId 操作者 ID
     */
    void delete(Long id, Long userId);
}
