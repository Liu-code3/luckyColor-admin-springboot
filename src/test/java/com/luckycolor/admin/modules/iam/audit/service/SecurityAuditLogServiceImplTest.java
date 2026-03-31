package com.luckycolor.admin.modules.iam.audit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.datascope.CurrentDataScopeResolver;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeRule;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeType;
import com.luckycolor.admin.infrastructure.security.jwt.JwtAccessTokenClaims;
import com.luckycolor.admin.infrastructure.security.jwt.JwtAuthenticatedUser;
import com.luckycolor.admin.infrastructure.security.jwt.JwtTokenService;
import com.luckycolor.admin.infrastructure.tenant.config.TenancyProperties;
import com.luckycolor.admin.modules.iam.audit.dataobject.SecurityAuditLogDO;
import com.luckycolor.admin.modules.iam.audit.mapper.SecurityAuditLogMapper;
import com.luckycolor.admin.modules.iam.audit.service.impl.SecurityAuditLogServiceImpl;
import com.luckycolor.admin.modules.iam.audit.web.request.SecurityAuditLogPageQuery;
import com.luckycolor.admin.modules.iam.audit.web.response.SecurityAuditLogPageResponse;
import com.luckycolor.admin.support.MyBatisTableInfoTestUtils;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

class SecurityAuditLogServiceImplTest {

    @Test
    void shouldReturnAuditLogPage() {
        SecurityAuditLogMapper mapper = Mockito.mock(SecurityAuditLogMapper.class);
        JwtTokenService jwtTokenService = Mockito.mock(JwtTokenService.class);
        SecurityAuditLogService service = new SecurityAuditLogServiceImpl(
            mapper,
            jwtTokenService,
            tenancyProperties(),
            noScopeBuilder()
        );
        SecurityAuditLogDO auditLog = new SecurityAuditLogDO();
        auditLog.setId(1L);
        auditLog.setTenantId(1001L);
        auditLog.setUserId(1L);
        auditLog.setUsername("admin");
        auditLog.setEventType("LOGIN_SUCCESS");
        auditLog.setSuccess(1);
        when(mapper.selectPageResult(any(), any())).thenReturn(PageResult.of(List.of(auditLog), 1L));

        PageResult<SecurityAuditLogPageResponse> result = service.pageSecurityAuditLogs(new SecurityAuditLogPageQuery());

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getList()).extracting(SecurityAuditLogPageResponse::eventType)
            .containsExactly("LOGIN_SUCCESS");
    }

    @Test
    void shouldAppendTenantDataScopeToAuditLogQuery() {
        MyBatisTableInfoTestUtils.initTableInfo(SecurityAuditLogDO.class);
        SecurityAuditLogMapper mapper = Mockito.mock(SecurityAuditLogMapper.class);
        SecurityAuditLogService service = new SecurityAuditLogServiceImpl(
            mapper,
            Mockito.mock(JwtTokenService.class),
            tenancyProperties(),
            tenantScopeBuilder(1001L)
        );
        when(mapper.selectPageResult(any(), any())).thenReturn(PageResult.of(List.of(), 0L));

        service.pageSecurityAuditLogs(new SecurityAuditLogPageQuery());

        ArgumentCaptor<Wrapper<SecurityAuditLogDO>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(mapper).selectPageResult(any(), wrapperCaptor.capture());
        LambdaQueryWrapper<SecurityAuditLogDO> wrapper = (LambdaQueryWrapper<SecurityAuditLogDO>) wrapperCaptor.getValue();
        assertThat(wrapper.getSqlSegment()).contains("tenant_id");
        assertThat(wrapper.getParamNameValuePairs()).containsValue(1001L);
    }

    @Test
    void shouldRecordUnauthorizedAuditFromBearerToken() {
        SecurityAuditLogMapper mapper = Mockito.mock(SecurityAuditLogMapper.class);
        JwtTokenService jwtTokenService = Mockito.mock(JwtTokenService.class);
        when(jwtTokenService.resolveBearerToken("Bearer jwt-token")).thenReturn("jwt-token");
        when(jwtTokenService.parseAccessToken("jwt-token")).thenReturn(
            new JwtAccessTokenClaims(1L, "admin", 1001L, List.of("ROLE_SUPER_ADMIN"))
        );
        SecurityAuditLogService service = new SecurityAuditLogServiceImpl(
            mapper,
            jwtTokenService,
            tenancyProperties(),
            noScopeBuilder()
        );
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/tenants/page");
        request.addHeader("Authorization", "Bearer jwt-token");
        request.setRemoteAddr("127.0.0.1");

        service.recordUnauthorized(request, "TOKEN_REVOKED");

        ArgumentCaptor<SecurityAuditLogDO> captor = ArgumentCaptor.forClass(SecurityAuditLogDO.class);
        verify(mapper).insert(captor.capture());
        SecurityAuditLogDO auditLog = captor.getValue();
        assertThat(auditLog.getUserId()).isEqualTo(1L);
        assertThat(auditLog.getUsername()).isEqualTo("admin");
        assertThat(auditLog.getTenantId()).isEqualTo(1001L);
        assertThat(auditLog.getEventType()).isEqualTo("UNAUTHORIZED");
        assertThat(auditLog.getSuccess()).isEqualTo(0);
        assertThat(auditLog.getRequestMethod()).isEqualTo("GET");
        assertThat(auditLog.getRequestUri()).isEqualTo("/admin/tenants/page");
        assertThat(auditLog.getReason()).isEqualTo("TOKEN_REVOKED");
    }

    @Test
    void shouldRecordAccessDeniedAuditFromAuthenticatedPrincipal() {
        SecurityAuditLogMapper mapper = Mockito.mock(SecurityAuditLogMapper.class);
        SecurityAuditLogService service = new SecurityAuditLogServiceImpl(
            mapper,
            Mockito.mock(JwtTokenService.class),
            tenancyProperties(),
            noScopeBuilder()
        );
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/security-audit-logs/page");
        request.setRemoteAddr("127.0.0.1");
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            new JwtAuthenticatedUser(1L, "admin", 1001L, List.of("ROLE_SUPER_ADMIN")),
            "jwt-token",
            List.of()
        );

        service.recordAccessDenied(request, authentication, "Forbidden");

        ArgumentCaptor<SecurityAuditLogDO> captor = ArgumentCaptor.forClass(SecurityAuditLogDO.class);
        verify(mapper).insert(captor.capture());
        SecurityAuditLogDO auditLog = captor.getValue();
        assertThat(auditLog.getEventType()).isEqualTo("ACCESS_DENIED");
        assertThat(auditLog.getUsername()).isEqualTo("admin");
        assertThat(auditLog.getTenantId()).isEqualTo(1001L);
        assertThat(auditLog.getReason()).isEqualTo("Forbidden");
    }

    private TenancyProperties tenancyProperties() {
        return new TenancyProperties();
    }

    private DataScopeConditionBuilder noScopeBuilder() {
        CurrentDataScopeResolver resolver = Mockito.mock(CurrentDataScopeResolver.class);
        when(resolver.resolveCurrentRule()).thenReturn(java.util.Optional.empty());
        return new DataScopeConditionBuilder(resolver);
    }

    private DataScopeConditionBuilder tenantScopeBuilder(Long tenantId) {
        CurrentDataScopeResolver resolver = Mockito.mock(CurrentDataScopeResolver.class);
        when(resolver.resolveCurrentRule()).thenReturn(
            java.util.Optional.of(new DataScopeRule(DataScopeType.TENANT, tenantId, null, List.of(tenantId), List.of()))
        );
        return new DataScopeConditionBuilder(resolver);
    }
}
