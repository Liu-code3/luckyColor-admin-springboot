package com.luckycolor.admin.modules.system.department.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.modules.system.department.dataobject.SystemDepartmentDO;
import com.luckycolor.admin.modules.system.department.mapper.SystemDepartmentMapper;
import com.luckycolor.admin.modules.system.department.service.SystemDepartmentService;
import com.luckycolor.admin.modules.system.department.web.request.SystemDepartmentSaveRequest;
import com.luckycolor.admin.modules.system.department.web.request.SystemDepartmentStatusRequest;
import com.luckycolor.admin.modules.system.department.web.request.SystemDepartmentTreeQuery;
import com.luckycolor.admin.modules.system.department.web.response.SystemDepartmentDetailResponse;
import com.luckycolor.admin.modules.system.department.web.response.SystemDepartmentTreeResponse;
import com.luckycolor.admin.modules.system.user.dataobject.SystemUserDO;
import com.luckycolor.admin.modules.system.user.mapper.SystemUserMapper;
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
    private final SystemUserMapper systemUserMapper;

    public SystemDepartmentServiceImpl(
        SystemDepartmentMapper systemDepartmentMapper,
        DataScopeConditionBuilder dataScopeConditionBuilder,
        SystemUserMapper systemUserMapper
    ) {
        this.systemDepartmentMapper = systemDepartmentMapper;
        this.dataScopeConditionBuilder = dataScopeConditionBuilder;
        this.systemUserMapper = systemUserMapper;
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

    @Override
    public Long createDepartment(SystemDepartmentSaveRequest request) {
        validateParentExists(request.getParentId());
        ensureDepartmentNameUnique(null, request.getParentId(), request.getDepartmentName());
        SystemDepartmentDO department = new SystemDepartmentDO();
        fillDepartment(department, request);
        systemDepartmentMapper.insert(department);
        return department.getId();
    }

    @Override
    public void updateDepartment(Long id, SystemDepartmentSaveRequest request) {
        SystemDepartmentDO department = getRequiredDepartment(id);
        validateParentExists(request.getParentId());
        ensureParentValid(id, request.getParentId());
        ensureDepartmentNameUnique(id, request.getParentId(), request.getDepartmentName());
        fillDepartment(department, request);
        systemDepartmentMapper.updateById(department);
    }

    @Override
    public void updateDepartmentStatus(Long id, SystemDepartmentStatusRequest request) {
        SystemDepartmentDO department = getRequiredDepartment(id);
        department.setStatus(request.getStatus());
        systemDepartmentMapper.updateById(department);
    }

    @Override
    public void deleteDepartment(Long id) {
        getRequiredDepartment(id);
        LambdaQueryWrapper<SystemDepartmentDO> childQuery = new LambdaQueryWrapper<>();
        childQuery.eq(SystemDepartmentDO::getParentId, id);
        Long childCount = systemDepartmentMapper.selectCount(childQuery);
        if (childCount != null && childCount > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department has children and cannot be deleted");
        }

        LambdaQueryWrapper<SystemUserDO> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(SystemUserDO::getDepartmentId, id);
        Long userCount = systemUserMapper.selectCount(userQuery);
        if (userCount != null && userCount > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department has users and cannot be deleted");
        }
        systemDepartmentMapper.deleteById(id);
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

    private void validateParentExists(Long parentId) {
        if (normalizeParentId(parentId).equals(0L)) {
            return;
        }
        if (systemDepartmentMapper.selectById(parentId) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent department not found");
        }
    }

    private void ensureParentValid(Long currentId, Long parentId) {
        Long normalizedParentId = normalizeParentId(parentId);
        if (currentId.equals(normalizedParentId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent department cannot be self");
        }
        Long currentParentId = normalizedParentId;
        while (!currentParentId.equals(0L)) {
            if (currentId.equals(currentParentId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent department cannot be child node");
            }
            SystemDepartmentDO parentDepartment = systemDepartmentMapper.selectById(currentParentId);
            if (parentDepartment == null) {
                break;
            }
            currentParentId = normalizeParentId(parentDepartment.getParentId());
        }
    }

    private void ensureDepartmentNameUnique(Long currentId, Long parentId, String departmentName) {
        if (!StringUtils.hasText(departmentName)) {
            return;
        }
        LambdaQueryWrapper<SystemDepartmentDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemDepartmentDO::getParentId, normalizeParentId(parentId))
            .eq(SystemDepartmentDO::getDepartmentName, departmentName.trim());
        List<SystemDepartmentDO> existingDepartments = systemDepartmentMapper.selectList(queryWrapper);
        boolean duplicated = existingDepartments.stream()
            .anyMatch(item -> currentId == null || !currentId.equals(item.getId()));
        if (duplicated) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Department name already exists");
        }
    }

    private void fillDepartment(SystemDepartmentDO department, SystemDepartmentSaveRequest request) {
        department.setParentId(normalizeParentId(request.getParentId()));
        department.setDepartmentName(request.getDepartmentName());
        department.setLeader(request.getLeader());
        department.setPhone(request.getPhone());
        department.setEmail(request.getEmail());
        department.setSort(request.getSort());
        department.setStatus(request.getStatus());
        department.setRemark(request.getRemark());
    }

    private Long normalizeParentId(Long parentId) {
        return parentId == null ? 0L : parentId;
    }
}
