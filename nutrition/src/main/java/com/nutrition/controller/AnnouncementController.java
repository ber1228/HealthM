package com.nutrition.controller;

import com.nutrition.entity.Announcement;
import com.nutrition.mapper.AnnouncementMapper;
import com.nutrition.mapper.AnnouncementReadMapper;
import com.nutrition.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/announcements")
@CrossOrigin
public class AnnouncementController {
    
    @Autowired
    private AnnouncementMapper announcementMapper;
    
    @Autowired
    private AnnouncementReadMapper readMapper;

    @Autowired
    private AuthUtil authUtil;
    
    // 用户端：获取所有公告（带已读状态）
    @GetMapping
    public ResponseEntity<?> getAllAnnouncements(HttpServletRequest request) {
        try {
            List<Announcement> announcements = announcementMapper.findAll();
            Long userId = (Long) request.getAttribute("userId");
            Set<Long> readIds = new HashSet<>();
            if (userId != null) {
                List<Long> ids = readMapper.listReadIds(userId);
                if (ids != null) readIds.addAll(ids);
            }
            List<Map<String, Object>> resp = announcements.stream().map(a -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", a.getId());
                m.put("title", a.getTitle());
                m.put("summary", a.getSummary());
                m.put("isPinned", a.getIsPinned());
                m.put("publishTime", a.getPublishTime());
                m.put("read", readIds.contains(a.getId()));
                return m;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取公告列表失败: " + e.getMessage());
        }
    }
    
    // 用户端：获取公告详情（登录则自动标记已读）
    @GetMapping("/{id}")
    public ResponseEntity<?> getAnnouncementById(@PathVariable Long id, HttpServletRequest request) {
        try {
            Announcement announcement = announcementMapper.findById(id);
            if (announcement == null) {
                return ResponseEntity.notFound().build();
            }
            Long userId = (Long) request.getAttribute("userId");
            if (userId != null) {
                readMapper.markRead(id, userId);
            }
            return ResponseEntity.ok(announcement);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取公告失败: " + e.getMessage());
        }
    }

    // 明确的“标记已读”接口（可选）
    @PostMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).body("未登录");
        readMapper.markRead(id, userId);
        return ResponseEntity.ok("OK");
    }
    
    // 管理员端：获取所有公告（用于管理）
    @GetMapping("/admin/list")
    public ResponseEntity<?> getAdminAnnouncements(HttpServletRequest request) {
        if (!authUtil.isAdmin(request)) {
            return ResponseEntity.status(403).body("权限不足，需要管理员权限");
        }
        try {
            List<Announcement> announcements = announcementMapper.findAll();
            return ResponseEntity.ok(announcements);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取公告列表失败: " + e.getMessage());
        }
    }
    
    // 管理员端：创建公告
    @PostMapping
    public ResponseEntity<?> createAnnouncement(@RequestBody Announcement announcement, HttpServletRequest request) {
        if (!authUtil.isAdmin(request)) {
            return ResponseEntity.status(403).body("权限不足，需要管理员权限");
        }
        try {
            if (announcement.getPublishTime() == null) {
                announcement.setPublishTime(new Timestamp(System.currentTimeMillis()));
            }
            if (announcement.getIsPinned() == null) {
                announcement.setIsPinned(false);
            }
            announcementMapper.insert(announcement);
            return ResponseEntity.ok(announcement);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("创建公告失败: " + e.getMessage());
        }
    }
    
    // 管理员端：更新公告
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAnnouncement(@PathVariable Long id, @RequestBody Announcement announcement, HttpServletRequest request) {
        if (!authUtil.isAdmin(request)) {
            return ResponseEntity.status(403).body("权限不足，需要管理员权限");
        }
        try {
            announcement.setId(id);
            if (announcement.getPublishTime() == null) {
                announcement.setPublishTime(new Timestamp(System.currentTimeMillis()));
            }
            int rows = announcementMapper.update(announcement);
            if (rows > 0) {
                return ResponseEntity.ok(announcementMapper.findById(id));
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("更新公告失败: " + e.getMessage());
        }
    }
    
    // 管理员端：删除公告
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAnnouncement(@PathVariable Long id, HttpServletRequest request) {
        if (!authUtil.isAdmin(request)) {
            return ResponseEntity.status(403).body("权限不足，需要管理员权限");
        }
        try {
            int rows = announcementMapper.deleteById(id);
            if (rows > 0) {
                return ResponseEntity.ok("删除成功");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("删除公告失败: " + e.getMessage());
        }
    }
    
    // 管理员端：切换置顶状态
    @PutMapping("/{id}/pin")
    public ResponseEntity<?> togglePin(@PathVariable Long id, @RequestBody Boolean isPinned, HttpServletRequest request) {
        if (!authUtil.isAdmin(request)) {
            return ResponseEntity.status(403).body("权限不足，需要管理员权限");
        }
        try {
            int rows = announcementMapper.updatePinnedStatus(id, isPinned);
            if (rows > 0) {
                return ResponseEntity.ok("置顶状态更新成功");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("更新置顶状态失败: " + e.getMessage());
        }
    }
}

