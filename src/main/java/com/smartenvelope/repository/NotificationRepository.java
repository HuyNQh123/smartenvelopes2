package com.smartenvelope.repository;

import com.smartenvelope.entity.Notification;
import com.smartenvelope.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository thao tác dữ liệu với bảng notifications.
 * Phụ trách bởi: Thành viên 4 & Thành viên 5.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndStatus(Long userId, NotificationStatus status);

    long countByUserIdAndStatus(Long userId, NotificationStatus status);
}

