package com.luckycolor.admin.modules.platform.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.luckycolor.admin.infrastructure.security.datascope.CurrentDataScopeResolver;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.modules.iam.audit.dataobject.SecurityAuditLogDO;
import com.luckycolor.admin.modules.iam.audit.mapper.SecurityAuditLogMapper;
import com.luckycolor.admin.modules.platform.dashboard.service.impl.DashboardServiceImpl;
import com.luckycolor.admin.modules.platform.dashboard.web.response.DashboardOverviewResponse;
import com.luckycolor.admin.modules.platform.dashboard.web.response.DashboardRecentVisitResponse;
import com.luckycolor.admin.modules.platform.dashboard.web.response.DashboardVisitTrendResponse;
import com.luckycolor.admin.modules.system.menu.mapper.MenuMapper;
import com.luckycolor.admin.modules.system.notice.mapper.NoticeMapper;
import com.luckycolor.admin.modules.system.role.mapper.SystemRoleMapper;
import com.luckycolor.admin.modules.system.user.mapper.SystemUserMapper;
import com.luckycolor.admin.modules.tenant.tenant.mapper.TenantMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DashboardServiceImplTest {

    @Test
    void shouldReturnDashboardOverview() {
        TenantMapper tenantMapper = Mockito.mock(TenantMapper.class);
        SystemUserMapper userMapper = Mockito.mock(SystemUserMapper.class);
        SystemRoleMapper roleMapper = Mockito.mock(SystemRoleMapper.class);
        MenuMapper menuMapper = Mockito.mock(MenuMapper.class);
        NoticeMapper noticeMapper = Mockito.mock(NoticeMapper.class);
        SecurityAuditLogMapper securityAuditLogMapper = Mockito.mock(SecurityAuditLogMapper.class);
        mockCounts(tenantMapper, userMapper, roleMapper, menuMapper, noticeMapper, securityAuditLogMapper);
        DashboardService service = new DashboardServiceImpl(
            tenantMapper,
            userMapper,
            roleMapper,
            menuMapper,
            noticeMapper,
            securityAuditLogMapper,
            noScopeBuilder()
        );

        DashboardOverviewResponse result = service.getOverview();

        assertThat(result.activeTenantCount()).isEqualTo(3L);
        assertThat(result.activeUserCount()).isEqualTo(12L);
        assertThat(result.loginTodayCount()).isEqualTo(18L);
    }

    @Test
    void shouldReturnVisitTrend() {
        SecurityAuditLogMapper securityAuditLogMapper = Mockito.mock(SecurityAuditLogMapper.class);
        when(securityAuditLogMapper.selectList(any())).thenReturn(List.of(
            loginLog(1L, "admin", 1, LocalDateTime.now().minusDays(1)),
            loginLog(2L, "operator", 0, LocalDateTime.now().minusDays(1)),
            loginLog(3L, "admin", 1, LocalDateTime.now())
        ));
        DashboardService service = new DashboardServiceImpl(
            Mockito.mock(TenantMapper.class),
            Mockito.mock(SystemUserMapper.class),
            Mockito.mock(SystemRoleMapper.class),
            Mockito.mock(MenuMapper.class),
            Mockito.mock(NoticeMapper.class),
            securityAuditLogMapper,
            noScopeBuilder()
        );

        DashboardVisitTrendResponse result = service.getVisitTrend();

        assertThat(result.points()).hasSize(7);
        assertThat(result.totalVisits()).isEqualTo(3L);
        assertThat(result.successVisits()).isEqualTo(2L);
        assertThat(result.failedVisits()).isEqualTo(1L);
    }

    @Test
    void shouldReturnRecentVisits() {
        SecurityAuditLogMapper securityAuditLogMapper = Mockito.mock(SecurityAuditLogMapper.class);
        when(securityAuditLogMapper.selectList(any())).thenReturn(List.of(
            loginLog(1L, "admin", 1, LocalDateTime.of(2026, 4, 1, 9, 0))
        ));
        DashboardService service = new DashboardServiceImpl(
            Mockito.mock(TenantMapper.class),
            Mockito.mock(SystemUserMapper.class),
            Mockito.mock(SystemRoleMapper.class),
            Mockito.mock(MenuMapper.class),
            Mockito.mock(NoticeMapper.class),
            securityAuditLogMapper,
            noScopeBuilder()
        );

        List<DashboardRecentVisitResponse> result = service.listRecentVisits();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).username()).isEqualTo("admin");
    }

    private void mockCounts(
        TenantMapper tenantMapper,
        SystemUserMapper userMapper,
        SystemRoleMapper roleMapper,
        MenuMapper menuMapper,
        NoticeMapper noticeMapper,
        SecurityAuditLogMapper securityAuditLogMapper
    ) {
        when(tenantMapper.selectCount(any())).thenReturn(3L);
        when(userMapper.selectCount(any())).thenReturn(12L);
        when(roleMapper.selectCount(any())).thenReturn(5L);
        when(menuMapper.selectCount(any())).thenReturn(22L);
        when(noticeMapper.selectCount(any())).thenReturn(4L);
        when(securityAuditLogMapper.selectCount(any())).thenReturn(18L);
    }

    private SecurityAuditLogDO loginLog(Long userId, String username, Integer success, LocalDateTime createTime) {
        SecurityAuditLogDO auditLog = new SecurityAuditLogDO();
        auditLog.setId(userId);
        auditLog.setTenantId(1L);
        auditLog.setUserId(userId);
        auditLog.setUsername(username);
        auditLog.setEventType("LOGIN_SUCCESS");
        auditLog.setSuccess(success);
        auditLog.setRequestUri("/auth/login");
        auditLog.setRemoteIp("127.0.0.1");
        auditLog.setCreateTime(createTime);
        return auditLog;
    }

    private DataScopeConditionBuilder noScopeBuilder() {
        CurrentDataScopeResolver resolver = Mockito.mock(CurrentDataScopeResolver.class);
        when(resolver.resolveCurrentRule()).thenReturn(java.util.Optional.empty());
        return new DataScopeConditionBuilder(resolver);
    }
}
