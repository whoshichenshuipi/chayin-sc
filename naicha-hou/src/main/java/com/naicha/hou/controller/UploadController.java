package com.naicha.hou.controller;

import com.naicha.hou.common.Result;
import com.naicha.hou.service.OssUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 * 
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Tag(name = "文件上传", description = "文件上传相关接口")
public class UploadController {

    private final OssUploadService ossUploadService;

    /**
     * 上传图片
     * 
     * @param file 图片文件
     * @param folder 文件夹名称（可选）
     * @return 上传结果
     */
    @PostMapping("/image")
    @Operation(summary = "上传图片", description = "上传图片文件到OSS")
    public Result<String> uploadImage(
            @Parameter(description = "图片文件", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "文件夹名称")
            @RequestParam(value = "folder", required = false) String folder) {
        
        try {
            log.info("开始上传图片: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());
            
            String imageUrl = ossUploadService.uploadImage(file, folder);
            
            log.info("图片上传成功: {}", imageUrl);
            return Result.success("图片上传成功", imageUrl);
            
        } catch (IllegalArgumentException e) {
            log.warn("图片上传参数错误: {}", e.getMessage());
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("图片上传失败: {}", e.getMessage(), e);
            return Result.error(500, "图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件
     * 
     * @param file 文件
     * @param folder 文件夹名称（可选）
     * @return 上传结果
     */
    @PostMapping("/file")
    @Operation(summary = "上传文件", description = "上传任意文件到OSS")
    public Result<String> uploadFile(
            @Parameter(description = "文件", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "文件夹名称")
            @RequestParam(value = "folder", required = false) String folder) {
        
        try {
            log.info("开始上传文件: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());
            
            String fileUrl = ossUploadService.uploadFile(file, folder);
            
            log.info("文件上传成功: {}", fileUrl);
            return Result.success("文件上传成功", fileUrl);
            
        } catch (IllegalArgumentException e) {
            log.warn("文件上传参数错误: {}", e.getMessage());
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            return Result.error(500, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     * 
     * @param fileUrl 文件URL
     * @return 删除结果
     */
    @DeleteMapping("/file")
    @Operation(summary = "删除文件", description = "从OSS删除文件")
    public Result<Boolean> deleteFile(
            @Parameter(description = "文件URL", required = true)
            @RequestParam("fileUrl") String fileUrl) {
        
        try {
            log.info("开始删除文件: {}", fileUrl);
            
            boolean success = ossUploadService.deleteFile(fileUrl);
            
            if (success) {
                log.info("文件删除成功: {}", fileUrl);
                return Result.success("文件删除成功", true);
            } else {
                log.warn("文件删除失败: {}", fileUrl);
                return Result.error(500, "文件删除失败");
            }
            
        } catch (Exception e) {
            log.error("文件删除异常: {}", e.getMessage(), e);
            return Result.error(500, "文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传图片
     * 
     * @param files 图片文件数组
     * @param folder 文件夹名称（可选）
     * @return 上传结果
     */
    @PostMapping("/images/batch")
    @Operation(summary = "批量上传图片", description = "批量上传多张图片到OSS")
    public Result<String[]> uploadImages(
            @Parameter(description = "图片文件数组", required = true)
            @RequestParam("files") MultipartFile[] files,
            @Parameter(description = "文件夹名称")
            @RequestParam(value = "folder", required = false) String folder) {
        
        try {
            if (files == null || files.length == 0) {
                return Result.error(400, "请选择要上传的图片");
            }
            
            log.info("开始批量上传图片，数量: {}", files.length);
            
            String[] imageUrls = new String[files.length];
            
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                if (file != null && !file.isEmpty()) {
                    imageUrls[i] = ossUploadService.uploadImage(file, folder);
                }
            }
            
            log.info("批量上传图片成功，数量: {}", files.length);
            return Result.success("批量上传成功", imageUrls);
            
        } catch (Exception e) {
            log.error("批量上传图片失败: {}", e.getMessage(), e);
            return Result.error(500, "批量上传失败: " + e.getMessage());
        }
    }
}
