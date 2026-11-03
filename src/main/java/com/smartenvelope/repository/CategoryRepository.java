package com.smartenvelope.repository;

import com.smartenvelope.entity.Category;
import com.smartenvelope.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository thao tác dữ liệu với bảng categories.
 * Phụ trách bởi: Thành viên 2 (Account & Category Management).
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByIsSystemTrue();

    List<Category> findByIsSystemTrueOrderByTypeAscNameAsc();

    List<Category> findByIsSystemTrueAndIsActiveTrueOrderByTypeAscNameAsc();

    boolean existsByNameIgnoreCaseAndTypeAndIsSystemTrue(String name, CategoryType type);

    boolean existsByNameIgnoreCaseAndTypeAndIsSystemTrueAndIdNot(String name, CategoryType type, Long id);

    @Query("SELECT c FROM Category c WHERE c.isSystem = true AND " +
           "(:type IS NULL OR c.type = :type) AND " +
           "(:isActive IS NULL OR c.isActive = :isActive) AND " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY c.type ASC, c.name ASC")
    List<Category> findSystemCategoriesFilter(@Param("type") CategoryType type,
                                             @Param("isActive") Boolean isActive,
                                             @Param("keyword") String keyword);

    List<Category> findByUserId(Long userId);

    List<Category> findByType(CategoryType type);

    @Query("SELECT c FROM Category c WHERE c.isSystem = true OR c.user.id = :userId")
    List<Category> findAllAvailableForUser(@Param("userId") Long userId);

    @Query("SELECT c FROM Category c WHERE (c.isSystem = true OR c.user.id = :userId) AND c.type = :type")
    List<Category> findAllAvailableForUserAndType(@Param("userId") Long userId, @Param("type") CategoryType type);
}
