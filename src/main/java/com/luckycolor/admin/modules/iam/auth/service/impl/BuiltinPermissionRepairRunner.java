package com.luckycolor.admin.modules.iam.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.common.config.ConditionalOnPersistenceEnabled;
import com.luckycolor.admin.infrastructure.tenant.annotation.TenantIgnore;
import com.luckycolor.admin.modules.system.role.dataobject.SystemRoleDO;
import com.luckycolor.admin.modules.system.role.mapper.SystemRoleMapper;
import com.luckycolor.admin.modules.system.user.dataobject.SystemUserDO;
import com.luckycolor.admin.modules.system.user.mapper.SystemUserMapper;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
@TenantIgnore
@ConditionalOnPersistenceEnabled
public class BuiltinPermissionRepairRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BuiltinPermissionRepairRunner.class);

    private static final Long BUILTIN_TENANT_ID = 1L;
    private static final String BUILTIN_ROLE_CODE = "ROLE_SUPER_ADMIN";
    private static final String BUILTIN_USERNAME = "admin";
    private static final List<String> REQUIRED_PERMISSION_CODES = List.of(
        "tenant:delete",
        "tenant:package:delete"
    );

    private final SystemRoleMapper systemRoleMapper;
    private final SystemUserMapper systemUserMapper;

    public BuiltinPermissionRepairRunner(SystemRoleMapper systemRoleMapper, SystemUserMapper systemUserMapper) {
        this.systemRoleMapper = systemRoleMapper;
        this.systemUserMapper = systemUserMapper;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        alignBuiltinRolePermissions();
        alignBuiltinUserPermissions();
    }

    private void alignBuiltinRolePermissions() {
        LambdaQueryWrapper<SystemRoleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemRoleDO::getTenantId, BUILTIN_TENANT_ID);
        queryWrapper.eq(SystemRoleDO::getRoleCode, BUILTIN_ROLE_CODE);
        queryWrapper.last("LIMIT 1");
        SystemRoleDO role = systemRoleMapper.selectOne(queryWrapper);
        if (role == null) {
            return;
        }

        String nextPermissionCodes = mergePermissionCodes(role.getPermissionCodes());
        if (nextPermissionCodes.equals(normalizePermissionCodes(role.getPermissionCodes()))) {
            return;
        }

        role.setPermissionCodes(nextPermissionCodes);
        role.setUpdateBy("builtin-permission-repair");
        systemRoleMapper.updateById(role);
        log.info("aligned builtin role permissions tenantId={} roleCode={}", role.getTenantId(), role.getRoleCode());
    }

    private void alignBuiltinUserPermissions() {
        LambdaQueryWrapper<SystemUserDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemUserDO::getTenantId, BUILTIN_TENANT_ID);
        queryWrapper.eq(SystemUserDO::getUsername, BUILTIN_USERNAME);
        queryWrapper.last("LIMIT 1");
        SystemUserDO user = systemUserMapper.selectOne(queryWrapper);
        if (user == null) {
            return;
        }

        String nextPermissionCodes = mergePermissionCodes(user.getPermissionCodes());
        if (nextPermissionCodes.equals(normalizePermissionCodes(user.getPermissionCodes()))) {
            return;
        }

        user.setPermissionCodes(nextPermissionCodes);
        user.setUpdateBy("builtin-permission-repair");
        systemUserMapper.updateById(user);
        log.info("aligned builtin user permissions tenantId={} username={}", user.getTenantId(), user.getUsername());
    }

    private String mergePermissionCodes(String currentPermissionCodes) {
        Set<String> values = new LinkedHashSet<>(splitPermissionCodes(currentPermissionCodes));
        values.addAll(REQUIRED_PERMISSION_CODES);
        return String.join(",", values);
    }

    private String normalizePermissionCodes(String permissionCodes) {
        return String.join(",", splitPermissionCodes(permissionCodes));
    }

    private List<String> splitPermissionCodes(String permissionCodes) {
        if (!StringUtils.hasText(permissionCodes)) {
            return List.of();
        }
        return List.of(permissionCodes.split(",")).stream()
            .map(String::trim)
            .filter(StringUtils::hasText)
            .toList();
    }
}
