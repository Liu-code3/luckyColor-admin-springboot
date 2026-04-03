package com.luckycolor.admin.modules.iam.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.common.config.ConditionalOnPersistenceEnabled;
import com.luckycolor.admin.infrastructure.tenant.annotation.TenantIgnore;
import com.luckycolor.admin.modules.iam.auth.model.AuthUser;
import com.luckycolor.admin.modules.iam.auth.service.AuthUserService;
import com.luckycolor.admin.modules.system.role.dataobject.SystemRoleDO;
import com.luckycolor.admin.modules.system.role.mapper.SystemRoleMapper;
import com.luckycolor.admin.modules.system.user.dataobject.SystemUserDO;
import com.luckycolor.admin.modules.system.user.mapper.SystemUserMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Primary
@TenantIgnore
@ConditionalOnPersistenceEnabled
public class PersistenceAuthUserServiceImpl implements AuthUserService {

    private final SystemUserMapper systemUserMapper;
    private final SystemRoleMapper systemRoleMapper;
    private final LocalAuthUserServiceImpl localAuthUserService;

    public PersistenceAuthUserServiceImpl(
        SystemUserMapper systemUserMapper,
        SystemRoleMapper systemRoleMapper,
        LocalAuthUserServiceImpl localAuthUserService
    ) {
        this.systemUserMapper = systemUserMapper;
        this.systemRoleMapper = systemRoleMapper;
        this.localAuthUserService = localAuthUserService;
    }

    @Override
    public AuthUser findByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        LambdaQueryWrapper<SystemUserDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemUserDO::getUsername, username.trim());
        queryWrapper.last("LIMIT 1");
        SystemUserDO user = systemUserMapper.selectOne(queryWrapper);
        if (user == null) {
            return localAuthUserService.findByUsername(username);
        }
        return toAuthUser(user);
    }

    @Override
    public AuthUser getByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        SystemUserDO user = systemUserMapper.selectById(userId);
        if (user == null) {
            return localAuthUserService.getByUserId(userId);
        }
        return toAuthUser(user);
    }

    private AuthUser toAuthUser(SystemUserDO user) {
        List<SystemRoleDO> activeRoles = resolveActiveRoles(user);
        return new AuthUser(
            user.getId(),
            user.getUsername(),
            user.getPassword(),
            user.getTenantId(),
            user.getNickname(),
            user.getStatus(),
            activeRoles.stream().map(SystemRoleDO::getRoleCode).toList(),
            resolvePermissions(user, activeRoles),
            resolveDataScope(user, activeRoles),
            user.getDepartmentId(),
            splitLongCodes(user.getDepartmentIds()),
            splitLongCodes(user.getScopeTenantIds())
        );
    }

    private List<SystemRoleDO> resolveActiveRoles(SystemUserDO user) {
        List<String> assignedRoleCodes = splitCodes(user.getRoleCodes());
        if (assignedRoleCodes.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<SystemRoleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemRoleDO::getTenantId, user.getTenantId());
        queryWrapper.in(SystemRoleDO::getRoleCode, assignedRoleCodes);
        List<SystemRoleDO> roles = systemRoleMapper.selectList(queryWrapper);
        List<SystemRoleDO> activeRoles = roles.stream()
            .filter(role -> role.getStatus() == null || role.getStatus() == 0)
            .sorted(Comparator.comparing(SystemRoleDO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(SystemRoleDO::getRoleCode, Comparator.nullsLast(String::compareTo)))
            .toList();
        if (activeRoles.isEmpty()) {
            return List.of();
        }
        List<SystemRoleDO> orderedRoles = new ArrayList<>();
        for (String roleCode : assignedRoleCodes) {
            activeRoles.stream()
                .filter(role -> roleCode.equals(role.getRoleCode()))
                .findFirst()
                .ifPresent(orderedRoles::add);
        }
        return orderedRoles.isEmpty() ? activeRoles : orderedRoles;
    }

    private List<String> resolvePermissions(SystemUserDO user, List<SystemRoleDO> activeRoles) {
        Set<String> values = new LinkedHashSet<>();
        for (SystemRoleDO activeRole : activeRoles) {
            values.addAll(splitCodes(activeRole.getPermissionCodes()));
        }
        values.addAll(splitCodes(user.getPermissionCodes()));
        return List.copyOf(values);
    }

    private String resolveDataScope(SystemUserDO user, List<SystemRoleDO> activeRoles) {
        if (StringUtils.hasText(user.getDataScope())) {
            return user.getDataScope().trim();
        }
        for (SystemRoleDO activeRole : activeRoles) {
            if (StringUtils.hasText(activeRole.getDataScope())) {
                return activeRole.getDataScope().trim();
            }
        }
        return "TENANT";
    }

    private List<String> splitCodes(String values) {
        if (!StringUtils.hasText(values)) {
            return List.of();
        }
        return List.of(values.split(",")).stream()
            .map(String::trim)
            .filter(StringUtils::hasText)
            .toList();
    }

    private List<Long> splitLongCodes(String values) {
        if (!StringUtils.hasText(values)) {
            return List.of();
        }
        List<Long> result = new ArrayList<>();
        for (String token : values.split(",")) {
            String trimmed = token == null ? null : token.trim();
            if (!StringUtils.hasText(trimmed)) {
                continue;
            }
            result.add(Long.valueOf(trimmed));
        }
        return Collections.unmodifiableList(result);
    }
}
