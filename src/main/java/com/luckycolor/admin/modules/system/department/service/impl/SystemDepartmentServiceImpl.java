package com.luckycolor.admin.modules.system.department.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.modules.system.department.dataobject.SystemDepartmentDO;
import com.luckycolor.admin.modules.system.department.mapper.SystemDepartmentMapper;
import com.luckycolor.admin.modules.system.department.service.SystemDepartmentService;
import com.luckycolor.admin.modules.system.department.web.request.SystemDepartmentTreeQuery;
import com.luckycolor.admin.modules.system.department.web.response.SystemDepartmentDetailResponse;
import com.luckycolor.admin.modules.system.department.web.response.SystemDepartmentTreeResponse;
import java.util.Comparator;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@ConditionalOnBean(SystemDepartmentMapper.class)
public class SystemDepartmentServiceImpl implements SystemDepartmentService {

    private final SystemDepartmentMapper systemDepartmentMapper;
    private final DataScopeConditionBuilder dataScopeConditionBuilder;

    public SystemDepartmentServiceImpl(
        SystemDepartmentMapper systemDepartmentMapper,
        DataScopeConditionBuilder dataScopeConditionBuilder
    ) {
        this.systemDepartmentMapper = systemDepartmentMapper;
        this.dataScopeConditionBuilder = dataScopeConditionBuilder;
    }

    @Override
    public List<SystemDepartmentTreeResponse> listDepartmentTree(SystemDepartmentTreeQuery query) {
        List<SystemDepartmentDO> departments = systemDepartmentMapper.selectList(buildQueryWrapper(query));
        return buildTree(departments, 0L);
    }

    @Override
    public SystemDepartmentDetailResponse getDepartment(Long id) {
        return toDetailResponse(getRequiredDepartment(id));
    }

    private LambdaQueryWrapper<SystemDepartmentDO> buildQueryWrapper(SystemDepartmentTreeQuery query) {
        LambdaQueryWrapper<SystemDepartmentDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(
            StringUtils.hasText(query.getDepartmentName()),
            SystemDepartmentDO::getDepartmentName,
            query.getDepartmentName()
        );
        queryWrapper.eq(query.getStatus() != null, SystemDepartmentDO::getStatus, query.getStatus());
        dataScopeConditionBuilder.applyCurrentScope(
            queryWrapper,
            SystemDepartmentDO::getTenantId,
            SystemDepartmentDO::getId
        );
        queryWrapper.orderByAsc(SystemDepartmentDO::getParentId)
            .orderByAsc(SystemDepartmentDO::getSort)
            .orderByAsc(SystemDepartmentDO::getId);
        return queryWrapper;
    }

    private List<SystemDepartmentTreeResponse> buildTree(List<SystemDepartmentDO> departments, Long parentId) {
        return departments.stream()
            .filter(department -> normalizeParentId(department.getParentId()).equals(normalizeParentId(parentId)))
            .sorted(Comparator.comparing(SystemDepartmentDO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(SystemDepartmentDO::getId, Comparator.nullsLast(Long::compareTo)))
            .map(department -> toTreeResponse(department, buildTree(departments, department.getId())))
            .toList();
    }

    private SystemDepartmentTreeResponse toTreeResponse(
        SystemDepartmentDO department,
        List<SystemDepartmentTreeResponse> children
    ) {
        return new SystemDepartmentTreeResponse(
            department.getId(),
            department.getTenantId(),
            normalizeParentId(department.getParentId()),
            department.getDepartmentName(),
            department.getLeader(),
            department.getPhone(),
            department.getEmail(),
            department.getSort(),
            department.getStatus(),
            children
        );
    }

    private SystemDepartmentDetailResponse toDetailResponse(SystemDepartmentDO department) {
        return new SystemDepartmentDetailResponse(
            department.getId(),
            department.getTenantId(),
            normalizeParentId(department.getParentId()),
            department.getDepartmentName(),
            department.getLeader(),
            department.getPhone(),
            department.getEmail(),
            department.getSort(),
            department.getStatus(),
            department.getRemark()
        );
    }

    private SystemDepartmentDO getRequiredDepartment(Long id) {
        SystemDepartmentDO department = systemDepartmentMapper.selectById(id);
        if (department == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found");
        }
        return department;
    }

    private Long normalizeParentId(Long parentId) {
        return parentId == null ? 0L : parentId;
    }
}
