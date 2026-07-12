package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.dto.CategoryDTO;
import com.blog.entity.Category;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService extends IService<Category> {

    /**
     * 查询所有启用的分类，按排序字段升序
     */
    List<Category> listAll();

    /**
     * 新增分类
     */
    Category add(CategoryDTO dto);

    /**
     * 修改分类
     */
    Category update(Long id, CategoryDTO dto);

    /**
     * 删除分类（若有关联文章则抛异常）
     */
    void delete(Long id);
}
