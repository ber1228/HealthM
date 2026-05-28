package com.nutrition.controller;

import com.nutrition.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
@CrossOrigin
public class UploadController {

    @Autowired
    private AuthUtil authUtil;

    @Value("${upload.base-dir}")
    private String uploadBaseDir;

    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (!authUtil.isAdmin(request)) {
            return ResponseEntity.status(403).body("权限不足，需要管理员权限");
        }
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("文件为空");
        }
        try {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
                return ResponseEntity.badRequest().body("仅支持图片类型");
            }
            String original = file.getOriginalFilename();
            String ext = "";
            if (original != null) {
                int idx = original.lastIndexOf('.');
                if (idx >= 0) ext = original.substring(idx);
            }
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;

            // 保存到配置的外部目录（如：~/nutrition-uploads）
            Path baseDir = Paths.get(uploadBaseDir);
            Files.createDirectories(baseDir);
            Path target = baseDir.resolve(filename);
            file.transferTo(target.toFile());

            Map<String, Object> resp = new HashMap<>();
            // 返回带有 context-path 的访问URL，前端可直接使用
            String ctx = request.getContextPath(); // e.g. /api
            resp.put("url", (ctx != null ? ctx : "") + "/uploads/" + filename);
            return ResponseEntity.ok(resp);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("上传失败: " + e.getMessage());
        }
    }
}
