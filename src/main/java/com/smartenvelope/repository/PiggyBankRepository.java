package com.smartenvelope.repository;

import com.smartenvelope.entity.PiggyBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository thao tác dữ liệu với bảng piggy_banks.
 * Phụ trách bởi: Thành viên 5 (Savings, Piggy Bank & Analytics).
 */
@Repository
public interface PiggyBankRepository extends JpaRepository<PiggyBank, Long> {

    Optional<PiggyBank> findByWorkspaceId(Long workspaceId);

    boolean existsByWorkspaceId(Long workspaceId);
}

