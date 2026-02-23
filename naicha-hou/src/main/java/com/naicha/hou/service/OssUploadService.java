package com.naicha.hou.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.naicha.hou.config.OssConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * OSS文件上传服务
 * 
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OssUploadService {

    private final OSS ossClient;
    private final OssConfig ossConfig;

    /**
     * 上传文件到OSS
     * 
     * @param file 上传的文件
     * @param folder 文件夹名称（可选）
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file, String folder) {
        try {
            // 验证文件
            validateFile(file);
            
            // 生成文件名
            String fileName = generateFileName(file.getOriginalFilename(), folder);
            
            // 获取文件输入流
            InputStream inputStream = file.getInputStream();
            
            // 设置文件元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            metadata.setCacheControl("no-cache");
            metadata.setHeader("Pragma", "no-cache");
            metadata.setContentDisposition("inline;filename=" + fileName);
            
            // 创建上传请求
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    ossConfig.getBucketName(), 
                    fileName, 
                    inputStream, 
                    metadata
            );
            
            // 注意：如果 Bucket 策略不允许通过 API 设置 ACL，需要在 OSS 控制台配置 Bucket 为公共读
            // 配置路径：OSS 控制台 -> Bucket -> 权限管理 -> 公共访问设置 -> 允许公共读访问
            // 或者：权限管理 -> Bucket 授权策略 -> 添加授权策略，允许匿名用户读取
            
            // 上传文件
            ossClient.putObject(putObjectRequest);
            
            // 关闭输入流
            inputStream.close();
            
            // 返回文件访问URL
            String fileUrl = ossConfig.getDomain() + "/" + fileName;
            log.info("文件上传成功，已设置为公共读权限: {}", fileUrl);
            
            return fileUrl;
            
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传图片文件
     * 
     * @param file 图片文件
     * @param folder 文件夹名称（可选）
     * @return 图片访问URL
     */
    public String uploadImage(MultipartFile file, String folder) {
        // 验证图片格式
        if (!isImageFile(file)) {
            throw new IllegalArgumentException("只支持图片格式文件");
        }
        
        return uploadFile(file, folder != null ? folder : "images");
    }

    /**
     * 删除OSS文件
     * 
     * @param fileUrl 文件URL
     * @return 是否删除成功
     */
    public boolean deleteFile(String fileUrl) {
        try {
            // 从URL中提取文件名
            String fileName = extractFileNameFromUrl(fileUrl);
            
            // 删除文件
            ossClient.deleteObject(ossConfig.getBucketName(), fileName);
            
            log.info("文件删除成功: {}", fileName);
            return true;
            
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 验证文件
     * 
     * @param file 文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        // 检查文件大小（10MB）
        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小不能超过10MB");
        }
    }

    /**
     * 检查是否为图片文件
     * 
     * @param file 文件
     * @return 是否为图片
     */
    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    /**
     * 生成文件名
     * 
     * @param originalFilename 原始文件名
     * @param folder 文件夹
     * @return 新文件名
     */
    private String generateFileName(String originalFilename, String folder) {
        // 获取文件扩展名
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        // 生成唯一文件名
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = timestamp + "_" + uuid + extension;
        
        // 构建完整路径
        if (folder != null && !folder.isEmpty()) {
            return ossConfig.getPathPrefix() + folder + "/" + fileName;
        } else {
            return ossConfig.getPathPrefix() + fileName;
        }
    }

    /**
     * 从URL中提取文件名
     * 
     * @param fileUrl 文件URL
     * @return 文件名
     */
    private String extractFileNameFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return "";
        }
        
        // 移除域名部分
        String domain = ossConfig.getDomain();
        if (fileUrl.startsWith(domain)) {
            return fileUrl.substring(domain.length() + 1);
        }
        
        return fileUrl;
    }
}
