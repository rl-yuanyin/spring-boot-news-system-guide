package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.FileRecord;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件服务接口
 */
public interface FileService extends IService<FileRecord> {

    /**
     * 上传文件
     *
     * @param file   上传的文件
     * @param userId 上传者 ID
     * @return 文件记录
     */
    FileRecord upload(MultipartFile file, Long userId);

    /**
     * 查询所有文件记录
     */
    List<FileRecord> listAll();

    /**
     * 删除文件
     *
     * @param id     文件记录 ID
     * @param userId 操作者 ID
     */
    void delete(Long id, Long userId);
}
