package com.luckycolor.admin.modules.system.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.modules.iam.auth.config.LocalAuthProperties;
import com.luckycolor.admin.modules.system.role.dataobject.SystemRoleDO;
import com.luckycolor.admin.modules.system.role.mapper.SystemRoleMapper;
import com.luckycolor.admin.modules.system.user.dataobject.SystemUserDO;
import com.luckycolor.admin.modules.system.user.mapper.SystemUserMapper;
import com.luckycolor.admin.modules.system.user.service.SystemUserService;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserAssignRolesRequest;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserPageQuery;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserResetPasswordRequest;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserSaveRequest;
import com.luckycolor.admin.modules.system.user.web.request.SystemUserStatusRequest;
import com.luckycolor.admin.modules.system.user.web.response.SystemUserDetailResponse;
import com.luckycolor.admin.modules.system.user.web.response.SystemUserExportPreviewResponse;
import com.luckycolor.admin.modules.system.user.web.response.SystemUserPageResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import com.luckycolor.admin.common.config.ConditionalOnPersistenceEnabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

@Service
@ConditionalOnPersistenceEnabled
public class SystemUserServiceImpl implements SystemUserService {

    private final SystemUserMapper systemUserMapper;
    private final DataScopeConditionBuilder dataScopeConditionBuilder;
    private final LocalAuthProperties localAuthProperties;
    private final PasswordEncoder passwordEncoder;
    private final SystemRoleMapper systemRoleMapper;

    @Autowired
    public SystemUserServiceImpl(
        SystemUserMapper systemUserMapper,
        DataScopeConditionBuilder dataScopeConditionBuilder,
        LocalAuthProperties localAuthProperties,
        PasswordEncoder passwordEncoder
    ) {
        this(systemUserMapper, dataScopeConditionBuilder, localAuthProperties, passwordEncoder, null);
    }

    public SystemUserServiceImpl(
        SystemUserMapper systemUserMapper,
        DataScopeConditionBuilder dataScopeConditionBuilder,
        LocalAuthProperties localAuthProperties,
        PasswordEncoder passwordEncoder,
        @Nullable SystemRoleMapper systemRoleMapper
    ) {
        this.systemUserMapper = systemUserMapper;
        this.dataScopeConditionBuilder = dataScopeConditionBuilder;
        this.localAuthProperties = localAuthProperties;
        this.passwordEncoder = passwordEncoder;
        this.systemRoleMapper = systemRoleMapper;
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
        if (systemRoleMapper != null) {
            LambdaQueryWrapper<SystemRoleDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SystemRoleDO::getStatus, 0)
                .orderByAsc(SystemRoleDO::getSort)
                .orderByAsc(SystemRoleDO::getId);
            systemRoleMapper.selectList(queryWrapper)
                .forEach(role -> roleCodes.add(role.getRoleCode()));
        }
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

    @Override
    public void resetPassword(Long id, SystemUserResetPasswordRequest request) {
        SystemUserDO user = getRequiredUser(id);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        systemUserMapper.updateById(user);
    }

    @Override
    public void assignRoles(Long id, SystemUserAssignRolesRequest request) {
        SystemUserDO user = getRequiredUser(id);
        user.setRoleCodes(joinCodes(request.getRoleCodes()));
        systemUserMapper.updateById(user);
    }

    @Override
    public byte[] exportUsers(SystemUserPageQuery query) {
        StringBuilder builder = new StringBuilder();
        builder.append("username,nickname,email,mobile,roleCodes,status").append('\n');
        selectUsersForExport(query).forEach(user -> {
            builder.append(csv(user.getUsername())).append(',')
                .append(csv(user.getNickname())).append(',')
                .append(csv(user.getEmail())).append(',')
                .append(csv(user.getMobile())).append(',')
                .append(csv(user.getRoleCodes())).append(',')
                .append(user.getStatus() == null ? "" : user.getStatus())
                .append('\n');
        });
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public int importUsers(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Import file is required");
        }
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)
        )) {
            String header = reader.readLine();
            if (!StringUtils.hasText(header)) {
                return 0;
            }
            int importedCount = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (!StringUtils.hasText(line)) {
                    continue;
                }
                upsertImportedUser(splitCsvLine(line));
                importedCount++;
            }
            return importedCount;
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to import users", exception);
        }
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

    private List<SystemUserDO> selectUsersForExport(SystemUserPageQuery query) {
        return systemUserMapper.selectList(buildQueryWrapper(query));
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

    private void upsertImportedUser(List<String> columns) {
        String username = column(columns, 0);
        if (!StringUtils.hasText(username)) {
            return;
        }
        SystemUserDO existingUser = findByUsername(username);
        if (existingUser == null) {
            existingUser = new SystemUserDO();
            existingUser.setUsername(username);
            existingUser.setPassword(passwordEncoder.encode(defaultPassword(column(columns, 7))));
            applyImportedColumns(existingUser, columns);
            systemUserMapper.insert(existingUser);
            return;
        }
        applyImportedColumns(existingUser, columns);
        String password = column(columns, 7);
        if (StringUtils.hasText(password)) {
            existingUser.setPassword(passwordEncoder.encode(password));
        }
        systemUserMapper.updateById(existingUser);
    }

    private void applyImportedColumns(SystemUserDO user, List<String> columns) {
        user.setNickname(column(columns, 1));
        user.setEmail(column(columns, 2));
        user.setMobile(column(columns, 3));
        user.setRoleCodes(column(columns, 4));
        user.setPermissionCodes(column(columns, 5));
        user.setDataScope(defaultValue(column(columns, 6), "TENANT"));
        user.setStatus(parseInteger(column(columns, 8), 0));
        user.setRemark(column(columns, 9));
    }

    private SystemUserDO findByUsername(String username) {
        LambdaQueryWrapper<SystemUserDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemUserDO::getUsername, username.trim());
        return systemUserMapper.selectList(queryWrapper).stream().findFirst().orElse(null);
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

    private List<String> splitCsvLine(String line) {
        java.util.ArrayList<String> columns = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < line.length(); index++) {
            char currentChar = line.charAt(index);
            if (currentChar == '"') {
                quoted = !quoted;
                continue;
            }
            if (currentChar == ',' && !quoted) {
                columns.add(current.toString().trim());
                current.setLength(0);
                continue;
            }
            current.append(currentChar);
        }
        columns.add(current.toString().trim());
        return columns;
    }

    private String csv(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private String column(List<String> columns, int index) {
        return index >= columns.size() ? null : columns.get(index);
    }

    private Integer parseInteger(String value, Integer defaultValue) {
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        return Integer.parseInt(value.trim());
    }

    private String defaultPassword(String password) {
        return StringUtils.hasText(password) ? password : "ChangeMe123!";
    }

    private String defaultValue(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}
