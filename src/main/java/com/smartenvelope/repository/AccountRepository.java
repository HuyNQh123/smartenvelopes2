package com.smartenvelope.repository;

import com.smartenvelope.entity.Account;
import com.smartenvelope.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository thao tác dữ liệu với bảng accounts.
 * Phụ trách bởi: Thành viên 2 (Account & Category Management).
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByWorkspaceId(Long workspaceId);

    List<Account> findByWorkspaceIdAndType(Long workspaceId, AccountType type);
}
