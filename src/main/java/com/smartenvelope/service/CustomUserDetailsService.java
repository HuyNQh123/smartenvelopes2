package com.smartenvelope.service;

import com.smartenvelope.entity.User;
import com.smartenvelope.enums.UserStatus;
import com.smartenvelope.repository.UserRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * Service xác thực tài khoản người dùng từ cơ sở dữ liệu cho Spring Security.
 * Hỗ trợ đăng nhập linh hoạt bằng email hoặc tên đăng nhập (username).
 * Phụ trách bởi: Thành viên 1.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // Tìm người dùng theo email trước, nếu không có thì tìm theo username
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Không tìm thấy tài khoản với thông tin đăng nhập: " + identifier));

        // Kiểm tra trạng thái tài khoản
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new LockedException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Quản trị viên.");
        }
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new DisabledException("Tài khoản chưa được kích hoạt.");
        }

        // Quyền hạn hệ thống: ROLE_USER hoặc ROLE_ADMIN
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        return new CustomUserDetails(user, List.of(authority));
    }

    /**
     * Lớp mở rộng UserDetails lưu trữ thêm thông tin id và họ tên người dùng để hiển thị trên giao diện.
     */
    public static class CustomUserDetails implements UserDetails {
        private final Long id;
        private final String username;
        private final String password;
        private final String fullName;
        private final String email;
        private final Collection<? extends GrantedAuthority> authorities;
        private final boolean enabled;
        private final boolean accountNonLocked;

        public CustomUserDetails(User user, Collection<? extends GrantedAuthority> authorities) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.password = user.getPassword();
            this.fullName = user.getFullName();
            this.email = user.getEmail();
            this.authorities = authorities;
            this.enabled = user.getStatus() == UserStatus.ACTIVE;
            this.accountNonLocked = user.getStatus() != UserStatus.LOCKED;
        }

        public Long getId() {
            return id;
        }

        public String getFullName() {
            return fullName;
        }

        public String getEmail() {
            return email;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public String getUsername() {
            // Trả về username hoặc email làm định danh
            return username;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return accountNonLocked;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }
    }
}

