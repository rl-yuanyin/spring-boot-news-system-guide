package com.blog.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页返回结果
 *
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /** 总记录数 */
    private long total;

    /** 当前页数据 */
    private List<T> list;

    /** 当前页码 */
    private int page;

    /** 每页大小 */
    private int pageSize;

    /**
     * 从 MyBatis-Plus 分页结果构建
     */
    public static <T> PageResult<T> of(com.baomidou.mybatisplus.extension.plugins.pagination.Page<?> page, List<T> list) {
        return new PageResult<>(
                page.getTotal(),
                list,
                (int) page.getCurrent(),
                (int) page.getSize()
        );
    }
}
