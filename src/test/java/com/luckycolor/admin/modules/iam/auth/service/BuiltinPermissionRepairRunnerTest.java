package com.luckycolor.admin.modules.iam.auth.service;

import com.luckycolor.admin.modules.iam.auth.service.impl.BuiltinPermissionRepairRunner;
import com.luckycolor.admin.modules.system.role.dataobject.SystemRoleDO;
import com.luckycolor.admin.modules.system.role.mapper.SystemRoleMapper;
import com.luckycolor.admin.modules.system.user.dataobject.SystemUserDO;
import com.luckycolor.admin.modules.system.user.mapper.SystemUserMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class BuiltinPermissionRepairRunnerTest {

    @Test
    void runAddsMissingTenantDeletePermissionsForBuiltinAdmin() throws Exception {
        SystemRoleMapper roleMapper = Mockito.mock(SystemRoleMapper.class);
        SystemUserMapper userMapper = Mockito.mock(SystemUserMapper.class);
        SystemRoleDO role = new SystemRoleDO();
        role.setId(1L);
        role.setTenantId(1L);
        role.setRoleCode("ROLE_SUPER_ADMIN");
        role.setPermissionCodes("tenant:create,tenant:update");
        SystemUserDO user = new SystemUserDO();
        user.setId(1L);
        user.setTenantId(1L);
        user.setUsername("admin");
        user.setPermissionCodes("tenant:create,tenant:update");
        when(roleMapper.selectOne(any())).thenReturn(role);
        when(userMapper.selectOne(any())).thenReturn(user);

        BuiltinPermissionRepairRunner runner = new BuiltinPermissionRepairRunner(roleMapper, userMapper);
        runner.run(null);

        ArgumentCaptor<SystemRoleDO> roleCaptor = ArgumentCaptor.forClass(SystemRoleDO.class);
        ArgumentCaptor<SystemUserDO> userCaptor = ArgumentCaptor.forClass(SystemUserDO.class);
        Mockito.verify(roleMapper).updateById(roleCaptor.capture());
        Mockito.verify(userMapper).updateById(userCaptor.capture());
        assertThat(roleCaptor.getValue().getPermissionCodes())
            .isEqualTo("tenant:create,tenant:update,tenant:delete,tenant:package:delete");
        assertThat(userCaptor.getValue().getPermissionCodes())
            .isEqualTo("tenant:create,tenant:update,tenant:delete,tenant:package:delete");
    }

    @Test
    void runSkipsUpdatesWhenBuiltinAdminAlreadyHasPermissions() throws Exception {
        SystemRoleMapper roleMapper = Mockito.mock(SystemRoleMapper.class);
        SystemUserMapper userMapper = Mockito.mock(SystemUserMapper.class);
        SystemRoleDO role = new SystemRoleDO();
        role.setId(1L);
        role.setTenantId(1L);
        role.setRoleCode("ROLE_SUPER_ADMIN");
        role.setPermissionCodes("tenant:create,tenant:update,tenant:delete,tenant:package:delete");
        SystemUserDO user = new SystemUserDO();
        user.setId(1L);
        user.setTenantId(1L);
        user.setUsername("admin");
        user.setPermissionCodes("tenant:create,tenant:update,tenant:delete,tenant:package:delete");
        when(roleMapper.selectOne(any())).thenReturn(role);
        when(userMapper.selectOne(any())).thenReturn(user);

        BuiltinPermissionRepairRunner runner = new BuiltinPermissionRepairRunner(roleMapper, userMapper);
        runner.run(null);

        Mockito.verify(roleMapper, Mockito.never()).updateById(any(SystemRoleDO.class));
        Mockito.verify(userMapper, Mockito.never()).updateById(any(SystemUserDO.class));
    }
}
