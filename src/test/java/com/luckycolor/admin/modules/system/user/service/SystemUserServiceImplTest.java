package com.luckycolor.admin.modules.system.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.datascope.CurrentDataScopeResolver;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.modules.iam.auth.config.LocalAuthProperties;
import com.luckycolor.admin.modules.system.user.dataobject.SystemUserDO;
import com.luckycolor.admin.modules.system.user.mapper.SystemUserMapper;
import com.luckycolor.admin.modules.system.user.service.impl.SystemUserServiceImpl;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserPageQuery;
import com.luckycolor.admin.modules.system.user.web.response.SystemUserDetailResponse;
import com.luckycolor.admin.modules.system.user.web.response.SystemUserExportPreviewResponse;
import com.luckycolor.admin.modules.system.user.web.response.SystemUserPageResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

class SystemUserServiceImplTest {

    @Test
    void shouldReturnUserPage() {
        SystemUserMapper mapper = Mockito.mock(SystemUserMapper.class);
        when(mapper.selectPageResult(any(), any())).thenReturn(PageResult.of(List.of(user()), 1L));
        SystemUserService service = new SystemUserServiceImpl(mapper, noScopeBuilder(), localAuthProperties());

        PageResult<SystemUserPageResponse> result = service.pageUsers(new SystemUserPageQuery());

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getList()).extracting(SystemUserPageResponse::username).containsExactly("admin");
    }

    @Test
    void shouldReturnUserDetail() {
        SystemUserMapper mapper = Mockito.mock(SystemUserMapper.class);
        when(mapper.selectById(1L)).thenReturn(user());
        SystemUserService service = new SystemUserServiceImpl(mapper, noScopeBuilder(), localAuthProperties());

        SystemUserDetailResponse result = service.getUser(1L);

        assertThat(result.username()).isEqualTo("admin");
        assertThat(result.roleCodes()).containsExactly("ROLE_SUPER_ADMIN", "ROLE_ADMIN");
    }

    @Test
    void shouldReturnRoleOptions() {
        SystemUserMapper mapper = Mockito.mock(SystemUserMapper.class);
        when(mapper.selectList(any(Wrapper.class))).thenReturn(List.of(user()));
        SystemUserService service = new SystemUserServiceImpl(mapper, noScopeBuilder(), localAuthProperties());

        List<String> result = service.listRoleOptions();

        assertThat(result).contains("ROLE_SUPER_ADMIN", "ROLE_ADMIN", "ROLE_TENANT_OPERATOR");
    }

    @Test
    void shouldReturnExportPreview() {
        SystemUserMapper mapper = Mockito.mock(SystemUserMapper.class);
        when(mapper.selectList(any(Wrapper.class))).thenReturn(List.of(user()));
        SystemUserPageQuery query = new SystemUserPageQuery();
        query.setPageSize(50L);
        SystemUserService service = new SystemUserServiceImpl(mapper, noScopeBuilder(), localAuthProperties());

        List<SystemUserExportPreviewResponse> result = service.listUsersForExportPreview(query);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).username()).isEqualTo("admin");
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        SystemUserMapper mapper = Mockito.mock(SystemUserMapper.class);
        when(mapper.selectById(99L)).thenReturn(null);
        SystemUserService service = new SystemUserServiceImpl(mapper, noScopeBuilder(), localAuthProperties());

        assertThatThrownBy(() -> service.getUser(99L))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("404 NOT_FOUND");
    }

    private SystemUserDO user() {
        SystemUserDO user = new SystemUserDO();
        user.setId(1L);
        user.setTenantId(1L);
        user.setUsername("admin");
        user.setPassword("encoded");
        user.setNickname("System Admin");
        user.setEmail("admin@example.com");
        user.setMobile("13800000000");
        user.setDepartmentId(100L);
        user.setRoleCodes("ROLE_SUPER_ADMIN,ROLE_ADMIN");
        user.setPermissionCodes("system:user:query,system:user:create");
        user.setDataScope("ALL");
        user.setDepartmentIds("100,101");
        user.setScopeTenantIds("1,2");
        user.setStatus(0);
        user.setRemark("default");
        return user;
    }

    private DataScopeConditionBuilder noScopeBuilder() {
        CurrentDataScopeResolver resolver = Mockito.mock(CurrentDataScopeResolver.class);
        when(resolver.resolveCurrentRule()).thenReturn(java.util.Optional.empty());
        return new DataScopeConditionBuilder(resolver);
    }

    private LocalAuthProperties localAuthProperties() {
        LocalAuthProperties properties = new LocalAuthProperties();
        LocalAuthProperties.User user = new LocalAuthProperties.User();
        user.setRoles(List.of("ROLE_TENANT_OPERATOR"));
        properties.setLocalUsers(List.of(user));
        return properties;
    }
}
