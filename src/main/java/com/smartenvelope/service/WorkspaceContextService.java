package com.smartenvelope.service;

import com.smartenvelope.entity.Membership;
import com.smartenvelope.entity.User;
import com.smartenvelope.entity.Workspace;
import com.smartenvelope.enums.MemberRole;
import com.smartenvelope.enums.MembershipStatus;
import com.smartenvelope.enums.WorkspaceType;
import com.smartenvelope.exception.ResourceNotFoundException;
import com.smartenvelope.repository.MembershipRepository;
import com.smartenvelope.repository.WorkspaceRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service quản lý Không gian làm việc / Quỹ tài chính hiện tại (Workspace Context) trong Session.
 * Cung cấp thông tin Workspace hiện tại và danh sách Workspace được phép truy cập cho cả 5 thành viên.
 * Phụ trách bởi: Thành viên 1.
 */
@Service
@Transactional(readOnly = true)
public class WorkspaceContextService {

    /**
     * Tên thuộc tính chính xác trong HTTP Session theo quy chuẩn toàn hệ thống.
     */
    public static final String CURRENT_WORKSPACE_ID_SESSION_KEY = "currentWorkspaceId";

    private final CurrentUserService currentUserService;
    private final WorkspaceRepository workspaceRepository;
    private final MembershipRepository membershipRepository;
    private final ObjectProvider<HttpSession> sessionProvider;
    private HttpSession testSession; // Dùng khi chạy Unit Test độc lập

    @Autowired
    public WorkspaceContextService(CurrentUserService currentUserService,
                                   WorkspaceRepository workspaceRepository,
                                   MembershipRepository membershipRepository,
                                   ObjectProvider<HttpSession> sessionProvider) {
        this.currentUserService = currentUserService;
        this.workspaceRepository = workspaceRepository;
        this.membershipRepository = membershipRepository;
        this.sessionProvider = sessionProvider;
    }

    /**
     * Constructor phụ trợ cho Unit Testing khi mock HttpSession trực tiếp.
     */
    public WorkspaceContextService(CurrentUserService currentUserService,
                                   WorkspaceRepository workspaceRepository,
                                   MembershipRepository membershipRepository,
                                   HttpSession testSession) {
        this.currentUserService = currentUserService;
        this.workspaceRepository = workspaceRepository;
        this.membershipRepository = membershipRepository;
        this.sessionProvider = null;
        this.testSession = testSession;
    }

    /**
     * Lấy ID của Workspace đang được lựa chọn trong Session của người dùng hiện tại.
     * Quy trình:
     * 1. Lấy currentWorkspaceId từ HttpSession.
     * 2. Nếu đã có trong Session, kiểm tra Workspace có tồn tại và người dùng hiện tại có Membership hợp lệ (ACCEPTED) không.
     * 3. Nếu không còn hợp lệ, không tiếp tục sử dụng ID đó mà loại bỏ khỏi Session.
     * 4. Nếu Session chưa có hoặc ID không hợp lệ, tự động chọn Workspace mặc định theo thứ tự ưu tiên:
     *    - Ưu tiên 1: Quỹ cá nhân (PERSONAL) của người dùng.
     *    - Ưu tiên 2: Bất kỳ Quỹ chung nào mà người dùng đang có Membership hợp lệ.
     * 5. Lưu ID Workspace đã chọn vào Session với thuộc tính "currentWorkspaceId".
     *
     * @return Long ID của Workspace hiện tại.
     * @throws ResourceNotFoundException nếu người dùng chưa có bất kỳ Workspace nào khả dụng.
     */
    public Long getCurrentWorkspaceId() {
        User currentUser = currentUserService.getCurrentUser();
        HttpSession session = resolveSession();

        if (session != null) {
            Object rawId = session.getAttribute(CURRENT_WORKSPACE_ID_SESSION_KEY);
            if (rawId != null) {
                Long candidateId = parseWorkspaceId(rawId);
                if (candidateId != null && hasValidMembership(currentUser.getId(), candidateId)) {
                    return candidateId;
                }
                // Nếu ID trong Session không còn hợp lệ, loại bỏ để chọn lại
                session.removeAttribute(CURRENT_WORKSPACE_ID_SESSION_KEY);
            }
        }

        // Tự động giải quyết Workspace mặc định
        Workspace defaultWorkspace = resolveDefaultWorkspace(currentUser);
        Long defaultId = defaultWorkspace.getId();

        if (session != null) {
            session.setAttribute(CURRENT_WORKSPACE_ID_SESSION_KEY, defaultId);
        }

        return defaultId;
    }

    /**
     * Lấy đối tượng Workspace thực thể hiện tại của người dùng.
     * Sử dụng getCurrentWorkspaceId() để xác định ID và đảm bảo người dùng có quyền hợp lệ.
     *
     * @return Workspace hiện tại.
     * @throws ResourceNotFoundException nếu không tìm thấy Workspace.
     */
    public Workspace getCurrentWorkspace() {
        Long workspaceId = getCurrentWorkspaceId();
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin Workspace với ID: " + workspaceId));
    }

    /**
     * Lấy danh sách tất cả các Workspace mà người dùng hiện tại có quyền truy cập (Membership ACCEPTED).
     * Bao gồm cả Quỹ cá nhân và các Quỹ chung/gia đình.
     *
     * @return Danh sách Workspace hợp lệ.
     */
    public List<Workspace> getAvailableWorkspaces() {
        User currentUser = currentUserService.getCurrentUser();
        return workspaceRepository.findWorkspacesJoinedByUser(currentUser.getId());
    }

    /**
     * Chuyển đổi Workspace đang hoạt động trong Session sang Workspace mới.
     * Yêu cầu kiểm tra bảo mật nghiêm ngặt: Người dùng phải có quyền truy cập hợp lệ (Membership ACCEPTED).
     *
     * @param newWorkspaceId ID của Workspace muốn chuyển sang.
     * @throws IllegalArgumentException nếu ID truyền vào rỗng.
     * @throws AccessDeniedException nếu người dùng không có quyền truy cập hoặc Workspace không tồn tại.
     */
    @Transactional
    public void setCurrentWorkspaceId(Long newWorkspaceId) {
        if (newWorkspaceId == null) {
            throw new IllegalArgumentException("Workspace ID không được để trống");
        }

        User currentUser = currentUserService.getCurrentUser();

        // Kiểm tra Workspace tồn tại và người dùng có Membership hợp lệ
        if (!hasValidMembership(currentUser.getId(), newWorkspaceId)) {
            throw new AccessDeniedException("Bạn không có quyền truy cập vào Workspace này hoặc Workspace không tồn tại.");
        }

        HttpSession session = resolveSession();
        if (session != null) {
            session.setAttribute(CURRENT_WORKSPACE_ID_SESSION_KEY, newWorkspaceId);
        }
    }

    /**
     * Tiện ích lấy vai trò của người dùng hiện tại trong một Workspace cụ thể.
     *
     * @param workspaceId ID của Workspace cần kiểm tra.
     * @return Optional chứa MemberRole (OWNER, MANAGER, WRITER, VIEWER) nếu hợp lệ.
     */
    public Optional<MemberRole> getCurrentUserRoleInWorkspace(Long workspaceId) {
        if (workspaceId == null || !currentUserService.isAuthenticated()) {
            return Optional.empty();
        }
        User currentUser = currentUserService.getCurrentUser();
        return membershipRepository.findByWorkspaceIdAndUserIdAndStatus(
                workspaceId, currentUser.getId(), MembershipStatus.ACCEPTED)
                .map(Membership::getRole);
    }

    /**
     * Tiện ích kiểm tra xem người dùng hiện tại có quyền truy cập vào một Workspace hay không.
     *
     * @param workspaceId ID Workspace cần kiểm tra.
     * @return true nếu có quyền, ngược lại false.
     */
    public boolean hasAccessToWorkspace(Long workspaceId) {
        if (workspaceId == null || !currentUserService.isAuthenticated()) {
            return false;
        }
        try {
            Long userId = currentUserService.getCurrentUserId();
            return hasValidMembership(userId, workspaceId);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Xóa thuộc tính currentWorkspaceId khỏi Session (ví dụ khi đăng xuất hoặc người dùng bị xóa khỏi quỹ).
     */
    public void clearWorkspaceSession() {
        HttpSession session = resolveSession();
        if (session != null) {
            session.removeAttribute(CURRENT_WORKSPACE_ID_SESSION_KEY);
        }
    }

    /**
     * Cơ chế tự động giải quyết Workspace mặc định cho người dùng:
     * 1. Ưu tiên 1: Quỹ cá nhân (PERSONAL) mà người dùng sở hữu và có membership ACCEPTED.
     * 2. Ưu tiên 2: Bất kỳ Quỹ nào mà người dùng đang có membership ACCEPTED.
     * 3. Không có: Ném ngoại lệ ResourceNotFoundException.
     */
    private Workspace resolveDefaultWorkspace(User currentUser) {
        // 1. Tìm Quỹ cá nhân của người dùng
        List<Workspace> personalWorkspaces = workspaceRepository.findByOwnerIdAndType(
                currentUser.getId(), WorkspaceType.PERSONAL);

        for (Workspace ws : personalWorkspaces) {
            if (hasValidMembership(currentUser.getId(), ws.getId())) {
                return ws;
            }
        }

        // 2. Tìm bất kỳ Quỹ nào có Membership ACCEPTED
        List<Workspace> joinedWorkspaces = workspaceRepository.findWorkspacesJoinedByUser(currentUser.getId());

        if (!joinedWorkspaces.isEmpty()) {
            return joinedWorkspaces.get(0);
        }

        throw new ResourceNotFoundException("Người dùng chưa có Quỹ tài chính (Workspace) nào khả dụng.");
    }

    /**
     * Kiểm tra xem người dùng có Membership hợp lệ (ACCEPTED) trong Workspace và Workspace còn tồn tại hay không.
     */
    private boolean hasValidMembership(Long userId, Long workspaceId) {
        if (userId == null || workspaceId == null) {
            return false;
        }
        if (!workspaceRepository.existsById(workspaceId)) {
            return false;
        }
        return membershipRepository.findByWorkspaceIdAndUserIdAndStatus(
                workspaceId, userId, MembershipStatus.ACCEPTED).isPresent();
    }

    /**
     * Chuyển đổi an toàn giá trị ID lưu trong Session sang Long.
     */
    private Long parseWorkspaceId(Object rawId) {
        if (rawId instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(rawId.toString().trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Lấy HttpSession an toàn trong mọi hoàn cảnh (runtime web hoặc test).
     */
    private HttpSession resolveSession() {
        if (this.testSession != null) {
            return this.testSession;
        }
        if (this.sessionProvider != null) {
            try {
                HttpSession session = this.sessionProvider.getIfAvailable();
                if (session != null) {
                    return session;
                }
            } catch (Exception ignored) {
            }
        }
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            return servletAttributes.getRequest().getSession(true);
        }
        return null;
    }
}

