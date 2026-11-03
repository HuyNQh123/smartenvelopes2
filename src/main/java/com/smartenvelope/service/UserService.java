package com.smartenvelope.service;

import com.smartenvelope.dto.admin.AdminUserResponse;
import com.smartenvelope.dto.auth.RegisterRequest;
import com.smartenvelope.entity.Membership;
import com.smartenvelope.entity.User;
import com.smartenvelope.entity.UserFinanceSettings;
import com.smartenvelope.entity.Workspace;
import com.smartenvelope.enums.MemberRole;
import com.smartenvelope.enums.MembershipStatus;
import com.smartenvelope.enums.Role;
import com.smartenvelope.enums.UserStatus;
import com.smartenvelope.enums.WorkspaceType;
import com.smartenvelope.exception.ResourceNotFoundException;
import com.smartenvelope.repository.MembershipRepository;
import com.smartenvelope.repository.UserFinanceSettingsRepository;
import com.smartenvelope.repository.UserRepository;
import com.smartenvelope.repository.WorkspaceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service xử lý các nghiệp vụ quản lý tài khoản người dùng và đăng ký tài khoản mới.
 * Tự động khởi tạo Workspace cá nhân, quyền OWNER và cấu hình tài chính mặc định.
 * Phụ trách bởi: Thành viên 1.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final MembershipRepository membershipRepository;
    private final UserFinanceSettingsRepository userFinanceSettingsRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       WorkspaceRepository workspaceRepository,
                       MembershipRepository membershipRepository,
                       UserFinanceSettingsRepository userFinanceSettingsRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.workspaceRepository = workspaceRepository;
        this.membershipRepository = membershipRepository;
        this.userFinanceSettingsRepository = userFinanceSettingsRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Đăng ký tài khoản người dùng mới.
     * Thực hiện trong một Transaction nguyên tử để đảm bảo tính toàn vẹn dữ liệu:
     * 1. Lưu User với mật khẩu BCrypt.
     * 2. Tạo Workspace cá nhân (PERSONAL).
     * 3. Tạo quyền OWNER cho người dùng trong Workspace cá nhân.
     * 4. Tạo cấu hình tài chính mặc định (UserFinanceSettings).
     */
    @Transactional
    public User register(RegisterRequest request) {
        // Kiểm tra khớp mật khẩu
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }

        // Kiểm tra trùng email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Địa chỉ email đã tồn tại trong hệ thống");
        }

        // Xác định username (nếu người dùng không nhập thì dùng email)
        String username = request.getUsername();
        if (username == null || username.trim().isEmpty()) {
            username = request.getEmail();
        } else {
            if (userRepository.existsByUsername(username)) {
                throw new IllegalArgumentException("Tên đăng nhập đã tồn tại trong hệ thống");
            }
        }

        // 1. Tạo và lưu User
        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setUsername(username.trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER); // Mặc định luôn là USER thông thường
        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);

        // 2. Tự động tạo Workspace cá nhân cho người dùng
        Workspace personalWorkspace = new Workspace(
                "Quỹ cá nhân của " + savedUser.getFullName(),
                WorkspaceType.PERSONAL,
                "Không gian quản lý ngân sách cá nhân",
                savedUser
        );
        Workspace savedWorkspace = workspaceRepository.save(personalWorkspace);

        // 3. Tạo Membership với vai trò OWNER
        Membership membership = new Membership(
                savedWorkspace,
                savedUser,
                MemberRole.OWNER,
                MembershipStatus.ACCEPTED
        );
        membershipRepository.save(membership);

        // 4. Tạo cấu hình tài chính mặc định (ngày lương mùng 5, quy tắc 50/30/20, cảnh báo 80%)
        UserFinanceSettings defaultSettings = new UserFinanceSettings(
                savedUser,
                "50/30/20",
                5,
                80
        );
        userFinanceSettingsRepository.save(defaultSettings);

        return savedUser;
    }

    public boolean existsByEmail(String email) {
        if (email == null) return false;
        return userRepository.existsByEmail(email.trim().toLowerCase());
    }

    public boolean existsByUsername(String username) {
        if (username == null) return false;
        return userRepository.existsByUsername(username.trim());
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Tìm kiếm và lọc danh sách người dùng cho Quản trị viên (Admin) có phân trang.
     */
    @Transactional(readOnly = true)
    public Page<User> searchUsers(String keyword, Role role, UserStatus status, Pageable pageable) {
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        return userRepository.findBySearchFilter(cleanKeyword, role, status, pageable);
    }

    /**
     * Lấy thông tin chi tiết người dùng an toàn phục vụ xem chi tiết trên Admin.
     */
    @Transactional(readOnly = true)
    public AdminUserResponse getUserDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));
        List<Membership> memberships = membershipRepository.findByUserIdAndStatus(userId, MembershipStatus.ACCEPTED);
        return new AdminUserResponse(user, memberships.size());
    }

    /**
     * Khóa tài khoản người dùng bởi Quản trị viên (Admin).
     * Quy tắc an toàn bắt buộc:
     * 1. Không cho phép Admin tự khóa tài khoản của chính mình.
     * 2. Không cho phép khóa tài khoản Admin cuối cùng còn hoạt động trong hệ thống.
     * 3. Tuyệt đối không xóa dữ liệu người dùng, Workspace, Membership hay Transaction.
     */
    @Transactional
    public void lockUser(Long targetUserId, Long currentAdminId) {
        if (targetUserId == null) {
            throw new IllegalArgumentException("ID người dùng không được để trống");
        }

        if (targetUserId.equals(currentAdminId)) {
            throw new IllegalArgumentException("Bạn không thể tự khóa tài khoản của chính mình!");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + targetUserId));

        if (targetUser.getRole() == Role.ADMIN && targetUser.getStatus() == UserStatus.ACTIVE) {
            long activeAdminCount = userRepository.countByRoleAndStatus(Role.ADMIN, UserStatus.ACTIVE);
            if (activeAdminCount <= 1) {
                throw new IllegalStateException("Không thể khóa tài khoản Quản trị viên (ADMIN) cuối cùng đang hoạt động!");
            }
        }

        targetUser.setStatus(UserStatus.LOCKED);
        userRepository.save(targetUser);
    }

    /**
     * Mở khóa tài khoản người dùng bởi Quản trị viên (Admin).
     */
    @Transactional
    public void unlockUser(Long targetUserId, Long currentAdminId) {
        if (targetUserId == null) {
            throw new IllegalArgumentException("ID người dùng không được để trống");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + targetUserId));

        targetUser.setStatus(UserStatus.ACTIVE);
        userRepository.save(targetUser);
    }
}
