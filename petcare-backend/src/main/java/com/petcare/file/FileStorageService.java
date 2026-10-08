package com.petcare.file;

import com.petcare.common.BusinessException;
import com.petcare.file.entity.SysFile;
import com.petcare.file.mapper.SysFileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 本地文件存储服务：./upload/yyyy/MM/uuid.ext，预留 OSS 扩展位
 */
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");

    @Value("${petcare.upload.dir}")
    private String uploadDir;

    @Value("${petcare.upload.max-size}")
    private String maxSize;

    private final SysFileMapper sysFileMapper;

    public Map<String, Object> save(MultipartFile file, String bizType, Long userId) {
        if (file == null || file.isEmpty()) throw new BusinessException("请选择要上传的文件");
        if (file.getSize() > parseSize(maxSize)) throw new BusinessException("文件大小超出限制（" + maxSize + "）");

        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String ext = original.contains(".")
                ? original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT) : "";
        if (!ALLOWED_EXT.contains(ext)) throw new BusinessException("仅支持图片格式：jpg/png/gif/webp");

        // 校验文件真实内容（magic number），防止伪造扩展名上传非图片文件（jpg/jpeg 归一比较）
        String realType = detectImageType(file);
        String extNorm = "jpg".equals(ext) ? "jpeg" : ext;
        if (realType == null || !extNorm.equals(realType)) {
            throw new BusinessException("文件内容与扩展名不符，请上传真实图片（jpg/png/gif/webp）");
        }

        String relative = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"))
                + "/" + UUID.randomUUID().toString().replace("-", "") + "." + ext;
        try {
            Path target = Paths.get(uploadDir).toAbsolutePath().resolve(relative);
            Files.createDirectories(target.getParent());
            file.transferTo(target.toFile());
        } catch (Exception e) {
            throw new RuntimeException("文件保存失败", e);
        }

        SysFile entity = new SysFile();
        entity.setStorageName(relative.substring(relative.lastIndexOf('/') + 1));
        entity.setOriginalName(original);
        entity.setPath(relative);
        entity.setBizType(bizType);
        entity.setSize(file.getSize());
        entity.setContentType(file.getContentType());
        entity.setUploadBy(userId);
        sysFileMapper.insert(entity);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("url", "/upload/" + relative);
        res.put("fileId", entity.getId());
        return res;
    }

    /** 读取文件头识别真实图片类型：jpeg/png/gif/webp；无法识别返回 null（调用方按不匹配拒绝） */
    private String detectImageType(MultipartFile file) {
        try (var in = file.getInputStream()) {
            byte[] head = in.readNBytes(16);
            if (head.length >= 3 && (head[0] & 0xFF) == 0xFF && (head[1] & 0xFF) == 0xD8 && (head[2] & 0xFF) == 0xFF) {
                return "jpeg";
            }
            if (head.length >= 8 && (head[0] & 0xFF) == 0x89 && head[1] == 'P' && head[2] == 'N' && head[3] == 'G') {
                return "png";
            }
            if (head.length >= 4 && head[0] == 'G' && head[1] == 'I' && head[2] == 'F' && head[3] == '8') {
                return "gif";
            }
            // WEBP: RIFF....WEBP
            if (head.length >= 12 && head[0] == 'R' && head[1] == 'I' && head[2] == 'F' && head[3] == 'F'
                    && head[8] == 'W' && head[9] == 'E' && head[10] == 'B' && head[11] == 'P') {
                return "webp";
            }
            return null; // 无法识别 → 调用方拒绝
        } catch (Exception e) {
            throw new BusinessException("读取文件失败");
        }
    }

    private long parseSize(String size) {
        String s = size.toUpperCase(Locale.ROOT).trim();
        long unit = s.endsWith("MB") ? 1024L * 1024 : s.endsWith("KB") ? 1024L : 1;
        return Long.parseLong(s.replaceAll("[^0-9]", "")) * unit;
    }
}
