package org.xiyu.yee.capespringboot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xiyu.yee.capespringboot.model.Announcement;
import org.xiyu.yee.capespringboot.repository.AnnouncementRepository;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AnnouncementService {
    
    @Autowired
    private AnnouncementRepository announcementRepository;

    public List<Announcement> getAllAnnouncements() {
        return announcementRepository.findAll();
    }

    public List<Announcement> getActiveAnnouncements() {
        return announcementRepository.findByEnabledTrueOrderByCreatedAtDesc();
    }

    public List<Announcement> getEnabledAnnouncements() {
        return announcementRepository.findByEnabledTrue();
    }

    public Announcement createAnnouncement(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("公告内容不能为空");
        }

        Announcement announcement = new Announcement();
        announcement.setContent(content);
        announcement.setEnabled(true);
        announcement.setCreatedAt(LocalDateTime.now());
        announcement.setUpdatedAt(LocalDateTime.now());

        return announcementRepository.save(announcement);
    }

    public Announcement updateAnnouncement(Long id, String content, boolean enabled) {
        Announcement announcement = announcementRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("公告不存在"));

        if (content != null && !content.trim().isEmpty()) {
            announcement.setContent(content);
        }
        announcement.setEnabled(enabled);
        announcement.setUpdatedAt(LocalDateTime.now());

        return announcementRepository.save(announcement);
    }

    public void deleteAnnouncement(Long id) {
        announcementRepository.deleteById(id);
    }

    public String getAnnouncementsAsString() {
        List<Announcement> announcements = getActiveAnnouncements();
        return String.join("||", announcements.stream()
            .map(Announcement::getContent)
            .toList());
    }
} 