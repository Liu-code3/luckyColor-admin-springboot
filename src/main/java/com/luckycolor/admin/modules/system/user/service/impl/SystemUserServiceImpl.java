package com.luckycolor.admin.modules.system.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.modules.iam.auth.config.LocalAuthProperties;
import com.luckycolor.admin.modules.system.user.dataobject.SystemUserDO;
import com.luckycolor.admin.modules.system.user.mapper.SystemUserMapper;
import com.luckycolor.admin.modules.system.user.service.SystemUserService;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserPageQuery;
import com.luckycolor.admin.modules.system.user.web.response.SystemUserDetailResponse;
import com.luckycolor.admin.modules.system.user.web.response.SystemUserExportPreviewResponse;
import com.luckycolor.admin.modules.system.user.web.response.SystemUserPageResponse;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@ConditionalOnBean(SystemUserMapper.class)
public class SystemUserServiceImpl implements SystemUserService {

    private final SystemUserMapper systemUserMapper;
    private final DataScopeConditionBuilder dataScopeConditionBuilder;
    private final LocalAuthProperties localAuthProperties;

    public SystemUserServiceImpl(
        SystemUserMapper systemUserMapper,
        DataScopeConditionBuilder dataScopeConditionBuilder,
        LocalAuthProperties localAuthProperties
    ) {
        this.systemUserMapper = systemUserMapper;
        this.dataScopeConditionBuilder = dataScopeConditionBuilder;
        this.localAuthProperties = localAuthProperties;
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

    private List<String> safeList(List<String> values) {
        return values == null ? List.of() : values;
    }
}
