package org.xiyu.yee.capespringboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.xiyu.yee.capespringboot.model.Announcement;
import org.xiyu.yee.capespringboot.service.AnnouncementService;
import java.util.List;

@ControllerAdvice
public class GlobalAnnouncementController {
    
    @Autowired
    private AnnouncementService announcementService;
    
    @ModelAttribute("announcements")
    public List<String> getGlobalAnnouncements() {
        return announcementService.getActiveAnnouncements()
            .stream()
            .map(Announcement::getContent)
            .toList();
    }
} 