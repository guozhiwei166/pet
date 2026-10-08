package com.tourism.controller;

import com.tourism.common.BizException;
import com.tourism.common.Result;
import com.tourism.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * 图片上传(本地目录,映射为 /uploads/**)
 * 允许类型:jpg/jpeg/png/gif/webp
 */
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private static final java.util.Set<String> ALLOWED = java.util.Set.of(
            "jpg", "jpeg", "png", "gif", "webp");

    @Value("${tourism.upload.dir}")
    private String uploadDir;

    @PostMapping("/image")
    public Result<String> image(@RequestParam("file") MultipartFile file) {
        // 登录用户可上传(拦截器统一鉴权),便于商家/用户维护图片;游客不可匿名写入
        LoginUser.require();
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择要上传的图片");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BizException("图片不能超过 5MB");
        }
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = original.contains(".")
                ? original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT)
                : "";
        if (!ALLOWED.contains(ext)) {
            throw new BizException("仅支持 jpg/png/gif/webp 图片");
        }
        try {
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
            String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path dir = Paths.get(uploadDir).toAbsolutePath().resolve(dateDir);
            Files.createDirectories(dir);
            Path target = dir.resolve(filename);
            file.transferTo(target.toFile());
            return Result.success("/uploads/" + dateDir + "/" + filename);
        } catch (Exception e) {
            throw new BizException("图片上传失败,请重试");
        }
    }
}
