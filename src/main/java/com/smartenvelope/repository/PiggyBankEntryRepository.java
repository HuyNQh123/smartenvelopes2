package com.smartenvelope.repository;

import com.smartenvelope.entity.PiggyBankEntry;
import com.smartenvelope.enums.PiggyBankEntryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository thao tác dữ liệu với bảng piggy_bank_entries.
 * Phụ trách bởi: Thành viên 5 (Savings, Piggy Bank & Analytics).
 */
@Repository
public interface PiggyBankEntryRepository extends JpaRepository<PiggyBankEntry, Long> {

    List<PiggyBankEntry> findByPiggyBankIdOrderByCreatedAtDesc(Long piggyBankId);

    List<PiggyBankEntry> findByPiggyBankIdAndType(Long piggyBankId, PiggyBankEntryType type);
}

