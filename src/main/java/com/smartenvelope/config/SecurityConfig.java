package com.smartenvelope.config;

import com.smartenvelope.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

/**
 * Cấu hình bảo mật Spring Security cho hệ thống SmartEnvelope.
 * Quản lý xác thực người dùng, bảo vệ CSRF, phân quyền theo vai trò (USER, ADMIN)
 * và điều hướng luồng Đăng nhập - Đăng ký - Đăng xuất.
 * Phụ trách bởi: Thành viên 1.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            // 1. Phân quyền truy cập các đường dẫn
            .authorizeHttpRequests(auth -> auth
                // Tài nguyên tĩnh và trang công khai
                .requestMatchers(
                    "/css/**", 
                    "/js/**", 
                    "/images/**", 
                    "/webjars/**", 
                    "/favicon.ico"
                ).permitAll()
                // Đường dẫn đăng nhập, đăng ký và xử lý auth
                .requestMatchers(
                    "/", 
                    "/login", 
                    "/register", 
                    "/auth/**", 
                    "/error/**"
                ).permitAll()
                // Khu vực quản trị hệ thống dành riêng cho ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Tất cả các chức năng nghiệp vụ còn lại bắt buộc phải đăng nhập
                .anyRequest().authenticated()
            )
            // 2. Cấu hình Form Login
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            // 3. Cấu hình Logout (hỗ trợ cả /logout và /auth/logout bằng GET và POST)
            .logout(logout -> logout
                .logoutRequestMatcher(new OrRequestMatcher(
                    new AntPathRequestMatcher("/logout"),
                    new AntPathRequestMatcher("/auth/logout")
                ))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        // Bảo mật CSRF được giữ nguyên mặc định theo chuẩn Spring Security
        return http.build();
    }
}
