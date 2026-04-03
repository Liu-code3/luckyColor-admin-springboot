package com.luckycolor.admin.modules.iam.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.luckycolor.admin.modules.iam.auth.config.LocalAuthProperties;
import com.luckycolor.admin.modules.iam.auth.model.AuthUser;
import com.luckycolor.admin.modules.iam.auth.service.impl.LocalAuthUserServiceImpl;
import com.luckycolor.admin.modules.iam.auth.service.impl.PersistenceAuthUserServiceImpl;
import com.luckycolor.admin.modules.system.role.dataobject.SystemRoleDO;
import com.luckycolor.admin.modules.system.role.mapper.SystemRoleMapper;
import com.luckycolor.admin.modules.system.user.dataobject.SystemUserDO;
import com.luckycolor.admin.modules.system.user.mapper.SystemUserMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PersistenceAuthUserServiceImplTest {

    @Test
    void shouldResolveUserFromDatabaseBeforeFallback() {
        SystemUserMapper systemUserMapper = Mockito.mock(SystemUserMapper.class);
        SystemRoleMapper systemRoleMapper = Mockito.mock(SystemRoleMapper.class);
        PersistenceAuthUserServiceImpl service = new PersistenceAuthUserServiceImpl(
            systemUserMapper,
            systemRoleMapper,
            new LocalAuthUserServiceImpl(buildLocalAuthProperties())
        );

        SystemUserDO user = new SystemUserDO();
        user.setId(1L);
        user.setTenantId(1L);
        user.setUsername("admin");
        user.setPassword("encoded");
        user.setNickname("System Admin");
        user.setStatus(0);
        user.setRoleCodes("ROLE_EDITOR,ROLE_ADMIN");
        user.setPermissionCodes("system:user:reset-password");
        user.setDataScope(null);
        user.setDepartmentId(100L);
        user.setDepartmentIds("100,101");
        user.setScopeTenantIds("1,2");

        SystemRoleDO adminRole = role(11L, 1L, "ROLE_ADMIN", "Admin", 2, 0);
        adminRole.setPermissionCodes("system:user:query,system:user:create");
        adminRole.setDataScope("ALL");

        SystemRoleDO disabledEditorRole = role(12L, 1L, "ROLE_EDITOR", "Editor", 1, 1);
        disabledEditorRole.setPermissionCodes("system:user:update");

        when(systemUserMapper.selectOne(any())).thenReturn(user);
        when(systemRoleMapper.selectList(any())).thenReturn(List.of(adminRole, disabledEditorRole));

        AuthUser result = service.findByUsername("admin");

        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.username()).isEqualTo("admin");
        assertThat(result.nickname()).isEqualTo("System Admin");
        assertThat(result.roles()).containsExactly("ROLE_ADMIN");
        assertThat(result.permissions()).containsExactly(
            "system:user:query",
            "system:user:create",
            "system:user:reset-password"
        );
        assertThat(result.dataScope()).isEqualTo("ALL");
        assertThat(result.departmentIds()).containsExactly(100L, 101L);
        assertThat(result.scopeTenantIds()).containsExactly(1L, 2L);
    }

    @Test
    void shouldFallbackToLocalUserWhenDatabaseUserMissing() {
        SystemUserMapper systemUserMapper = Mockito.mock(SystemUserMapper.class);
        SystemRoleMapper systemRoleMapper = Mockito.mock(SystemRoleMapper.class);
        PersistenceAuthUserServiceImpl service = new PersistenceAuthUserServiceImpl(
            systemUserMapper,
            systemRoleMapper,
            new LocalAuthUserServiceImpl(buildLocalAuthProperties())
        );

        when(systemUserMapper.selectOne(any())).thenReturn(null);

        AuthUser result = service.findByUsername("local-admin");

        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(99L);
        assertThat(result.username()).isEqualTo("local-admin");
        assertThat(result.roles()).containsExactly("ROLE_LOCAL_ADMIN");
        verify(systemUserMapper).selectOne(any());
    }

    private LocalAuthProperties buildLocalAuthProperties() {
        LocalAuthProperties properties = new LocalAuthProperties();
        LocalAuthProperties.User user = new LocalAuthProperties.User();
        user.setUserId(99L);
        user.setUsername("local-admin");
        user.setPassword("local123");
        user.setTenantId(9L);
        user.setNickname("Local Admin");
        user.setStatus(0);
        user.setRoles(List.of("ROLE_LOCAL_ADMIN"));
        user.setPermissions(List.of("local:debug"));
        properties.setLocalUsers(List.of(user));
        return properties;
    }

    private SystemRoleDO role(Long id, Long tenantId, String code, String name, Integer sort, Integer status) {
        SystemRoleDO role = new SystemRoleDO();
        role.setId(id);
        role.setTenantId(tenantId);
        role.setRoleCode(code);
        role.setRoleName(name);
        role.setSort(sort);
        role.setStatus(status);
        return role;
    }
}
