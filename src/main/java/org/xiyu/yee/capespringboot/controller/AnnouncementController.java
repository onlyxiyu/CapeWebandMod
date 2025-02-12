package org.xiyu.yee.capespringboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.xiyu.yee.capespringboot.model.Announcement;
import org.xiyu.yee.capespringboot.service.AnnouncementService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/announcements")
@PreAuthorize("hasRole('ADMIN')")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @GetMapping
    public String showAnnouncementManager(Model model) {
        model.addAttribute("announcements", announcementService.getAllAnnouncements());
        return "announcement-manager";
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<?> createAnnouncement(@RequestBody Map<String, String> request) {
        try {
            Announcement announcement = announcementService.createAnnouncement(request.get("content"));
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "公告创建成功",
                "announcement", announcement
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> updateAnnouncement(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            String content = (String) request.get("content");
            boolean enabled = (boolean) request.get("enabled");
            Announcement announcement = announcementService.updateAnnouncement(id, content, enabled);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "公告更新成功",
                "announcement", announcement
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteAnnouncement(@PathVariable Long id) {
        try {
            announcementService.deleteAnnouncement(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "公告删除成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
} 