package com.luckycolor.admin.modules.system.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.datascope.CurrentDataScopeResolver;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.modules.iam.auth.config.LocalAuthProperties;
import com.luckycolor.admin.modules.system.user.dataobject.SystemUserDO;
import com.luckycolor.admin.modules.system.user.mapper.SystemUserMapper;
import com.luckycolor.admin.modules.system.user.service.impl.SystemUserServiceImpl;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserAssignRolesRequest;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserPageQuery;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserResetPasswordRequest;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserSaveRequest;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserStatusRequest;
import com.luckycolor.admin.modules.system.user.web.response.SystemUserDetailResponse;
import com.luckycolor.admin.modules.system.user.web.response.SystemUserExportPreviewResponse;
import com.luckycolor.admin.modules.system.user.web.response.SystemUserPageResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

class SystemUserServiceImplTest {

    @Test
    void shouldReturnUserPage() {
        SystemUserMapper mapper = Mockito.mock(SystemUserMapper.class);
        when(mapper.selectPageResult(any(), any())).thenReturn(PageResult.of(List.of(user()), 1L));
        SystemUserService service = new SystemUserServiceImpl(mapper, noScopeBuilder(), localAuthProperties(), passwordEncoder());

        PageResult<SystemUserPageResponse> result = service.pageUsers(new SystemUserPageQuery());

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getList()).extracting(SystemUserPageResponse::username).containsExactly("admin");
    }

    @Test
    void shouldReturnUserDetail() {
        SystemUserMapper mapper = Mockito.mock(SystemUserMapper.class);
        when(mapper.selectById(1L)).thenReturn(user());
        SystemUserService service = new SystemUserServiceImpl(mapper, noScopeBuilder(), localAuthProperties(), passwordEncoder());

        SystemUserDetailResponse result = service.getUser(1L);

        assertThat(result.username()).isEqualTo("admin");
        assertThat(result.roleCodes()).containsExactly("ROLE_SUPER_ADMIN", "ROLE_ADMIN");
    }

    @Test
    void shouldReturnRoleOptions() {
        SystemUserMapper mapper = Mockito.mock(SystemUserMapper.class);
        when(mapper.selectList(any(Wrapper.class))).thenReturn(List.of(user()));
        SystemUserService service = new SystemUserServiceImpl(mapper, noScopeBuilder(), localAuthProperties(), passwordEncoder());

        List<String> result = service.listRoleOptions();

        assertThat(result).contains("ROLE_SUPER_ADMIN", "ROLE_ADMIN", "ROLE_TENANT_OPERATOR");
    }

    @Test
    void shouldReturnExportPreview() {
        SystemUserMapper mapper = Mockito.mock(SystemUserMapper.class);
        when(mapper.selectList(any(Wrapper.class))).thenReturn(List.of(user()));
        SystemUserPageQuery query = new SystemUserPageQuery();
        query.setPageSize(50L);
        SystemUserService service = new SystemUserServiceImpl(mapper, noScopeBuilder(), localAuthProperties(), passwordEncoder());

        List<SystemUserExportPreviewResponse> result = service.listUsersForExportPreview(query);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).username()).isEqualTo("admin");
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        SystemUserMapper mapper = Mockito.mock(SystemUserMapper.class);
        when(mapper.selectById(99L)).thenReturn(null);
        SystemUserService service = new SystemUserServiceImpl(mapper, noScopeBuilder(), localAuthProperties(), passwordEncoder());

        assertThatThrownBy(() -> service.getUser(99L))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("404 NOT_FOUND");
    }

    @Test
    void shouldCreateUser() {
        SystemUserMapper mapper = Mockito.mock(SystemUserMapper.class);
        when(mapper.selectList(any(Wrapper.class))).thenReturn(List.of());
        SystemUserService service = new SystemUserServiceImpl(mapper, noScopeBuilder(), localAuthProperties(), passwordEncoder());

        Long result = service.createUser(saveRequest());

        assertThat(result).isNull();
        verify(mapper).insert(any(SystemUserDO.class));
    }

    @Test
    void shouldUpdateUserStatus() {
        SystemUserMapper mapper = Mockito.mock(SystemUserMapper.class);
        SystemUserDO user = user();
        when(mapper.selectById(1L)).thenReturn(user);
        SystemUserService service = new SystemUserServiceImpl(mapper, noScopeBuilder(), localAuthProperties(), passwordEncoder());
        SystemUserStatusRequest request = new SystemUserStatusRequest();
        request.setStatus(1);

        service.updateUserStatus(1L, request);

        assertThat(user.getStatus()).isEqualTo(1);
        verify(mapper).updateById(user);
    }

    @Test
    void shouldDeleteUser() {
        SystemUserMapper mapper = Mockito.mock(SystemUserMapper.class);
        when(mapper.selectById(1L)).thenReturn(user());
        SystemUserService service = new SystemUserServiceImpl(mapper, noScopeBuilder(), localAuthProperties(), passwordEncoder());

        service.deleteUser(1L);

        verify(mapper).deleteById(1L);
    }

    @Test
    void shouldResetPassword() {
        SystemUserMapper mapper = Mockito.mock(SystemUserMapper.class);
        SystemUserDO user = user();
        when(mapper.selectById(1L)).thenReturn(user);
        SystemUserService service = new SystemUserServiceImpl(mapper, noScopeBuilder(), localAuthProperties(), passwordEncoder());
        SystemUserResetPasswordRequest request = new SystemUserResetPasswordRequest();
        request.setNewPassword("new-password");

        service.resetPassword(1L, request);

        assertThat(user.getPassword()).isEqualTo("$2a$encoded-password");
        verify(mapper).updateById(user);
    }

    @Test
    void shouldAssignRoles() {
        SystemUserMapper mapper = Mockito.mock(SystemUserMapper.class);
        SystemUserDO user = user();
        when(mapper.selectById(1L)).thenReturn(user);
        SystemUserService service = new SystemUserServiceImpl(mapper, noScopeBuilder(), localAuthProperties(), passwordEncoder());
        SystemUserAssignRolesRequest request = new SystemUserAssignRolesRequest();
        request.setRoleCodes(List.of("ROLE_ADMIN", "ROLE_EDITOR"));

        service.assignRoles(1L, request);

        assertThat(user.getRoleCodes()).isEqualTo("ROLE_ADMIN,ROLE_EDITOR");
        verify(mapper).updateById(user);
    }

    @Test
    void shouldExportUsers() {
        SystemUserMapper mapper = Mockito.mock(SystemUserMapper.class);
        when(mapper.selectList(any(Wrapper.class))).thenReturn(List.of(user()));
        SystemUserService service = new SystemUserServiceImpl(mapper, noScopeBuilder(), localAuthProperties(), passwordEncoder());

        byte[] result = service.exportUsers(new SystemUserPageQuery());

        assertThat(new String(result, java.nio.charset.StandardCharsets.UTF_8)).contains("username,nickname,email");
        assertThat(new String(result, java.nio.charset.StandardCharsets.UTF_8)).contains("admin");
    }

    @Test
    void shouldImportUsers() {
        SystemUserMapper mapper = Mockito.mock(SystemUserMapper.class);
        when(mapper.selectList(any(Wrapper.class))).thenReturn(List.of());
        SystemUserService service = new SystemUserServiceImpl(mapper, noScopeBuilder(), localAuthProperties(), passwordEncoder());
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "users.csv",
            "text/csv",
            """
                username,nickname,email,mobile,roleCodes,permissionCodes,dataScope,password,status,remark
                import-user,Import User,import@example.com,13800000001,ROLE_ADMIN,system:user:query,ALL,import123,0,imported
                """.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );

        int result = service.importUsers(file);

        assertThat(result).isEqualTo(1);
        verify(mapper).insert(any(SystemUserDO.class));
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

    private PasswordEncoder passwordEncoder() {
        PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
        when(passwordEncoder.encode(any())).thenReturn("$2a$encoded-password");
        return passwordEncoder;
    }

    private SystemUserSaveRequest saveRequest() {
        SystemUserSaveRequest request = new SystemUserSaveRequest();
        request.setUsername("admin");
        request.setPassword("admin123");
        request.setNickname("System Admin");
        request.setEmail("admin@example.com");
        request.setMobile("13800000000");
        request.setRoleCodes(List.of("ROLE_SUPER_ADMIN"));
        request.setPermissionCodes(List.of("system:user:query"));
        request.setDataScope("ALL");
        request.setDepartmentId(100L);
        request.setDepartmentIds(List.of(100L, 101L));
        request.setScopeTenantIds(List.of(1L));
        request.setStatus(0);
        request.setRemark("default");
        return request;
    }
}
