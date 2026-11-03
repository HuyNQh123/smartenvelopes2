package com.smartenvelope.service;

import com.smartenvelope.entity.User;
import com.smartenvelope.enums.UserStatus;
import com.smartenvelope.exception.ResourceNotFoundException;
import com.smartenvelope.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service tiện ích cung cấp thông tin người dùng hiện đang đăng nhập cho toàn bộ hệ thống.
 * Phục vụ cho cả 5 thành viên (TV 1, 2, 3, 4, 5) để xác định danh tính người thực hiện thao tác.
 * Phụ trách bởi: Thành viên 1.
 */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Kiểm tra xem phiên làm việc hiện tại có người dùng đã đăng nhập hợp lệ hay không.
     *
     * @return true nếu đã đăng nhập và không phải Anonymous, ngược lại false.
     */
    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }

    /**
     * Lấy thông tin User thực thể đầy đủ từ cơ sở dữ liệu dựa trên phiên đăng nhập hiện tại.
     * Tuyệt đối không nhận userId từ request và không trả về User giả lập.
     *
     * @return đối tượng User tương ứng trong database.
     * @throws AccessDeniedException nếu người dùng chưa đăng nhập.
     * @throws ResourceNotFoundException nếu không tìm thấy User tương ứng trong database.
     */
    public User getCurrentUser() {
        if (!isAuthenticated()) {
            throw new AccessDeniedException("Người dùng chưa đăng nhập hoặc phiên làm việc đã kết thúc.");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        User user;
        // 1. Nếu Principal là CustomUserDetails đã lưu sẵn ID người dùng
        if (principal instanceof CustomUserDetailsService.CustomUserDetails customUserDetails) {
            user = userRepository.findById(customUserDetails.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy tài khoản người dùng với ID: " + customUserDetails.getId()));
        } else {
            // 2. Fallback tìm kiếm theo email hoặc username nếu principal là đối tượng khác
            String identifier = auth.getName();
            if (identifier == null || identifier.isBlank()) {
                throw new AccessDeniedException("Không xác định được danh tính người dùng trong phiên làm việc.");
            }

            user = userRepository.findByEmail(identifier)
                    .or(() -> userRepository.findByUsername(identifier))
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy tài khoản người dùng: " + identifier));
        }

        // Kiểm tra an toàn: Nếu tài khoản vừa bị Admin khóa khi đang có session hoạt động
        if (user.getStatus() == UserStatus.LOCKED) {
            SecurityContextHolder.clearContext();
            throw new LockedException("Tài khoản của bạn đã bị khóa bởi Quản trị viên. Vui lòng liên hệ hỗ trợ.");
        }

        return user;
    }

    /**
     * Lấy ID của người dùng đang đăng nhập.
     * Tối ưu hiệu năng: Lấy trực tiếp từ CustomUserDetails nếu có để tránh truy vấn DB lặp lại.
     *
     * @return Long ID của người dùng.
     * @throws AccessDeniedException nếu chưa đăng nhập.
     */
    public Long getCurrentUserId() {
        if (!isAuthenticated()) {
            throw new AccessDeniedException("Người dùng chưa đăng nhập hoặc phiên làm việc đã kết thúc.");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        if (principal instanceof CustomUserDetailsService.CustomUserDetails customUserDetails) {
            return customUserDetails.getId();
        }

        return getCurrentUser().getId();
    }

    /**
     * Lấy địa chỉ email của người dùng hiện tại.
     * Tối ưu hiệu năng: Lấy trực tiếp từ CustomUserDetails hoặc Authentication name mà không truy vấn database lặp lại.
     *
     * @return String email người dùng.
     * @throws AccessDeniedException nếu chưa đăng nhập.
     */
    public String getCurrentUserEmail() {
        if (!isAuthenticated()) {
            throw new AccessDeniedException("Người dùng chưa đăng nhập hoặc phiên làm việc đã kết thúc.");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        if (principal instanceof CustomUserDetailsService.CustomUserDetails customUserDetails) {
            return customUserDetails.getEmail();
        }

        return getCurrentUser().getEmail();
    }

    /**
     * Phương thức tiện ích trả về Optional<User> an toàn, không ném exception nếu chưa đăng nhập.
     * Thích hợp cho ControllerAdvice hoặc các trang công khai.
     *
     * @return Optional<User> chứa User nếu đã đăng nhập, rỗng nếu chưa.
     */
    public Optional<User> findCurrentUser() {
        if (!isAuthenticated()) {
            return Optional.empty();
        }
        try {
            return Optional.of(getCurrentUser());
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
