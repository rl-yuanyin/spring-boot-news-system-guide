package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.AdminApply;

public interface AdminApplyService extends IService<AdminApply> {
    /** 用户提交管理员申请 */
    void submit(Long userId, String reason);
    /** 查看自己的申请 */
    AdminApply getByUserId(Long userId);
    /** 管理员：分页查询申请列表 */
    Page<AdminApply> listApplies(int page, int pageSize);
    /** 管理员：通过申请 */
    void approve(Long id);
    /** 管理员：驳回申请 */
    void reject(Long id, String reply);
}
