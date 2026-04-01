package com.luckycolor.admin.modules.system.department.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.luckycolor.admin.infrastructure.security.datascope.CurrentDataScopeResolver;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.modules.system.department.dataobject.SystemDepartmentDO;
import com.luckycolor.admin.modules.system.department.mapper.SystemDepartmentMapper;
import com.luckycolor.admin.modules.system.department.service.impl.SystemDepartmentServiceImpl;
import com.luckycolor.admin.modules.system.department.web.request.SystemDepartmentSaveRequest;
import com.luckycolor.admin.modules.system.department.web.request.SystemDepartmentStatusRequest;
import com.luckycolor.admin.modules.system.department.web.request.SystemDepartmentTreeQuery;
import com.luckycolor.admin.modules.system.user.mapper.SystemUserMapper;
import com.luckycolor.admin.modules.system.department.web.response.SystemDepartmentDetailResponse;
import com.luckycolor.admin.modules.system.department.web.response.SystemDepartmentTreeResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

class SystemDepartmentServiceImplTest {

    @Test
    void shouldReturnDepartmentTreeSortedByParentAndSort() {
        SystemDepartmentMapper mapper = Mockito.mock(SystemDepartmentMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of(
            department(2L, 1L, "Operations", 2),
            department(1L, 0L, "Headquarters", 1),
            department(3L, 1L, "Engineering", 1)
        ));
        SystemDepartmentService service = new SystemDepartmentServiceImpl(mapper, noScopeBuilder(), userMapper());

        List<SystemDepartmentTreeResponse> result = service.listDepartmentTree(new SystemDepartmentTreeQuery());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).departmentName()).isEqualTo("Headquarters");
        assertThat(result.get(0).children()).extracting(SystemDepartmentTreeResponse::departmentName)
            .containsExactly("Engineering", "Operations");
    }

    @Test
    void shouldReturnDepartmentDetail() {
        SystemDepartmentMapper mapper = Mockito.mock(SystemDepartmentMapper.class);
        SystemDepartmentDO department = department(1L, 0L, "Headquarters", 1);
        department.setRemark("default");
        when(mapper.selectById(1L)).thenReturn(department);
        SystemDepartmentService service = new SystemDepartmentServiceImpl(mapper, noScopeBuilder(), userMapper());

        SystemDepartmentDetailResponse result = service.getDepartment(1L);

        assertThat(result.departmentName()).isEqualTo("Headquarters");
        assertThat(result.remark()).isEqualTo("default");
    }

    @Test
    void shouldThrowWhenDepartmentNotFound() {
        SystemDepartmentMapper mapper = Mockito.mock(SystemDepartmentMapper.class);
        when(mapper.selectById(99L)).thenReturn(null);
        SystemDepartmentService service = new SystemDepartmentServiceImpl(mapper, noScopeBuilder(), userMapper());

        assertThatThrownBy(() -> service.getDepartment(99L))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("404 NOT_FOUND");
    }

    @Test
    void shouldCreateDepartment() {
        SystemDepartmentMapper mapper = Mockito.mock(SystemDepartmentMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of());
        SystemDepartmentService service = new SystemDepartmentServiceImpl(mapper, noScopeBuilder(), userMapper());

        Long result = service.createDepartment(saveRequest());

        assertThat(result).isNull();
        verify(mapper).insert(any(SystemDepartmentDO.class));
    }

    @Test
    void shouldUpdateDepartmentStatus() {
        SystemDepartmentMapper mapper = Mockito.mock(SystemDepartmentMapper.class);
        SystemDepartmentDO department = department(1L, 0L, "Headquarters", 1);
        when(mapper.selectById(1L)).thenReturn(department);
        SystemDepartmentService service = new SystemDepartmentServiceImpl(mapper, noScopeBuilder(), userMapper());
        SystemDepartmentStatusRequest request = new SystemDepartmentStatusRequest();
        request.setStatus(1);

        service.updateDepartmentStatus(1L, request);

        assertThat(department.getStatus()).isEqualTo(1);
        verify(mapper).updateById(department);
    }

    @Test
    void shouldRejectDeleteWhenDepartmentHasChildren() {
        SystemDepartmentMapper mapper = Mockito.mock(SystemDepartmentMapper.class);
        SystemUserMapper userMapper = userMapper();
        when(mapper.selectById(1L)).thenReturn(department(1L, 0L, "Headquarters", 1));
        when(mapper.selectCount(any())).thenReturn(1L);
        SystemDepartmentService service = new SystemDepartmentServiceImpl(mapper, noScopeBuilder(), userMapper);

        assertThatThrownBy(() -> service.deleteDepartment(1L))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST");

        verify(mapper, never()).deleteById(1L);
        verify(userMapper, never()).selectCount(any());
    }

    @Test
    void shouldRejectDeleteWhenDepartmentHasUsers() {
        SystemDepartmentMapper mapper = Mockito.mock(SystemDepartmentMapper.class);
        SystemUserMapper userMapper = userMapper();
        when(mapper.selectById(1L)).thenReturn(department(1L, 0L, "Headquarters", 1));
        when(mapper.selectCount(any())).thenReturn(0L);
        when(userMapper.selectCount(any())).thenReturn(1L);
        SystemDepartmentService service = new SystemDepartmentServiceImpl(mapper, noScopeBuilder(), userMapper);

        assertThatThrownBy(() -> service.deleteDepartment(1L))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST");

        verify(mapper, never()).deleteById(1L);
    }

    @Test
    void shouldDeleteDepartmentWithoutChildrenAndUsers() {
        SystemDepartmentMapper mapper = Mockito.mock(SystemDepartmentMapper.class);
        SystemUserMapper userMapper = userMapper();
        when(mapper.selectById(1L)).thenReturn(department(1L, 0L, "Headquarters", 1));
        when(mapper.selectCount(any())).thenReturn(0L);
        when(userMapper.selectCount(any())).thenReturn(0L);
        SystemDepartmentService service = new SystemDepartmentServiceImpl(mapper, noScopeBuilder(), userMapper);

        service.deleteDepartment(1L);

        verify(mapper).deleteById(1L);
    }

    private SystemDepartmentDO department(Long id, Long parentId, String departmentName, Integer sort) {
        SystemDepartmentDO department = new SystemDepartmentDO();
        department.setId(id);
        department.setTenantId(1L);
        department.setParentId(parentId);
        department.setDepartmentName(departmentName);
        department.setLeader("leader");
        department.setPhone("13800000000");
        department.setEmail("dept@example.com");
        department.setSort(sort);
        department.setStatus(0);
        return department;
    }

    private DataScopeConditionBuilder noScopeBuilder() {
        CurrentDataScopeResolver resolver = Mockito.mock(CurrentDataScopeResolver.class);
        when(resolver.resolveCurrentRule()).thenReturn(java.util.Optional.empty());
        return new DataScopeConditionBuilder(resolver);
    }

    private SystemUserMapper userMapper() {
        return Mockito.mock(SystemUserMapper.class);
    }

    private SystemDepartmentSaveRequest saveRequest() {
        SystemDepartmentSaveRequest request = new SystemDepartmentSaveRequest();
        request.setParentId(0L);
        request.setDepartmentName("Engineering");
        request.setLeader("Bob");
        request.setPhone("13800000001");
        request.setEmail("eng@example.com");
        request.setSort(1);
        request.setStatus(0);
        request.setRemark("default");
        return request;
    }
}
