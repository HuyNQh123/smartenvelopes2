package com.smartenvelope.repository;

import com.smartenvelope.entity.UserFinanceSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository thao tác dữ liệu với bảng user_finance_settings. Phụ trách bởi:
 * Thành viên 4 & Thành viên 1.
 */

public interface UserFinanceSettingsRepository extends JpaRepository<UserFinanceSettings, Long> {

    Optional<UserFinanceSettings> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
