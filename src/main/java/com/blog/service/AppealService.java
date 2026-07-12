package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.Appeal;

public interface AppealService extends IService<Appeal> {
    /** 提交申诉 */
    void submit(String username, String reason);
    /** 查看自己的申诉 */
    Appeal getByUsername(String username);
    /** 管理员：分页查询申诉列表 */
    Page<Appeal> listAppeals(int page, int pageSize);
    /** 管理员：通过申诉 */
    void approve(Long id);
    /** 管理员：驳回申诉 */
    void reject(Long id, String reply);
}
