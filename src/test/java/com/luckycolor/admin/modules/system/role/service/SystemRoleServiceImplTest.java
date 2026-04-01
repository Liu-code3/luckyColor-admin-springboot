package com.luckycolor.admin.modules.system.role.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.datascope.CurrentDataScopeResolver;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.modules.system.role.dataobject.SystemRoleDO;
import com.luckycolor.admin.modules.system.role.mapper.SystemRoleMapper;
import com.luckycolor.admin.modules.system.role.service.impl.SystemRoleServiceImpl;
import com.luckycolor.admin.modules.system.role.web.request.SystemRolePageQuery;
import com.luckycolor.admin.modules.system.role.web.request.SystemRoleSaveRequest;
import com.luckycolor.admin.modules.system.role.web.request.SystemRoleStatusRequest;
import com.luckycolor.admin.modules.system.role.web.response.SystemRoleDetailResponse;
import com.luckycolor.admin.modules.system.role.web.response.SystemRolePageResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

class SystemRoleServiceImplTest {

    @Test
    void shouldReturnRolePage() {
        SystemRoleMapper mapper = Mockito.mock(SystemRoleMapper.class);
        when(mapper.selectPageResult(any(), any())).thenReturn(PageResult.of(List.of(role()), 1L));
        SystemRoleService service = new SystemRoleServiceImpl(mapper, noScopeBuilder());

        PageResult<SystemRolePageResponse> result = service.pageRoles(new SystemRolePageQuery());

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getList()).extracting(SystemRolePageResponse::roleCode).containsExactly("ROLE_ADMIN");
    }

    @Test
    void shouldReturnRoleDetail() {
        SystemRoleMapper mapper = Mockito.mock(SystemRoleMapper.class);
        when(mapper.selectById(1L)).thenReturn(role());
        SystemRoleService service = new SystemRoleServiceImpl(mapper, noScopeBuilder());

        SystemRoleDetailResponse result = service.getRole(1L);

        assertThat(result.roleName()).isEqualTo("Admin");
    }

    @Test
    void shouldCreateRole() {
        SystemRoleMapper mapper = Mockito.mock(SystemRoleMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of());
        SystemRoleService service = new SystemRoleServiceImpl(mapper, noScopeBuilder());

        Long result = service.createRole(saveRequest());

        assertThat(result).isNull();
        verify(mapper).insert(any(SystemRoleDO.class));
    }

    @Test
    void shouldUpdateRoleStatus() {
        SystemRoleMapper mapper = Mockito.mock(SystemRoleMapper.class);
        SystemRoleDO role = role();
        when(mapper.selectById(1L)).thenReturn(role);
        SystemRoleService service = new SystemRoleServiceImpl(mapper, noScopeBuilder());
        SystemRoleStatusRequest request = new SystemRoleStatusRequest();
        request.setStatus(1);

        service.updateRoleStatus(1L, request);

        assertThat(role.getStatus()).isEqualTo(1);
        verify(mapper).updateById(role);
    }

    @Test
    void shouldThrowWhenRoleNotFound() {
        SystemRoleMapper mapper = Mockito.mock(SystemRoleMapper.class);
        when(mapper.selectById(99L)).thenReturn(null);
        SystemRoleService service = new SystemRoleServiceImpl(mapper, noScopeBuilder());

        assertThatThrownBy(() -> service.getRole(99L))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("404 NOT_FOUND");
    }

    private SystemRoleDO role() {
        SystemRoleDO role = new SystemRoleDO();
        role.setId(1L);
        role.setTenantId(1L);
        role.setRoleCode("ROLE_ADMIN");
        role.setRoleName("Admin");
        role.setSort(1);
        role.setStatus(0);
        role.setRemark("default");
        return role;
    }

    private SystemRoleSaveRequest saveRequest() {
        SystemRoleSaveRequest request = new SystemRoleSaveRequest();
        request.setRoleCode("ROLE_ADMIN");
        request.setRoleName("Admin");
        request.setSort(1);
        request.setStatus(0);
        request.setRemark("default");
        return request;
    }

    private DataScopeConditionBuilder noScopeBuilder() {
        CurrentDataScopeResolver resolver = Mockito.mock(CurrentDataScopeResolver.class);
        when(resolver.resolveCurrentRule()).thenReturn(java.util.Optional.empty());
        return new DataScopeConditionBuilder(resolver);
    }
}
