package com.luckycolor.admin.modules.frontend.web;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.infrastructure.security.jwt.JwtAuthenticatedUser;
import com.luckycolor.admin.modules.iam.auth.model.AuthUser;
import com.luckycolor.admin.modules.iam.auth.service.AuthUserService;
import com.luckycolor.admin.modules.platform.dashboard.service.DashboardService;
import com.luckycolor.admin.modules.platform.dashboard.web.response.DashboardOverviewResponse;
import com.luckycolor.admin.modules.platform.dashboard.web.response.DashboardRecentVisitResponse;
import com.luckycolor.admin.modules.platform.dashboard.web.response.DashboardVisitTrendResponse;
import jakarta.validation.Valid;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class FrontendCompatibilityController {

    private static final Logger log = LoggerFactory.getLogger(FrontendCompatibilityController.class);

    private static final String LAYOUT_MODULAR = "modular";

    private final AuthUserService authUserService;
    private final DashboardService dashboardService;

    public FrontendCompatibilityController(
        AuthUserService authUserService,
        @Nullable DashboardService dashboardService
    ) {
        this.authUserService = authUserService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard/overview")
    @RequirePermission("dashboard:query")
    public ApiResponse<LegacyDashboardOverviewResponse> dashboardOverview(Authentication authentication) {
        AuthUser user = getRequiredUser(authentication);
        return ApiResponse.success(buildDashboardOverview(user));
    }

    @PostMapping("/dashboard/track-visit")
    public ApiResponse<LegacyDashboardVisitTrackResponse> trackDashboardVisit(
        Authentication authentication,
        @Valid @RequestBody LegacyDashboardVisitTrackRequest request
    ) {
        getRequiredUser(authentication);
        return ApiResponse.success(new LegacyDashboardVisitTrackResponse(
            UUID.randomUUID().toString(),
            Instant.now().toString()
        ));
    }

    private AuthUser getRequiredUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtAuthenticatedUser principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        AuthUser user = authUserService.getByUserId(principal.userId());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return user;
    }

    private List<LegacyMenuItem> buildMenuTree(AuthUser user) {
        List<LegacyMenuItem> items = new ArrayList<>();
        items.add(page(100, 0, "工作台", "dashboardIndex", "/index", "index/index", "solar:home-2-linear"));

        List<LegacyMenuItem> systemChildren = new ArrayList<>();
        addIfPermitted(systemChildren, user, "system:user:query",
            page(201, 200, "用户管理", "systemUsers", "/systemManagement/system/users", "sys/user", "solar:users-group-rounded-linear"));
        addIfPermitted(systemChildren, user, "system:role:query",
            page(202, 200, "角色管理", "systemRole", "/systemManagement/system/role", "sys/role/index", "solar:shield-user-linear"));
        addIfPermitted(systemChildren, user, "system:department:query",
            page(203, 200, "部门管理", "systemDepartment", "/systemManagement/system/department", "sys/department/department", "solar:buildings-linear"));
        addIfPermitted(systemChildren, user, "system:dictionary:query",
            page(204, 200, "字典管理", "systemDict", "/systemManagement/system/dict", "sys/dict/index", "solar:book-bookmark-linear"));
        addIfPermitted(systemChildren, user, "system:config:query",
            page(205, 200, "参数管理", "systemConfig", "/systemManagement/system/config", "sys/config/index", "solar:settings-linear"));
        addIfPermitted(systemChildren, user, "system:notice:query",
            page(206, 200, "通知公告", "systemNotice", "/systemManagement/system/notice", "sys/notice/index", "solar:bell-linear"));
        if (!systemChildren.isEmpty()) {
            items.add(directory(200, 0, "系统管理", "systemManagement", "/systemManagement", "sys/index", "solar:settings-linear", systemChildren));
        }

        List<LegacyMenuItem> tenantChildren = new ArrayList<>();
        addIfPermitted(tenantChildren, user, "tenant:query",
            page(301, 300, "租户管理", "tenantManagement", "/tenantCenter/tenant", "sys/tenant/index", "solar:buildings-2-linear"));
        addIfPermitted(tenantChildren, user, "tenant:package:query",
            page(302, 300, "租户套餐", "tenantPackage", "/tenantCenter/tenantPackage", "sys/tenantPackage/index", "solar:box-linear"));
        if (!tenantChildren.isEmpty()) {
            items.add(directory(300, 0, "租户中心", "tenantCenter", "/tenantCenter", "sys/index", "solar:buildings-2-linear", tenantChildren));
        }

        List<LegacyMenuItem> toolChildren = new ArrayList<>();
        addIfPermitted(toolChildren, user, "codegen:query",
            page(401, 400, "代码生成", "toolCodegen", "/tool/codegen", "tool/codegen/index", "solar:code-square-linear"));
        if (!toolChildren.isEmpty()) {
            items.add(directory(400, 0, "开发工具", "toolCenter", "/tool", "sys/index", "solar:widget-4-linear", toolChildren));
        }

        return items;
    }

    private LegacyDashboardOverviewResponse buildDashboardOverview(AuthUser user) {
        DashboardOverviewResponse overview = loadDashboardOverview();
        DashboardVisitTrendResponse visitTrend = loadDashboardVisitTrend();
        List<DashboardRecentVisitResponse> recentVisits = loadRecentVisits();

        return new LegacyDashboardOverviewResponse(
            new LegacyDashboardUser(user.userId(), user.username(), user.nickname()),
            new LegacyDashboardStats(
                overview.activeUserCount(),
                overview.loginTodayCount(),
                visitTrend.totalVisits(),
                300
            ),
            visitTrend.points().stream()
                .map(point -> new LegacyDashboardTrendPoint(
                    point.date().toString(),
                    point.visitCount(),
                    point.successCount()
                ))
                .toList(),
            recentVisits.stream()
                .map(item -> new LegacyDashboardRecentVisit(
                    normalizeRoutePath(item.requestUri()),
                    resolveRouteTitle(item.requestUri()),
                    null,
                    formatInstant(item.accessTime())
                ))
                .toList(),
            List.of()
        );
    }

    private DashboardOverviewResponse loadDashboardOverview() {
        if (dashboardService == null) {
            return new DashboardOverviewResponse(0, 0, 0, 0, 0, 0);
        }
        try {
            return dashboardService.getOverview();
        } catch (RuntimeException exception) {
            log.warn("failed to load dashboard overview from native service, fallback to empty payload", exception);
            return new DashboardOverviewResponse(0, 0, 0, 0, 0, 0);
        }
    }

    private DashboardVisitTrendResponse loadDashboardVisitTrend() {
        if (dashboardService == null) {
            return new DashboardVisitTrendResponse(List.of(), 0, 0, 0);
        }
        try {
            return dashboardService.getVisitTrend();
        } catch (RuntimeException exception) {
            log.warn("failed to load dashboard visit trend from native service, fallback to empty payload", exception);
            return new DashboardVisitTrendResponse(List.of(), 0, 0, 0);
        }
    }

    private List<DashboardRecentVisitResponse> loadRecentVisits() {
        if (dashboardService == null) {
            return List.of();
        }
        try {
            return dashboardService.listRecentVisits();
        } catch (RuntimeException exception) {
            log.warn("failed to load dashboard recent visits from native service, fallback to empty payload", exception);
            return List.of();
        }
    }

    private String normalizeRoutePath(String requestUri) {
        if (requestUri == null || requestUri.isBlank()) {
            return "/index";
        }
        return switch (requestUri) {
            case "/admin/users/page" -> "/systemManagement/system/users";
            case "/admin/roles/page" -> "/systemManagement/system/role";
            case "/admin/departments/tree" -> "/systemManagement/system/department";
            case "/admin/dictionaries/user_status/items", "/admin/dictionaries/page" -> "/systemManagement/system/dict";
            case "/admin/system-configs/page" -> "/systemManagement/system/config";
            case "/admin/notices/page" -> "/systemManagement/system/notice";
            case "/admin/tenants/page" -> "/tenantCenter/tenant";
            case "/admin/tenant-packages/page" -> "/tenantCenter/tenantPackage";
            default -> "/index";
        };
    }

    private String resolveRouteTitle(String requestUri) {
        if (requestUri == null || requestUri.isBlank()) {
            return "工作台";
        }
        return switch (requestUri) {
            case "/admin/users/page" -> "用户管理";
            case "/admin/roles/page" -> "角色管理";
            case "/admin/departments/tree" -> "部门管理";
            case "/admin/dictionaries/user_status/items", "/admin/dictionaries/page" -> "字典管理";
            case "/admin/system-configs/page" -> "参数管理";
            case "/admin/notices/page" -> "通知公告";
            case "/admin/tenants/page" -> "租户管理";
            case "/admin/tenant-packages/page" -> "租户套餐";
            default -> "工作台";
        };
    }

    private String formatInstant(java.time.LocalDateTime value) {
        if (value == null) {
            return Instant.now().toString();
        }
        return value.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private void addIfPermitted(List<LegacyMenuItem> items, AuthUser user, String permission, LegacyMenuItem item) {
        if (user.permissions().contains(permission)) {
            items.add(item);
        }
    }

    private LegacyMenuItem directory(
        long id,
        long pid,
        String title,
        String name,
        String path,
        String component,
        String icon,
        List<LegacyMenuItem> children
    ) {
        return new LegacyMenuItem(id, pid, pid, title, name, 1, path, "menu_" + id, icon, LAYOUT_MODULAR, component, null, true, null, children);
    }

    private LegacyMenuItem page(
        long id,
        long pid,
        String title,
        String name,
        String path,
        String component,
        String icon
    ) {
        return new LegacyMenuItem(
            id,
            pid,
            pid,
            title,
            name,
            2,
            path,
            "menu_" + id,
            icon,
            LAYOUT_MODULAR,
            component,
            null,
            true,
            new LegacyMenuMeta(null, true, false),
            List.of()
        );
    }

    public record LegacyMenuItem(
        long id,
        long pid,
        long parentId,
        String title,
        String name,
        int type,
        String path,
        String key,
        String icon,
        String layout,
        String component,
        String redirect,
        boolean isVisible,
        LegacyMenuMeta meta,
        List<LegacyMenuItem> children
    ) {
    }

    public record LegacyMenuMeta(
        String type,
        boolean keepAlive,
        boolean hidden
    ) {
    }

    public record LegacyDashboardOverviewResponse(
        LegacyDashboardUser user,
        LegacyDashboardStats stats,
        List<LegacyDashboardTrendPoint> trend,
        List<LegacyDashboardRecentVisit> recentVisits,
        List<LegacyDashboardNotice> notices
    ) {
    }

    public record LegacyDashboardUser(Long id, String username, String nickname) {
    }

    public record LegacyDashboardStats(long onlineUsers, long visitorUv, long pageViews, long onlineWindowSeconds) {
    }

    public record LegacyDashboardTrendPoint(String date, long pv, long uv) {
    }

    public record LegacyDashboardRecentVisit(String routePath, String routeTitle, String routeIcon, String lastVisitedAt) {
    }

    public record LegacyDashboardNotice(
        String id,
        String title,
        String content,
        String type,
        boolean status,
        String publisher,
        String publishedAt,
        String createdAt
    ) {
    }

    public record LegacyDashboardVisitTrackRequest(
        String visitorId,
        String sessionId,
        String routePath,
        String routeTitle,
        String routeIcon
    ) {
    }

    public record LegacyDashboardVisitTrackResponse(String id, String visitedAt) {
    }
}
