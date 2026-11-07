package com.smartenvelope.repository;

import com.smartenvelope.entity.Category;
import com.smartenvelope.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository thao tác dữ liệu với bảng Category.
 * Phụ trách bởi: Thành viên 3 (Quang).
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Tìm danh sách các danh mục theo loại (EXPENSE / INCOME) và đang ở trạng thái kích hoạt (active = true).
     *
     * @param type Loại danh mục
     * @return Danh sách các danh mục thỏa mãn
     */
    List<Category> findByTypeAndActiveTrue(CategoryType type);

    /**
     * Tìm tất cả các danh mục đang ở trạng thái kích hoạt (active = true).
     *
     * @return Danh sách các danh mục kích hoạt
     */
    List<Category> findByActiveTrue();
}
