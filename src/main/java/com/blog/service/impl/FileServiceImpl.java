package com.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.Constants;
import com.blog.entity.FileRecord;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.mapper.FileRecordMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl extends ServiceImpl<FileRecordMapper, FileRecord> implements FileService {

    private final UserMapper userMapper;

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Value("${file.upload.allowed-extensions:jpg,jpeg,png,gif}")
    private String allowedExtensions;

    /** 最大文件大小：10MB */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /** 上传目录的绝对路径（启动时解析一次） */
    private Path resolvedUploadPath;

    @PostConstruct
    public void init() {
        this.resolvedUploadPath = new File(uploadPath).getAbsoluteFile().toPath().normalize();
        try {
            if (!Files.exists(resolvedUploadPath)) {
                Files.createDirectories(resolvedUploadPath);
            }
            log.info("文件上传目录: {}", resolvedUploadPath);
        } catch (IOException e) {
            log.error("创建上传目录失败", e);
        }
    }

    @Override
    public FileRecord upload(MultipartFile file, Long userId) {
        // 1. 文件非空校验
        if (file.isEmpty()) {
            throw BusinessException.badRequest("文件不能为空");
        }

        // 2. 大小校验
        if (file.getSize() > MAX_FILE_SIZE) {
            throw BusinessException.badRequest("文件大小不能超过 10MB");
        }

        // 3. 类型校验
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            throw BusinessException.badRequest("文件名无效");
        }
        String extension = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
        List<String> allowed = Arrays.asList(allowedExtensions.split(","));
        if (!allowed.contains(extension)) {
            throw BusinessException.badRequest("不支持的文件类型，仅允许：" + allowedExtensions);
        }

        // 4. UUID 重命名
        String newFileName = UUID.randomUUID().toString() + "." + extension;

        // 5. 保存到磁盘（使用启动时解析好的绝对路径）
        try {
            Path targetPath = resolvedUploadPath.resolve(newFileName);
            file.transferTo(targetPath.toFile());
            log.info("文件保存成功: {}", targetPath.toAbsolutePath());
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new BusinessException("文件保存失败，请稍后重试");
        }

        // 7. 保存记录到数据库
        String fileUrl = "/uploads/" + newFileName;
        FileRecord record = FileRecord.builder()
                .originalName(originalName)
                .fileName(newFileName)
                .fileUrl(fileUrl)
                .fileSize(file.getSize())
                .fileType(file.getContentType())
                .userId(userId)
                .build();
        save(record);

        return record;
    }

    @Override
    public List<FileRecord> listAll() {
        return list();
    }

    @Override
    public void delete(Long id, Long userId) {
        FileRecord record = getById(id);
        if (record == null) {
            throw BusinessException.notFound("文件不存在");
        }

        // 权限：上传者或管理员
        User user = userMapper.selectById(userId);
        boolean isAdmin = user != null && user.getRole() == Constants.ROLE_ADMIN;
        if (!record.getUserId().equals(userId) && !isAdmin) {
            throw BusinessException.forbidden("只能删除自己上传的文件");
        }

        // 删除磁盘文件
        try {
            Path filePath = resolvedUploadPath.resolve(record.getFileName());
            Files.deleteIfExists(filePath);
            log.info("磁盘文件已删除: {}", filePath.toAbsolutePath());
        } catch (IOException e) {
            log.warn("磁盘文件删除失败: {}", e.getMessage());
        }

        // 删除数据库记录
        removeById(id);
    }
}
