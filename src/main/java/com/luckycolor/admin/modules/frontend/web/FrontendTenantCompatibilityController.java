package com.luckycolor.admin.modules.frontend.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.config.ConditionalOnPersistenceEnabled;
import com.luckycolor.admin.common.page.PageQuery;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.infrastructure.tenant.core.TenantIgnoreContextHolder;
import com.luckycolor.admin.modules.platform.storage.service.FileStorageService;
import com.luckycolor.admin.modules.platform.storage.web.response.FileUploadResponse;
import com.luckycolor.admin.modules.system.menu.dataobject.MenuDO;
import com.luckycolor.admin.modules.system.menu.mapper.MenuMapper;
import com.luckycolor.admin.modules.tenant.audit.dataobject.TenantAuditLogDO;
import com.luckycolor.admin.modules.tenant.audit.mapper.TenantAuditLogMapper;
import com.luckycolor.admin.modules.tenant.bootstrap.config.TenantBootstrapProperties;
import com.luckycolor.admin.modules.tenant.bootstrap.dataobject.TenantBootstrapRecordDO;
import com.luckycolor.admin.modules.tenant.bootstrap.mapper.TenantBootstrapRecordMapper;
import com.luckycolor.admin.modules.tenant.bootstrap.service.TenantBootstrapService;
import com.luckycolor.admin.modules.tenant.bootstrap.web.request.TenantBootstrapExecuteRequest;
import com.luckycolor.admin.modules.tenant.bootstrap.web.response.TenantBootstrapTemplateResponse;
import com.luckycolor.admin.modules.tenant.packageinfo.dataobject.TenantPackageDO;
import com.luckycolor.admin.modules.tenant.packageinfo.mapper.TenantPackageMapper;
import com.luckycolor.admin.modules.tenant.packageinfo.service.TenantPackageService;
import com.luckycolor.admin.modules.tenant.packageinfo.web.request.TenantPackageSaveRequest;
import com.luckycolor.admin.modules.tenant.profile.dataobject.TenantProfileDO;
import com.luckycolor.admin.modules.tenant.profile.mapper.TenantProfileMapper;
import com.luckycolor.admin.modules.tenant.tenant.dataobject.TenantDO;
import com.luckycolor.admin.modules.tenant.tenant.mapper.TenantMapper;
import com.luckycolor.admin.modules.tenant.tenant.service.TenantService;
import com.luckycolor.admin.modules.tenant.tenant.web.request.TenantSaveRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.luckycolor.admin.modules.platform.storage.service.StoredFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Validated
@ConditionalOnPersistenceEnabled
public class FrontendTenantCompatibilityController {

    private static final Logger log = LoggerFactory.getLogger(FrontendTenantCompatibilityController.class);
    private static final String META_PREFIX = "LC_META:";
    private static final Pattern NON_CODE_CHARS = Pattern.compile("[^a-z0-9]+");
    private static final int ACTIVE_STATUS = 0;
    private static final int DISABLED_STATUS = 1;

    private final TenantService tenantService;
    private final TenantMapper tenantMapper;
    private final TenantPackageService tenantPackageService;
    private final TenantPackageMapper tenantPackageMapper;
    private final TenantProfileMapper tenantProfileMapper;
    private final TenantBootstrapService tenantBootstrapService;
    private final TenantBootstrapProperties tenantBootstrapProperties;
    private final TenantBootstrapRecordMapper tenantBootstrapRecordMapper;
    private final TenantAuditLogMapper tenantAuditLogMapper;
    private final MenuMapper menuMapper;
    private final FileStorageService fileStorageService;
    private final DataScopeConditionBuilder dataScopeConditionBuilder;
    private final ObjectMapper objectMapper;

    public FrontendTenantCompatibilityController(
        TenantService tenantService,
        TenantMapper tenantMapper,
        TenantPackageService tenantPackageService,
        TenantPackageMapper tenantPackageMapper,
        TenantProfileMapper tenantProfileMapper,
        TenantBootstrapService tenantBootstrapService,
        TenantBootstrapProperties tenantBootstrapProperties,
        TenantBootstrapRecordMapper tenantBootstrapRecordMapper,
        TenantAuditLogMapper tenantAuditLogMapper,
        MenuMapper menuMapper,
        FileStorageService fileStorageService,
        DataScopeConditionBuilder dataScopeConditionBuilder,
        ObjectMapper objectMapper
    ) {
        this.tenantService = tenantService;
        this.tenantMapper = tenantMapper;
        this.tenantPackageService = tenantPackageService;
        this.tenantPackageMapper = tenantPackageMapper;
        this.tenantProfileMapper = tenantProfileMapper;
        this.tenantBootstrapService = tenantBootstrapService;
        this.tenantBootstrapProperties = tenantBootstrapProperties;
        this.tenantBootstrapRecordMapper = tenantBootstrapRecordMapper;
        this.tenantAuditLogMapper = tenantAuditLogMapper;
        this.menuMapper = menuMapper;
        this.fileStorageService = fileStorageService;
        this.dataScopeConditionBuilder = dataScopeConditionBuilder;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/tenants")
    @RequirePermission("tenant:query")
    public ApiResponse<FrontendPageResult<FrontendTenantRecord>> pageTenants(
        @RequestParam(value = "page", required = false) Long page,
        @RequestParam(value = "size", required = false) Long size,
        @RequestParam(value = "keyword", required = false) String keyword,
        @RequestParam(value = "status", required = false) String status
    ) {
        PageQuery pageQuery = buildPageQuery(page, size);
        List<TenantDO> tenants = listTenants();
        Map<Long, TenantProfileDO> profiles = loadTenantProfilesByIds(extractTenantIds(tenants));
        Map<Long, TenantPackageDO> packages = loadTenantPackagesByIds(extractPackageIds(tenants));
        List<FrontendTenantRecord> records = tenants.stream()
            .map(tenant -> toFrontendTenant(tenant, profiles.get(tenant.getId()), packages.get(tenant.getPackageId())))
            .filter(record -> matchesTenantStatus(record, status))
            .filter(record -> matchesTenantKeyword(record, keyword))
            .toList();
        return ApiResponse.success(toFrontendPage(pageQuery, records));
    }

    @GetMapping("/tenants/{id}")
    @RequirePermission("tenant:query")
    public ApiResponse<FrontendTenantRecord> getTenant(@PathVariable Long id) {
        TenantDO tenant = getRequiredTenant(id);
        return ApiResponse.success(toFrontendTenant(tenant, loadTenantProfile(id), loadTenantPackage(tenant.getPackageId())));
    }

    @PostMapping("/tenants")
    @RequirePermission("tenant:create")
    public ApiResponse<FrontendTenantInitResult> createTenant(@Valid @RequestBody FrontendTenantCreateRequest request) {
        ensureTenantCodeUnique(request.getCode(), null);
        Long packageId = parseId(request.getPackageId(), "Tenant package id");
        TenantPackageDO tenantPackage = packageId == null ? null : getRequiredTenantPackage(packageId);

        TenantSaveRequest nativeRequest = new TenantSaveRequest();
        nativeRequest.setName(request.getName().trim());
        nativeRequest.setPackageId(packageId == null ? resolveDefaultPackageId() : packageId);
        nativeRequest.setContactName(resolveString(request.getContactName(), "Tenant Admin"));
        nativeRequest.setContactMobile(resolveString(request.getContactPhone(), "13800000000"));
        nativeRequest.setAccountCount(resolveAccountCount(tenantPackage, null));
        nativeRequest.setExpireTime(resolveExpireTime(request.getExpiresAt(), null));
        nativeRequest.setStatus(toNativeTenantStatus(request.getStatus()));

        Long tenantId = tenantService.createTenant(nativeRequest);
        upsertTenantProfile(
            tenantId,
            request.getCode(),
            request.getStatus(),
            request.getContactEmail(),
            request.getRemark(),
            resolveString(request.getAdminUsername(), "admin"),
            resolveString(request.getAdminNickname(), request.getName().trim() + " Admin")
        );
        recordBootstrap(tenantId, request);

        TenantDO tenant = getRequiredTenant(tenantId);
        TenantProfileDO profile = loadTenantProfile(tenantId);
        TenantPackageDO createdPackage = loadTenantPackage(tenant.getPackageId());
        return ApiResponse.success(buildTenantInitResult(tenant, profile, createdPackage));
    }

    @PatchMapping("/tenants/{id}")
    @RequirePermission("tenant:update")
    public ApiResponse<FrontendTenantRecord> updateTenant(
        @PathVariable Long id,
        @RequestBody FrontendTenantUpdateRequest request
    ) {
        TenantDO current = getRequiredTenant(id);
        TenantProfileDO currentProfile = loadTenantProfile(id);
        Long packageId = request.getPackageId() != null ? parseId(request.getPackageId(), "Tenant package id") : current.getPackageId();
        TenantPackageDO tenantPackage = packageId == null ? null : loadTenantPackage(packageId);

        TenantSaveRequest nativeRequest = new TenantSaveRequest();
        nativeRequest.setName(resolveString(request.getName(), current.getName()));
        nativeRequest.setPackageId(packageId);
        nativeRequest.setContactName(resolveNullableString(request.getContactName(), current.getContactName()));
        nativeRequest.setContactMobile(resolveNullableString(request.getContactPhone(), current.getContactMobile()));
        nativeRequest.setAccountCount(resolveAccountCount(tenantPackage, current.getAccountCount()));
        nativeRequest.setExpireTime(resolveExpireTime(request.getExpiresAt(), current.getExpireTime()));
        nativeRequest.setStatus(request.getStatus() != null ? toNativeTenantStatus(request.getStatus()) : current.getStatus());
        tenantService.updateTenant(id, nativeRequest);

        String nextCode = currentProfile != null && StringUtils.hasText(currentProfile.getTenantCode())
            ? currentProfile.getTenantCode()
            : defaultTenantCode(id);
        ensureTenantCodeUnique(nextCode, id);
        upsertTenantProfile(
            id,
            nextCode,
            request.getStatus() != null ? request.getStatus() : currentProfileStatus(currentProfile, current.getStatus()),
            request.getContactEmail() != null ? request.getContactEmail() : valueOrNull(currentProfile == null ? null : currentProfile.getContactEmail()),
            request.getRemark() != null ? request.getRemark() : valueOrNull(currentProfile == null ? null : currentProfile.getRemark()),
            currentProfile == null ? "admin" : currentProfile.getAdminUsername(),
            currentProfile == null ? current.getName() + " Admin" : currentProfile.getAdminNickname()
        );

        TenantDO updated = getRequiredTenant(id);
        return ApiResponse.success(
            toFrontendTenant(updated, loadTenantProfile(id), loadTenantPackage(updated.getPackageId()))
        );
    }

    @DeleteMapping("/tenants/{id}")
    @RequirePermission("tenant:delete")
    public ApiResponse<Boolean> deleteTenant(@PathVariable Long id) {
        getRequiredTenant(id);
        tenantBootstrapRecordMapper.delete(new LambdaQueryWrapper<TenantBootstrapRecordDO>().eq(TenantBootstrapRecordDO::getTenantId, id));
        tenantAuditLogMapper.delete(new LambdaQueryWrapper<TenantAuditLogDO>().eq(TenantAuditLogDO::getTenantId, id));
        tenantProfileMapper.deleteById(id);
        tenantMapper.deleteById(id);
        return ApiResponse.success(true);
    }

    @GetMapping("/tenant-packages")
    @RequirePermission("tenant:package:query")
    public ApiResponse<FrontendPageResult<FrontendTenantPackageRecord>> pageTenantPackages(
        @RequestParam(value = "page", required = false) Long page,
        @RequestParam(value = "size", required = false) Long size,
        @RequestParam(value = "keyword", required = false) String keyword,
        @RequestParam(value = "status", required = false) Boolean status
    ) {
        PageQuery pageQuery = buildPageQuery(page, size);
        LambdaQueryWrapper<TenantPackageDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(TenantPackageDO::getSort).orderByDesc(TenantPackageDO::getCreateTime);
        List<FrontendTenantPackageRecord> records = tenantPackageMapper.selectList(queryWrapper).stream()
            .map(this::toFrontendTenantPackage)
            .filter(record -> matchesPackageStatus(record, status))
            .filter(record -> matchesPackageKeyword(record, keyword))
            .toList();
        return ApiResponse.success(toFrontendPage(pageQuery, records));
    }

    @GetMapping("/tenant-packages/{id}")
    @RequirePermission("tenant:package:query")
    public ApiResponse<FrontendTenantPackageRecord> getTenantPackage(@PathVariable Long id) {
        return ApiResponse.success(toFrontendTenantPackage(getRequiredTenantPackage(id)));
    }

    @PostMapping("/tenant-packages")
    @RequirePermission("tenant:package:create")
    public ApiResponse<FrontendTenantPackageRecord> createTenantPackage(
        @Valid @RequestBody FrontendTenantPackageCreateRequest request
    ) {
        ensureTenantPackageCodeUnique(request.getCode(), null);
        TenantPackageSaveRequest nativeRequest = new TenantPackageSaveRequest();
        nativeRequest.setPackageName(request.getName().trim());
        nativeRequest.setStatus(toNativePackageStatus(request.getStatus()));
        nativeRequest.setSort(0);
        nativeRequest.setRemark(encodePackageRemark(toPackageMeta(request, List.of())));
        Long id = tenantPackageService.createTenantPackage(nativeRequest);
        return ApiResponse.success(toFrontendTenantPackage(getRequiredTenantPackage(id)));
    }

    @PatchMapping("/tenant-packages/{id}")
    @RequirePermission("tenant:package:update")
    public ApiResponse<FrontendTenantPackageRecord> updateTenantPackage(
        @PathVariable Long id,
        @RequestBody FrontendTenantPackageUpdateRequest request
    ) {
        TenantPackageDO current = getRequiredTenantPackage(id);
        TenantPackageMetaPayload currentMeta = parsePackageMeta(current);
        String nextCode = request.getCode() != null ? request.getCode().trim() : currentMeta.getCode();
        ensureTenantPackageCodeUnique(nextCode, id);

        TenantPackageSaveRequest nativeRequest = new TenantPackageSaveRequest();
        nativeRequest.setPackageName(resolveString(request.getName(), current.getPackageName()));
        nativeRequest.setStatus(request.getStatus() != null ? toNativePackageStatus(request.getStatus()) : current.getStatus());
        nativeRequest.setSort(current.getSort());
        nativeRequest.setRemark(encodePackageRemark(mergePackageMeta(currentMeta, request, nextCode)));
        tenantPackageService.updateTenantPackage(id, nativeRequest);
        return ApiResponse.success(toFrontendTenantPackage(getRequiredTenantPackage(id)));
    }

    @DeleteMapping("/tenant-packages/{id}")
    @RequirePermission("tenant:package:delete")
    public ApiResponse<Boolean> deleteTenantPackage(@PathVariable Long id) {
        getRequiredTenantPackage(id);
        LambdaQueryWrapper<TenantDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TenantDO::getPackageId, id);
        Long usedCount = tenantMapper.selectCount(queryWrapper);
        if (usedCount != null && usedCount > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tenant package is in use");
        }
        tenantPackageMapper.deleteById(id);
        return ApiResponse.success(true);
    }

    @GetMapping("/tenant-packages/{id}/menus")
    @RequirePermission("tenant:package:query")
    public ApiResponse<FrontendTenantPackageMenuAssignment> getTenantPackageMenus(@PathVariable Long id) {
        TenantPackageMetaPayload metadata = parsePackageMeta(getRequiredTenantPackage(id));
        return ApiResponse.success(new FrontendTenantPackageMenuAssignment(new ArrayList<>(metadata.getMenuIds())));
    }

    @PutMapping("/tenant-packages/{id}/menus")
    @RequirePermission("tenant:package:update")
    public ApiResponse<FrontendTenantPackageMenuAssignment> assignTenantPackageMenus(
        @PathVariable Long id,
        @RequestBody FrontendTenantPackageMenuAssignment request
    ) {
        TenantPackageDO current = getRequiredTenantPackage(id);
        TenantPackageMetaPayload metadata = parsePackageMeta(current);
        metadata.setMenuIds(deduplicateLongIds(request.menuIds()));
        current.setRemark(encodePackageRemark(metadata));
        tenantPackageMapper.updateById(current);
        return ApiResponse.success(new FrontendTenantPackageMenuAssignment(new ArrayList<>(metadata.getMenuIds())));
    }

    @PostMapping(value = "/file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequirePermission("file:upload")
    public ApiResponse<FrontendFileInfo> uploadFile(@RequestParam("file") MultipartFile file) {
        FileUploadResponse uploaded = fileStorageService.upload(file);
        return ApiResponse.success(new FrontendFileInfo(
            uploaded.originalFilename(),
            uploaded.downloadUrl(),
            uploaded.relativePath()
        ));
    }

    @GetMapping("/file/delete")
    @RequirePermission("file:delete")
    public ApiResponse<Boolean> deleteFile(@RequestParam("filePath") String filePath) {
        return ApiResponse.success(fileStorageService.delete(normalizeFilePath(filePath)));
    }

    @GetMapping("/file/**")
    @RequirePermission("file:download")
    public ResponseEntity<Resource> readFile(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        int markerIndex = requestUri.indexOf("/file/");
        String storagePath = markerIndex >= 0
            ? requestUri.substring(markerIndex + "/file/".length())
            : requestUri;
        StoredFile storedFile = fileStorageService.download(storagePath);
        MediaType mediaType = storedFile.contentType() == null
            ? MediaType.APPLICATION_OCTET_STREAM
            : MediaType.parseMediaType(storedFile.contentType());
        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.inline()
                    .filename(storedFile.storedFilename(), StandardCharsets.UTF_8)
                    .build()
                    .toString()
            )
            .contentType(mediaType)
            .contentLength(storedFile.size())
            .body(storedFile.resource());
    }

    private List<TenantDO> listTenants() {
        LambdaQueryWrapper<TenantDO> queryWrapper = new LambdaQueryWrapper<>();
        dataScopeConditionBuilder.applyCurrentScope(queryWrapper, TenantDO::getId, null);
        queryWrapper.orderByDesc(TenantDO::getUpdateTime).orderByDesc(TenantDO::getId);
        return tenantMapper.selectList(queryWrapper);
    }

    private Map<Long, TenantProfileDO> loadTenantProfilesByIds(Collection<Long> tenantIds) {
        List<Long> ids = tenantIds == null ? List.of() : tenantIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return tenantProfileMapper.selectBatchIds(ids).stream()
            .collect(Collectors.toMap(TenantProfileDO::getId, item -> item));
    }

    private Map<Long, TenantPackageDO> loadTenantPackagesByIds(Collection<Long> packageIds) {
        List<Long> ids = packageIds == null ? List.of() : packageIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return tenantPackageMapper.selectBatchIds(ids).stream()
            .collect(Collectors.toMap(TenantPackageDO::getId, item -> item));
    }

    private List<Long> extractTenantIds(List<TenantDO> tenants) {
        return tenants.stream().map(TenantDO::getId).filter(Objects::nonNull).toList();
    }

    private List<Long> extractPackageIds(List<TenantDO> tenants) {
        return tenants.stream().map(TenantDO::getPackageId).filter(Objects::nonNull).distinct().toList();
    }

    private FrontendTenantRecord toFrontendTenant(
        TenantDO tenant,
        TenantProfileDO profile,
        TenantPackageDO tenantPackage
    ) {
        TenantPackageMetaPayload packageMeta = tenantPackage == null ? null : parsePackageMeta(tenantPackage);
        FrontendTenantPackageSummary packageSummary = tenantPackage == null
            ? null
            : new FrontendTenantPackageSummary(
                stringify(tenantPackage.getId()),
                defaultString(packageMeta == null ? null : packageMeta.getCode(), defaultPackageCode(tenantPackage)),
                tenantPackage.getPackageName(),
                tenantPackage.getStatus() == ACTIVE_STATUS
            );
        return new FrontendTenantRecord(
            stringify(tenant.getId()),
            profile != null && StringUtils.hasText(profile.getTenantCode()) ? profile.getTenantCode() : defaultTenantCode(tenant.getId()),
            tenant.getName(),
            currentProfileStatus(profile, tenant.getStatus()),
            toIsoInstant(tenant.getExpireTime()),
            tenant.getContactName(),
            tenant.getContactMobile(),
            profile == null ? null : profile.getContactEmail(),
            packageSummary,
            profile == null ? null : profile.getRemark(),
            toIsoInstant(tenant.getCreateTime()),
            toIsoInstant(tenant.getUpdateTime())
        );
    }

    private FrontendTenantInitResult buildTenantInitResult(
        TenantDO tenant,
        TenantProfileDO profile,
        TenantPackageDO tenantPackage
    ) {
        TenantBootstrapTemplateResponse template = resolveDefaultTemplate();
        String tenantCode = profile != null && StringUtils.hasText(profile.getTenantCode())
            ? profile.getTenantCode()
            : defaultTenantCode(tenant.getId());
        String adminUsername = profile != null && StringUtils.hasText(profile.getAdminUsername())
            ? profile.getAdminUsername()
            : "admin";
        String adminNickname = profile != null && StringUtils.hasText(profile.getAdminNickname())
            ? profile.getAdminNickname()
            : tenant.getName() + " Admin";

        List<FrontendTenantInitRole> roles = template == null
            ? List.of()
            : template.roleCodes().stream()
                .map(code -> new FrontendTenantInitRole("tenant:" + tenant.getId() + ":role:" + code, code, humanizeCode(code)))
                .toList();

        List<FrontendTenantInitDepartment> departments = List.of(
            new FrontendTenantInitDepartment(tenant.getId(), tenantCode + "_headquarters", "Headquarters")
        );
        List<Long> menuIds = template == null ? List.of() : resolveMenuIds(template.menuCodes());

        return new FrontendTenantInitResult(
            toFrontendTenant(tenant, profile, tenantPackage),
            new FrontendTenantInitAdminUser("tenant:" + tenant.getId() + ":admin", adminUsername, adminNickname),
            roles,
            departments,
            menuIds,
            buildDictionaryIds(tenant.getId())
        );
    }

    private FrontendTenantPackageRecord toFrontendTenantPackage(TenantPackageDO tenantPackage) {
        TenantPackageMetaPayload metadata = parsePackageMeta(tenantPackage);
        return new FrontendTenantPackageRecord(
            stringify(tenantPackage.getId()),
            defaultString(metadata.getCode(), defaultPackageCode(tenantPackage)),
            tenantPackage.getPackageName(),
            tenantPackage.getStatus() == ACTIVE_STATUS,
            metadata.getMaxUsers(),
            metadata.getMaxRoles(),
            metadata.getMaxMenus(),
            metadata.getFeatureFlags().isEmpty() ? null : metadata.getFeatureFlags(),
            metadata.getLegacyRemark(),
            toIsoInstant(tenantPackage.getCreateTime()),
            toIsoInstant(tenantPackage.getUpdateTime())
        );
    }

    private TenantProfileDO loadTenantProfile(Long tenantId) {
        return tenantId == null ? null : tenantProfileMapper.selectById(tenantId);
    }

    private TenantDO getRequiredTenant(Long id) {
        TenantDO tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found");
        }
        return tenant;
    }

    private TenantPackageDO getRequiredTenantPackage(Long id) {
        TenantPackageDO tenantPackage = tenantPackageMapper.selectById(id);
        if (tenantPackage == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant package not found");
        }
        return tenantPackage;
    }

    private TenantPackageDO loadTenantPackage(Long id) {
        return id == null ? null : tenantPackageMapper.selectById(id);
    }

    private void upsertTenantProfile(
        Long tenantId,
        String tenantCode,
        String status,
        String contactEmail,
        String remark,
        String adminUsername,
        String adminNickname
    ) {
        TenantProfileDO profile = loadTenantProfile(tenantId);
        boolean create = profile == null;
        if (profile == null) {
            profile = new TenantProfileDO();
            profile.setId(tenantId);
        }
        profile.setTenantCode(tenantCode == null ? defaultTenantCode(tenantId) : tenantCode.trim());
        profile.setStatusCode(resolveFrontendTenantStatus(status, null));
        profile.setContactEmail(valueOrNull(contactEmail));
        profile.setRemark(valueOrNull(remark));
        profile.setAdminUsername(valueOrNull(adminUsername));
        profile.setAdminNickname(valueOrNull(adminNickname));
        if (create) {
            tenantProfileMapper.insert(profile);
        } else {
            tenantProfileMapper.updateById(profile);
        }
    }

    private void ensureTenantCodeUnique(String tenantCode, Long currentTenantId) {
        if (!StringUtils.hasText(tenantCode)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant code is required");
        }
        LambdaQueryWrapper<TenantProfileDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TenantProfileDO::getTenantCode, tenantCode.trim());
        TenantProfileDO existing = tenantProfileMapper.selectOne(queryWrapper);
        if (existing != null && !Objects.equals(existing.getId(), currentTenantId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tenant code already exists");
        }
    }

    private void ensureTenantPackageCodeUnique(String tenantPackageCode, Long currentId) {
        if (!StringUtils.hasText(tenantPackageCode)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant package code is required");
        }
        for (TenantPackageDO tenantPackage : tenantPackageMapper.selectList(new LambdaQueryWrapper<>())) {
            if (Objects.equals(tenantPackage.getId(), currentId)) {
                continue;
            }
            TenantPackageMetaPayload metadata = parsePackageMeta(tenantPackage);
            String existingCode = defaultString(metadata.getCode(), defaultPackageCode(tenantPackage));
            if (tenantPackageCode.trim().equalsIgnoreCase(existingCode)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Tenant package code already exists");
            }
        }
    }

    private void recordBootstrap(Long tenantId, FrontendTenantCreateRequest request) {
        TenantBootstrapTemplateResponse template = resolveDefaultTemplate();
        if (template == null) {
            return;
        }
        try {
            TenantBootstrapExecuteRequest bootstrapRequest = new TenantBootstrapExecuteRequest();
            bootstrapRequest.setTemplateCode(template.code());
            bootstrapRequest.setAdminUsername(resolveString(request.getAdminUsername(), "admin"));
            bootstrapRequest.setAdminNickname(resolveString(request.getAdminNickname(), request.getName().trim() + " Admin"));
            bootstrapRequest.setRemark(valueOrNull(request.getRemark()));
            tenantBootstrapService.bootstrapTenant(tenantId, bootstrapRequest);
        } catch (RuntimeException exception) {
            log.warn("failed to persist tenant bootstrap record tenantId={}", tenantId, exception);
        }
    }

    private TenantBootstrapTemplateResponse resolveDefaultTemplate() {
        return tenantBootstrapProperties.getTemplates().stream()
            .filter(template -> StringUtils.hasText(template.getCode()))
            .filter(template -> template.getStatus() == null || template.getStatus() == ACTIVE_STATUS)
            .findFirst()
            .map(template -> new TenantBootstrapTemplateResponse(
                template.getCode(),
                template.getName(),
                template.getRoleCodes(),
                template.getMenuCodes(),
                template.getAdminUsername(),
                template.getAdminNickname(),
                template.getStatus(),
                template.getRemark()
            ))
            .orElse(null);
    }

    private List<Long> resolveMenuIds(List<String> menuCodes) {
        if (menuCodes == null || menuCodes.isEmpty()) {
            return List.of();
        }
        Map<String, Long> knownMenuIds = new LinkedHashMap<>();
        TenantIgnoreContextHolder.enter();
        try {
            for (MenuDO menu : menuMapper.selectList(new LambdaQueryWrapper<>())) {
                String code = toCompatibilityMenuCode(menu);
                if (code != null) {
                    knownMenuIds.putIfAbsent(code, menu.getId());
                }
            }
        } finally {
            TenantIgnoreContextHolder.exit();
        }
        return menuCodes.stream()
            .map(knownMenuIds::get)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    }

    private String toCompatibilityMenuCode(MenuDO menu) {
        if (menu == null) {
            return null;
        }
        if ("/dashboard".equals(menu.getRoutePath()) || "dashboard:query".equals(menu.getPermissionCode())) {
            return "dashboard";
        }
        if ("system:user:query".equals(menu.getPermissionCode())) {
            return "system:user";
        }
        if ("system:role:query".equals(menu.getPermissionCode())) {
            return "system:role";
        }
        if ("system:department:query".equals(menu.getPermissionCode())) {
            return "system:department";
        }
        if ("system:dictionary:query".equals(menu.getPermissionCode())) {
            return "system:dictionary";
        }
        if ("system:config:query".equals(menu.getPermissionCode())) {
            return "system:config";
        }
        if ("system:notice:query".equals(menu.getPermissionCode())) {
            return "system:notice";
        }
        return null;
    }

    private TenantPackageMetaPayload toPackageMeta(FrontendTenantPackageCreateRequest request, List<Long> menuIds) {
        TenantPackageMetaPayload metadata = new TenantPackageMetaPayload();
        metadata.setCode(request.getCode().trim());
        metadata.setMaxUsers(request.getMaxUsers());
        metadata.setMaxRoles(request.getMaxRoles());
        metadata.setMaxMenus(request.getMaxMenus());
        metadata.setFeatureFlags(request.getFeatureFlags() == null ? Map.of() : request.getFeatureFlags());
        metadata.setMenuIds(menuIds);
        metadata.setLegacyRemark(valueOrNull(request.getRemark()));
        return metadata;
    }

    private TenantPackageMetaPayload mergePackageMeta(
        TenantPackageMetaPayload current,
        FrontendTenantPackageUpdateRequest request,
        String nextCode
    ) {
        TenantPackageMetaPayload metadata = new TenantPackageMetaPayload();
        metadata.setCode(nextCode);
        metadata.setMaxUsers(request.getMaxUsers() != null ? request.getMaxUsers() : current.getMaxUsers());
        metadata.setMaxRoles(request.getMaxRoles() != null ? request.getMaxRoles() : current.getMaxRoles());
        metadata.setMaxMenus(request.getMaxMenus() != null ? request.getMaxMenus() : current.getMaxMenus());
        metadata.setFeatureFlags(request.getFeatureFlags() != null ? request.getFeatureFlags() : current.getFeatureFlags());
        metadata.setMenuIds(current.getMenuIds());
        metadata.setLegacyRemark(request.getRemark() != null ? valueOrNull(request.getRemark()) : current.getLegacyRemark());
        return metadata;
    }

    private TenantPackageMetaPayload parsePackageMeta(TenantPackageDO tenantPackage) {
        TenantPackageMetaPayload fallback = new TenantPackageMetaPayload();
        fallback.setCode(defaultPackageCode(tenantPackage));
        fallback.setFeatureFlags(Map.of());
        fallback.setMenuIds(List.of());
        fallback.setLegacyRemark(valueOrNull(tenantPackage.getRemark()));
        if (!StringUtils.hasText(tenantPackage.getRemark()) || !tenantPackage.getRemark().startsWith(META_PREFIX)) {
            return fallback;
        }
        try {
            TenantPackageMetaPayload metadata = objectMapper.readValue(
                tenantPackage.getRemark().substring(META_PREFIX.length()),
                TenantPackageMetaPayload.class
            );
            if (!StringUtils.hasText(metadata.getCode())) {
                metadata.setCode(fallback.getCode());
            }
            if (metadata.getFeatureFlags() == null) {
                metadata.setFeatureFlags(Map.of());
            }
            if (metadata.getMenuIds() == null) {
                metadata.setMenuIds(List.of());
            }
            return metadata;
        } catch (JsonProcessingException exception) {
            log.warn("failed to parse tenant package compatibility metadata id={}", tenantPackage.getId(), exception);
            return fallback;
        }
    }

    private String encodePackageRemark(TenantPackageMetaPayload metadata) {
        try {
            return META_PREFIX + objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to encode tenant package metadata", exception);
        }
    }

    private String normalizeFilePath(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File path is required");
        }
        String trimmed = filePath.trim();
        String resolved = extractPathQueryValue(trimmed);
        if (StringUtils.hasText(resolved)) {
            return resolved;
        }
        String uriPath = extractUriPath(trimmed);
        if (StringUtils.hasText(uriPath)) {
            trimmed = uriPath;
        }
        String frontendPrefix = "/api/file/";
        if (trimmed.startsWith(frontendPrefix)) {
            return trimmed.substring(frontendPrefix.length());
        }
        if (trimmed.startsWith("file/")) {
            return trimmed.substring("file/".length());
        }
        return trimmed.startsWith("/") ? trimmed.substring(1) : trimmed;
    }

    private String extractPathQueryValue(String filePath) {
        try {
            URI uri = URI.create(filePath);
            String query = uri.getQuery();
            if (!StringUtils.hasText(query)) {
                return null;
            }
            for (String entry : query.split("&")) {
                int index = entry.indexOf('=');
                if (index < 0) {
                    continue;
                }
                if (!"path".equals(entry.substring(0, index))) {
                    continue;
                }
                return URLDecoder.decode(entry.substring(index + 1), StandardCharsets.UTF_8);
            }
            return null;
        } catch (IllegalArgumentException exception) {
            int index = filePath.indexOf("path=");
            return index < 0 ? null : filePath.substring(index + "path=".length());
        }
    }

    private String extractUriPath(String filePath) {
        try {
            URI uri = URI.create(filePath);
            String path = uri.getPath();
            if (!StringUtils.hasText(path)) {
                return null;
            }
            return URLDecoder.decode(path, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private PageQuery buildPageQuery(Long page, Long size) {
        PageQuery query = new PageQuery();
        query.setPageNo(page);
        query.setPageSize(size);
        return query;
    }

    private <T> FrontendPageResult<T> toFrontendPage(PageQuery pageQuery, List<T> records) {
        long total = records.size();
        long pageNo = pageQuery.resolvePageNo();
        long pageSize = pageQuery.resolvePageSize();
        int fromIndex = (int) Math.min(Math.max((pageNo - 1L) * pageSize, 0L), total);
        int toIndex = (int) Math.min(fromIndex + pageSize, total);
        List<T> paged = records.subList(fromIndex, toIndex);
        return new FrontendPageResult<>(total, pageNo, pageSize, paged);
    }

    private boolean matchesTenantKeyword(FrontendTenantRecord record, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String normalized = keyword.trim().toLowerCase(Locale.ROOT);
        return containsIgnoreCase(record.code(), normalized)
            || containsIgnoreCase(record.name(), normalized)
            || containsIgnoreCase(record.contactName(), normalized);
    }

    private boolean matchesTenantStatus(FrontendTenantRecord record, String status) {
        return !StringUtils.hasText(status) || status.trim().equalsIgnoreCase(record.status());
    }

    private boolean matchesPackageKeyword(FrontendTenantPackageRecord record, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String normalized = keyword.trim().toLowerCase(Locale.ROOT);
        return containsIgnoreCase(record.code(), normalized) || containsIgnoreCase(record.name(), normalized);
    }

    private boolean matchesPackageStatus(FrontendTenantPackageRecord record, Boolean status) {
        return status == null || status.equals(record.status());
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private Integer resolveAccountCount(TenantPackageDO tenantPackage, Integer fallback) {
        TenantPackageMetaPayload metadata = tenantPackage == null ? null : parsePackageMeta(tenantPackage);
        if (metadata != null && metadata.getMaxUsers() != null && metadata.getMaxUsers() > 0) {
            return metadata.getMaxUsers();
        }
        return fallback != null && fallback > 0 ? fallback : 200;
    }

    private LocalDateTime resolveExpireTime(String expiresAt, LocalDateTime fallback) {
        if (!StringUtils.hasText(expiresAt)) {
            return fallback != null ? fallback : LocalDateTime.now().plusYears(1);
        }
        return LocalDateTime.parse(expiresAt.trim().replace("Z", ""));
    }

    private String resolveFrontendTenantStatus(String status, Integer fallbackNativeStatus) {
        if (StringUtils.hasText(status)) {
            return status.trim().toUpperCase(Locale.ROOT);
        }
        if (fallbackNativeStatus == null) {
            return "ACTIVE";
        }
        return fallbackNativeStatus == ACTIVE_STATUS ? "ACTIVE" : "DISABLED";
    }

    private String currentProfileStatus(TenantProfileDO profile, Integer fallbackNativeStatus) {
        return resolveFrontendTenantStatus(profile == null ? null : profile.getStatusCode(), fallbackNativeStatus);
    }

    private Integer toNativeTenantStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return ACTIVE_STATUS;
        }
        return "ACTIVE".equalsIgnoreCase(status.trim()) ? ACTIVE_STATUS : DISABLED_STATUS;
    }

    private Integer toNativePackageStatus(Boolean status) {
        return status == null || status ? ACTIVE_STATUS : DISABLED_STATUS;
    }

    private List<Long> deduplicateLongIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream().filter(Objects::nonNull).distinct().toList();
    }

    private Long parseId(String value, String label) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + " is invalid", exception);
        }
    }

    private Long resolveDefaultPackageId() {
        LambdaQueryWrapper<TenantPackageDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(TenantPackageDO::getSort).orderByAsc(TenantPackageDO::getId);
        TenantPackageDO tenantPackage = tenantPackageMapper.selectList(queryWrapper).stream().findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant package not found"));
        return tenantPackage.getId();
    }

    private String defaultTenantCode(Long tenantId) {
        return "tenant_" + tenantId;
    }

    private String defaultPackageCode(TenantPackageDO tenantPackage) {
        String base = defaultString(tenantPackage.getPackageName(), "package");
        String normalized = NON_CODE_CHARS.matcher(base.toLowerCase(Locale.ROOT)).replaceAll("_");
        normalized = normalized.replaceAll("^_+|_+$", "");
        return StringUtils.hasText(normalized) ? normalized : "pkg_" + tenantPackage.getId();
    }

    private List<String> buildDictionaryIds(Long tenantId) {
        String prefix = defaultTenantCode(tenantId);
        return List.of(
            prefix + "_notice_scope_root",
            prefix + "_notice_scope_all",
            prefix + "_notice_scope_department",
            prefix + "_notice_scope_role"
        );
    }

    private String humanizeCode(String code) {
        if (!StringUtils.hasText(code)) {
            return "Role";
        }
        return List.of(code.replace(':', ' ').replace('_', ' ').split(" ")).stream()
            .filter(StringUtils::hasText)
            .map(item -> item.substring(0, 1).toUpperCase(Locale.ROOT) + item.substring(1).toLowerCase(Locale.ROOT))
            .collect(Collectors.joining(" "));
    }

    private String resolveString(String preferred, String fallback) {
        return StringUtils.hasText(preferred) ? preferred.trim() : fallback;
    }

    private String resolveNullableString(String preferred, String fallback) {
        return preferred != null ? valueOrNull(preferred) : fallback;
    }

    private String valueOrNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String defaultString(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String stringify(Long value) {
        return value == null ? null : String.valueOf(value);
    }

    private String toIsoInstant(LocalDateTime value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC).toInstant().toString();
    }

    public record FrontendPageResult<T>(long total, long current, long size, List<T> records) {
    }

    public record FrontendTenantPackageSummary(String id, String code, String name, boolean status) {
    }

    public record FrontendTenantRecord(
        String id,
        String code,
        String name,
        String status,
        String expiresAt,
        String contactName,
        String contactPhone,
        String contactEmail,
        FrontendTenantPackageSummary tenantPackage,
        String remark,
        String createdAt,
        String updatedAt
    ) {
    }

    public record FrontendTenantInitAdminUser(String id, String username, String nickname) {
    }

    public record FrontendTenantInitRole(String id, String code, String name) {
    }

    public record FrontendTenantInitDepartment(Long id, String code, String name) {
    }

    public record FrontendTenantInitResult(
        FrontendTenantRecord tenant,
        FrontendTenantInitAdminUser adminUser,
        List<FrontendTenantInitRole> roles,
        List<FrontendTenantInitDepartment> departments,
        List<Long> menuIds,
        List<String> dictionaryIds
    ) {
    }

    public record FrontendTenantPackageRecord(
        String id,
        String code,
        String name,
        boolean status,
        Integer maxUsers,
        Integer maxRoles,
        Integer maxMenus,
        Map<String, Object> featureFlags,
        String remark,
        String createdAt,
        String updatedAt
    ) {
    }

    public record FrontendTenantPackageMenuAssignment(List<Long> menuIds) {
    }

    public record FrontendFileInfo(String name, String url, String relativePath) {
    }

    @Getter
    @Setter
    public static class FrontendTenantCreateRequest {

        @NotBlank
        private String code;

        @NotBlank
        private String name;

        private String packageId;

        private String status;

        private String expiresAt;

        private String contactName;

        private String contactPhone;

        private String contactEmail;

        private String remark;

        private String adminUsername;

        @NotBlank
        private String adminPassword;

        private String adminNickname;
    }

    @Getter
    @Setter
    public static class FrontendTenantUpdateRequest {

        private String name;

        private String packageId;

        private String status;

        private String expiresAt;

        private String contactName;

        private String contactPhone;

        private String contactEmail;

        private String remark;
    }

    @Getter
    @Setter
    public static class FrontendTenantPackageCreateRequest {

        @NotBlank
        private String code;

        @NotBlank
        private String name;

        private Boolean status;

        private Integer maxUsers;

        private Integer maxRoles;

        private Integer maxMenus;

        private Map<String, Object> featureFlags = Map.of();

        private String remark;
    }

    @Getter
    @Setter
    public static class FrontendTenantPackageUpdateRequest {

        private String code;

        private String name;

        private Boolean status;

        private Integer maxUsers;

        private Integer maxRoles;

        private Integer maxMenus;

        private Map<String, Object> featureFlags;

        private String remark;
    }

    @Getter
    @Setter
    public static class TenantPackageMetaPayload {

        private String code;

        private Integer maxUsers;

        private Integer maxRoles;

        private Integer maxMenus;

        private Map<String, Object> featureFlags = Map.of();

        private List<Long> menuIds = List.of();

        private String legacyRemark;
    }
}
