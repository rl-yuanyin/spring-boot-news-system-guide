package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.dto.CategoryDTO;
import com.blog.entity.Category;
import com.blog.exception.BusinessException;
import com.blog.mapper.CategoryMapper;
import com.blog.service.ArticleService;
import com.blog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类服务实现
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final ArticleService articleService;

    @Override
    public List<Category> listAll() {
        return list(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 0)
                .orderByAsc(Category::getSort));
    }

    @Override
    public Category add(CategoryDTO dto) {
        // 校验名称唯一
        checkNameUnique(dto.getName(), null);

        Category category = Category.builder()
                .name(dto.getName())
                .sort(dto.getSort() != null ? dto.getSort() : 0)
                .status(0)
                .build();
        save(category);
        return category;
    }

    @Override
    public Category update(Long id, CategoryDTO dto) {
        Category category = getById(id);
        if (category == null) {
            throw BusinessException.notFound("分类不存在");
        }

        // 校验名称唯一（排除自身）
        checkNameUnique(dto.getName(), id);

        category.setName(dto.getName());
        if (dto.getSort() != null) {
            category.setSort(dto.getSort());
        }
        updateById(category);
        return category;
    }

    @Override
    public void delete(Long id) {
        Category category = getById(id);
        if (category == null) {
            throw BusinessException.notFound("分类不存在");
        }

        // 判断是否有关联文章
        if (articleService.countByCategoryId(id) > 0) {
            throw BusinessException.badRequest("该分类下存在文章，无法删除");
        }

        removeById(id);
    }

    /**
     * 校验分类名称唯一
     */
    private void checkNameUnique(String name, Long excludeId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<Category>()
                .eq(Category::getName, name);
        if (excludeId != null) {
            wrapper.ne(Category::getId, excludeId);
        }
        if (count(wrapper) > 0) {
            throw BusinessException.badRequest("分类名称已存在");
        }
    }
}
