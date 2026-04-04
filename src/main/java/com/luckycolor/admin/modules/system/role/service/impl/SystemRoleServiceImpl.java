package com.luckycolor.admin.modules.system.role.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.modules.system.role.dataobject.SystemRoleDO;
import com.luckycolor.admin.modules.system.role.mapper.SystemRoleMapper;
import com.luckycolor.admin.modules.system.role.service.SystemRoleService;
import com.luckycolor.admin.modules.system.role.web.request.SystemRoleAuthorityRequest;
import com.luckycolor.admin.modules.system.role.web.request.SystemRolePageQuery;
import com.luckycolor.admin.modules.system.role.web.request.SystemRoleSaveRequest;
import com.luckycolor.admin.modules.system.role.web.request.SystemRoleStatusRequest;
import com.luckycolor.admin.modules.system.role.web.response.SystemRoleAuthorityResponse;
import com.luckycolor.admin.modules.system.role.web.response.SystemRoleDetailResponse;
import com.luckycolor.admin.modules.system.role.web.response.SystemRolePageResponse;
import java.util.List;
import com.luckycolor.admin.common.config.ConditionalOnPersistenceEnabled;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@ConditionalOnPersistenceEnabled
public class SystemRoleServiceImpl implements SystemRoleService {

    private final SystemRoleMapper systemRoleMapper;
    private final DataScopeConditionBuilder dataScopeConditionBuilder;

    public SystemRoleServiceImpl(
        SystemRoleMapper systemRoleMapper,
        DataScopeConditionBuilder dataScopeConditionBuilder
    ) {
        this.systemRoleMapper = systemRoleMapper;
        this.dataScopeConditionBuilder = dataScopeConditionBuilder;
    }

    @Override
    public PageResult<SystemRolePageResponse> pageRoles(SystemRolePageQuery query) {
        PageResult<SystemRoleDO> pageResult = systemRoleMapper.selectPageResult(query, buildQueryWrapper(query));
        return PageResult.of(pageResult.getList().stream().map(this::toPageResponse).toList(), pageResult.getTotal());
    }

    @Override
    public SystemRoleDetailResponse getRole(Long id) {
        return toDetailResponse(getRequiredRole(id));
    }

    @Override
    public Long createRole(SystemRoleSaveRequest request) {
        ensureRoleCodeUnique(null, request.getRoleCode());
        SystemRoleDO role = new SystemRoleDO();
        fillRole(role, request);
        systemRoleMapper.insert(role);
        return role.getId();
    }

    @Override
    public void updateRole(Long id, SystemRoleSaveRequest request) {
        SystemRoleDO role = getRequiredRole(id);
        ensureRoleCodeUnique(id, request.getRoleCode());
        fillRole(role, request);
        systemRoleMapper.updateById(role);
    }

    @Override
    public void updateRoleStatus(Long id, SystemRoleStatusRequest request) {
        SystemRoleDO role = getRequiredRole(id);
        role.setStatus(request.getStatus());
        systemRoleMapper.updateById(role);
    }

    @Override
    public SystemRoleAuthorityResponse getRoleAuthority(Long id) {
        return toAuthorityResponse(getRequiredRole(id));
    }

    @Override
    public void updateRoleAuthority(Long id, SystemRoleAuthorityRequest request) {
        SystemRoleDO role = getRequiredRole(id);
        role.setMenuIds(joinLongValues(request.getMenuIds()));
        role.setPermissionCodes(joinStringValues(request.getPermissionCodes()));
        role.setDataScope(request.getDataScope());
        role.setDepartmentId(request.getDepartmentId());
        role.setDepartmentIds(joinLongValues(request.getDepartmentIds()));
        systemRoleMapper.updateById(role);
    }

    private LambdaQueryWrapper<SystemRoleDO> buildQueryWrapper(SystemRolePageQuery query) {
        LambdaQueryWrapper<SystemRoleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(query.getRoleCode()), SystemRoleDO::getRoleCode, query.getRoleCode());
        queryWrapper.like(StringUtils.hasText(query.getRoleName()), SystemRoleDO::getRoleName, query.getRoleName());
        queryWrapper.eq(query.getStatus() != null, SystemRoleDO::getStatus, query.getStatus());
        dataScopeConditionBuilder.applyCurrentScope(queryWrapper, SystemRoleDO::getTenantId, null);
        queryWrapper.orderByAsc(SystemRoleDO::getSort)
            .orderByDesc(SystemRoleDO::getCreateTime);
        return queryWrapper;
    }

    private SystemRoleDO getRequiredRole(Long id) {
        SystemRoleDO role = systemRoleMapper.selectById(id);
        if (role == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found");
        }
        return role;
    }

    private void ensureRoleCodeUnique(Long currentId, String roleCode) {
        if (!StringUtils.hasText(roleCode)) {
            return;
        }
        LambdaQueryWrapper<SystemRoleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemRoleDO::getRoleCode, roleCode.trim());
        List<SystemRoleDO> roles = systemRoleMapper.selectList(queryWrapper);
        boolean duplicated = roles.stream().anyMatch(role -> currentId == null || !currentId.equals(role.getId()));
        if (duplicated) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Role code already exists");
        }
    }

    private void fillRole(SystemRoleDO role, SystemRoleSaveRequest request) {
        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());
        role.setSort(request.getSort());
        role.setStatus(request.getStatus());
        role.setRemark(request.getRemark());
    }

    private SystemRolePageResponse toPageResponse(SystemRoleDO role) {
        return new SystemRolePageResponse(
            role.getId(),
            role.getTenantId(),
            role.getRoleCode(),
            role.getRoleName(),
            role.getSort(),
            role.getStatus()
        );
    }

    private SystemRoleDetailResponse toDetailResponse(SystemRoleDO role) {
        return new SystemRoleDetailResponse(
            role.getId(),
            role.getTenantId(),
            role.getRoleCode(),
            role.getRoleName(),
            role.getSort(),
            role.getStatus(),
            role.getRemark()
        );
    }

    private SystemRoleAuthorityResponse toAuthorityResponse(SystemRoleDO role) {
        return new SystemRoleAuthorityResponse(
            role.getId(),
            role.getTenantId(),
            role.getRoleCode(),
            role.getRoleName(),
            splitLongValues(role.getMenuIds()),
            splitStringValues(role.getPermissionCodes()),
            role.getDataScope(),
            role.getDepartmentId(),
            splitLongValues(role.getDepartmentIds())
        );
    }

    private String joinLongValues(List<Long> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.stream()
            .map(String::valueOf)
            .distinct()
            .reduce((left, right) -> left + "," + right)
            .orElse(null);
    }

    private String joinStringValues(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.stream()
            .filter(StringUtils::hasText)
            .map(String::trim)
            .distinct()
            .reduce((left, right) -> left + "," + right)
            .orElse(null);
    }

    private List<Long> splitLongValues(String values) {
        if (!StringUtils.hasText(values)) {
            return List.of();
        }
        return java.util.Arrays.stream(values.split(","))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .map(Long::valueOf)
            .toList();
    }

    private List<String> splitStringValues(String values) {
        if (!StringUtils.hasText(values)) {
            return List.of();
        }
        return java.util.Arrays.stream(values.split(","))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .toList();
    }
}
