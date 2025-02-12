package org.xiyu.yee.capespringboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.xiyu.yee.capespringboot.model.Announcement;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    // 查找所有启用的公告，按创建时间降序排序
    List<Announcement> findByEnabledTrueOrderByCreatedAtDesc();
    
    // 查找所有启用的公告
    List<Announcement> findByEnabledTrue();
} 