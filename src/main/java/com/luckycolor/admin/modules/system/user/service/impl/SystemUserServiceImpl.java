package com.luckycolor.admin.modules.system.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.modules.iam.auth.config.LocalAuthProperties;
import com.luckycolor.admin.modules.system.user.dataobject.SystemUserDO;
import com.luckycolor.admin.modules.system.user.mapper.SystemUserMapper;
import com.luckycolor.admin.modules.system.user.service.SystemUserService;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserPageQuery;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserSaveRequest;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserStatusRequest;
import com.luckycolor.admin.modules.system.user.web.response.SystemUserDetailResponse;
import com.luckycolor.admin.modules.system.user.web.response.SystemUserExportPreviewResponse;
import com.luckycolor.admin.modules.system.user.web.response.SystemUserPageResponse;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@ConditionalOnBean(SystemUserMapper.class)
public class SystemUserServiceImpl implements SystemUserService {

    private final SystemUserMapper systemUserMapper;
    private final DataScopeConditionBuilder dataScopeConditionBuilder;
    private final LocalAuthProperties localAuthProperties;
    private final PasswordEncoder passwordEncoder;

    public SystemUserServiceImpl(
        SystemUserMapper systemUserMapper,
        DataScopeConditionBuilder dataScopeConditionBuilder,
        LocalAuthProperties localAuthProperties,
        PasswordEncoder passwordEncoder
    ) {
        this.systemUserMapper = systemUserMapper;
        this.dataScopeConditionBuilder = dataScopeConditionBuilder;
        this.localAuthProperties = localAuthProperties;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PageResult<SystemUserPageResponse> pageUsers(SystemUserPageQuery query) {
        PageResult<SystemUserDO> pageResult = systemUserMapper.selectPageResult(query, buildQueryWrapper(query));
        return PageResult.of(pageResult.getList().stream().map(this::toPageResponse).toList(), pageResult.getTotal());
    }

    @Override
    public SystemUserDetailResponse getUser(Long id) {
        return toDetailResponse(getRequiredUser(id));
    }

    @Override
    public List<String> listRoleOptions() {
        Set<String> roleCodes = new LinkedHashSet<>();
        systemUserMapper.selectList(new LambdaQueryWrapper<SystemUserDO>())
            .forEach(user -> roleCodes.addAll(splitCodes(user.getRoleCodes())));
        localAuthProperties.getLocalUsers().forEach(user -> roleCodes.addAll(safeList(user.getRoles())));
        return roleCodes.stream().toList();
    }

    @Override
    public List<SystemUserExportPreviewResponse> listUsersForExportPreview(SystemUserPageQuery query) {
        LambdaQueryWrapper<SystemUserDO> queryWrapper = buildQueryWrapper(query);
        long pageSize = Math.max(query.resolvePageSize(), 200L);
        queryWrapper.last("limit " + Math.min(pageSize, 500L));
        return systemUserMapper.selectList(queryWrapper).stream()
            .map(this::toExportPreviewResponse)
            .toList();
    }

    @Override
    public Long createUser(SystemUserSaveRequest request) {
        if (!StringUtils.hasText(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }
        ensureUsernameUnique(null, request.getUsername());
        SystemUserDO user = new SystemUserDO();
        fillUser(user, request, true);
        systemUserMapper.insert(user);
        return user.getId();
    }

    @Override
    public void updateUser(Long id, SystemUserSaveRequest request) {
        SystemUserDO user = getRequiredUser(id);
        ensureUsernameUnique(id, request.getUsername());
        fillUser(user, request, false);
        systemUserMapper.updateById(user);
    }

    @Override
    public void updateUserStatus(Long id, SystemUserStatusRequest request) {
        SystemUserDO user = getRequiredUser(id);
        user.setStatus(request.getStatus());
        systemUserMapper.updateById(user);
    }

    @Override
    public void deleteUser(Long id) {
        getRequiredUser(id);
        systemUserMapper.deleteById(id);
    }

    private LambdaQueryWrapper<SystemUserDO> buildQueryWrapper(SystemUserPageQuery query) {
        LambdaQueryWrapper<SystemUserDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(query.getUsername()), SystemUserDO::getUsername, query.getUsername());
        queryWrapper.like(StringUtils.hasText(query.getNickname()), SystemUserDO::getNickname, query.getNickname());
        queryWrapper.like(StringUtils.hasText(query.getMobile()), SystemUserDO::getMobile, query.getMobile());
        queryWrapper.eq(query.getStatus() != null, SystemUserDO::getStatus, query.getStatus());
        dataScopeConditionBuilder.applyCurrentScope(
            queryWrapper,
            SystemUserDO::getTenantId,
            SystemUserDO::getDepartmentId
        );
        queryWrapper.orderByDesc(SystemUserDO::getCreateTime);
        return queryWrapper;
    }

    private SystemUserDO getRequiredUser(Long id) {
        SystemUserDO user = systemUserMapper.selectById(id);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return user;
    }

    private void ensureUsernameUnique(Long currentId, String username) {
        if (!StringUtils.hasText(username)) {
            return;
        }
        LambdaQueryWrapper<SystemUserDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemUserDO::getUsername, username.trim());
        List<SystemUserDO> existingUsers = systemUserMapper.selectList(queryWrapper);
        boolean duplicated = existingUsers.stream()
            .anyMatch(item -> currentId == null || !currentId.equals(item.getId()));
        if (duplicated) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
    }

    private void fillUser(SystemUserDO user, SystemUserSaveRequest request, boolean creating) {
        user.setUsername(request.getUsername());
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setMobile(request.getMobile());
        user.setRoleCodes(joinCodes(request.getRoleCodes()));
        user.setPermissionCodes(joinCodes(request.getPermissionCodes()));
        user.setDataScope(request.getDataScope());
        user.setDepartmentId(request.getDepartmentId());
        user.setDepartmentIds(joinLongCodes(request.getDepartmentIds()));
        user.setScopeTenantIds(joinLongCodes(request.getScopeTenantIds()));
        user.setStatus(request.getStatus());
        user.setRemark(request.getRemark());
        if (creating) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
    }

    private SystemUserPageResponse toPageResponse(SystemUserDO user) {
        return new SystemUserPageResponse(
            user.getId(),
            user.getTenantId(),
            user.getUsername(),
            user.getNickname(),
            user.getEmail(),
            user.getMobile(),
            user.getDepartmentId(),
            splitCodes(user.getRoleCodes()),
            user.getStatus()
        );
    }

    private SystemUserDetailResponse toDetailResponse(SystemUserDO user) {
        return new SystemUserDetailResponse(
            user.getId(),
            user.getTenantId(),
            user.getUsername(),
            user.getNickname(),
            user.getEmail(),
            user.getMobile(),
            user.getDepartmentId(),
            splitCodes(user.getRoleCodes()),
            splitCodes(user.getPermissionCodes()),
            user.getDataScope(),
            splitLongCodes(user.getDepartmentIds()),
            splitLongCodes(user.getScopeTenantIds()),
            user.getStatus(),
            user.getRemark()
        );
    }

    private SystemUserExportPreviewResponse toExportPreviewResponse(SystemUserDO user) {
        return new SystemUserExportPreviewResponse(
            user.getId(),
            user.getUsername(),
            user.getNickname(),
            user.getEmail(),
            user.getMobile(),
            splitCodes(user.getRoleCodes()),
            user.getStatus()
        );
    }

    private List<String> splitCodes(String values) {
        if (!StringUtils.hasText(values)) {
            return List.of();
        }
        return java.util.Arrays.stream(values.split(","))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .toList();
    }

    private List<Long> splitLongCodes(String values) {
        if (!StringUtils.hasText(values)) {
            return List.of();
        }
        return java.util.Arrays.stream(values.split(","))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .map(Long::valueOf)
            .toList();
    }

    private String joinCodes(List<String> values) {
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

    private String joinLongCodes(List<Long> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.stream()
            .map(String::valueOf)
            .distinct()
            .reduce((left, right) -> left + "," + right)
            .orElse(null);
    }

    private List<String> safeList(List<String> values) {
        return values == null ? List.of() : values;
    }
}
