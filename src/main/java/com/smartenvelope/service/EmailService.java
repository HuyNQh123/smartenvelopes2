package com.smartenvelope.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service xử lý gửi email thông báo và lời mời tham gia hệ thống.
 * Hỗ trợ chế độ DEV (ghi log link mời an toàn phục vụ kiểm thử) khi chưa cấu hình máy chủ SMTP thực tế.
 * Phụ trách bởi: Thành viên 1.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    /**
     * Gửi email mời tham gia Quỹ tài chính.
     *
     * @param toEmail Địa chỉ email người nhận.
     * @param workspaceName Tên Quỹ mời tham gia.
     * @param inviterName Họ tên người gửi lời mời.
     * @param inviteUrl Đường dẫn xác nhận tham gia kèm token.
     */
    public void sendWorkspaceInvitationEmail(String toEmail, String workspaceName, String inviterName, String inviteUrl) {
        // Chế độ DEV thông báo chi tiết vào log hệ thống
        log.info("================================================================================");
        log.info("[DEV EMAIL NOTIFICATION] Gửi lời mời tham gia Quỹ tài chính");
        log.info("  Người nhận: {}", toEmail);
        log.info("  Người mời : {}", inviterName);
        log.info("  Quỹ chung : {}", workspaceName);
        log.info("  Link mời  : {}", inviteUrl);
        log.info("  (Lưu ý: Đang ở chế độ phát triển DEV, email thực tế chưa được gửi ra ngoài)");
        log.info("================================================================================");
    }
}

