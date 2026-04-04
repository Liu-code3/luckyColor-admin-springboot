package com.luckycolor.admin.modules.frontend.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.config.ConditionalOnPersistenceEnabled;
import com.luckycolor.admin.common.page.PageQuery;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.modules.system.config.dataobject.SystemConfigDO;
import com.luckycolor.admin.modules.system.config.mapper.SystemConfigMapper;
import com.luckycolor.admin.modules.system.dictionary.cache.service.DictionaryCatalogCacheService;
import com.luckycolor.admin.modules.system.dictionary.item.dataobject.DictionaryItemDO;
import com.luckycolor.admin.modules.system.dictionary.item.mapper.DictionaryItemMapper;
import com.luckycolor.admin.modules.system.dictionary.item.service.DictionaryItemService;
import com.luckycolor.admin.modules.system.dictionary.item.web.request.DictionaryItemSaveRequest;
import com.luckycolor.admin.modules.system.dictionary.type.dataobject.DictionaryTypeDO;
import com.luckycolor.admin.modules.system.dictionary.type.mapper.DictionaryTypeMapper;
import com.luckycolor.admin.modules.system.dictionary.type.service.DictionaryTypeService;
import com.luckycolor.admin.modules.system.dictionary.type.web.request.DictionaryTypeSaveRequest;
import com.luckycolor.admin.modules.system.notice.dataobject.NoticeDO;
import com.luckycolor.admin.modules.system.notice.mapper.NoticeMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Validated
@ConditionalOnPersistenceEnabled
public class FrontendContentCompatibilityController {

    private static final int ENABLED = 0;

    private static final int DISABLED = 1;

    private static final int DRAFT = 0;

    private static final int PUBLISHED = 1;

    private static final String META_PREFIX = "LC_META:";

    private final SystemConfigMapper systemConfigMapper;
    private final DictionaryTypeService dictionaryTypeService;
    private final DictionaryTypeMapper dictionaryTypeMapper;
    private final DictionaryItemService dictionaryItemService;
    private final DictionaryItemMapper dictionaryItemMapper;
    private final DictionaryCatalogCacheService dictionaryCatalogCacheService;
    private final NoticeMapper noticeMapper;
    private final DataScopeConditionBuilder dataScopeConditionBuilder;
    private final ObjectMapper objectMapper;

    public FrontendContentCompatibilityController(
        SystemConfigMapper systemConfigMapper,
        DictionaryTypeService dictionaryTypeService,
        DictionaryTypeMapper dictionaryTypeMapper,
        DictionaryItemService dictionaryItemService,
        DictionaryItemMapper dictionaryItemMapper,
        DictionaryCatalogCacheService dictionaryCatalogCacheService,
        NoticeMapper noticeMapper,
        DataScopeConditionBuilder dataScopeConditionBuilder,
        ObjectMapper objectMapper
    ) {
        this.systemConfigMapper = systemConfigMapper;
        this.dictionaryTypeService = dictionaryTypeService;
        this.dictionaryTypeMapper = dictionaryTypeMapper;
        this.dictionaryItemService = dictionaryItemService;
        this.dictionaryItemMapper = dictionaryItemMapper;
        this.dictionaryCatalogCacheService = dictionaryCatalogCacheService;
        this.noticeMapper = noticeMapper;
        this.dataScopeConditionBuilder = dataScopeConditionBuilder;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/configs")
    @RequirePermission("system:config:query")
    public ApiResponse<FrontendPageResult<FrontendConfigRecord>> pageConfigs(
        @RequestParam(value = "page", required = false) Long page,
        @RequestParam(value = "size", required = false) Long size,
        @RequestParam(value = "keyword", required = false) String keyword
    ) {
        PageQuery pageQuery = buildPageQuery(page, size);
        LambdaQueryWrapper<SystemConfigDO> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String value = keyword.trim();
            queryWrapper.and(wrapper -> wrapper
                .like(SystemConfigDO::getConfigKey, value)
                .or()
                .like(SystemConfigDO::getConfigName, value));
        }
        queryWrapper.select(
            SystemConfigDO::getId,
            SystemConfigDO::getTenantId,
            SystemConfigDO::getConfigKey,
            SystemConfigDO::getConfigName,
            SystemConfigDO::getConfigValue,
            SystemConfigDO::getStatus,
            SystemConfigDO::getSort,
            SystemConfigDO::getRemark,
            SystemConfigDO::getCreateTime,
            SystemConfigDO::getUpdateTime
        );
        dataScopeConditionBuilder.applyCurrentScope(queryWrapper, SystemConfigDO::getTenantId, null);
        queryWrapper.orderByAsc(SystemConfigDO::getSort)
            .orderByDesc(SystemConfigDO::getUpdateTime)
            .orderByDesc(SystemConfigDO::getCreateTime);
        PageResult<SystemConfigDO> pageResult = systemConfigMapper.selectPageResult(pageQuery, queryWrapper);
        List<FrontendConfigRecord> records = pageResult.getList().stream()
            .map(this::toFrontendConfig)
            .toList();
        return ApiResponse.success(toFrontendPage(pageQuery, pageResult.getTotal(), records));
    }

    @GetMapping("/configs/{id}")
    @RequirePermission("system:config:query")
    public ApiResponse<FrontendConfigRecord> getConfig(@PathVariable Long id) {
        return ApiResponse.success(toFrontendConfig(getRequiredConfig(id)));
    }

    @PostMapping("/configs")
    @RequirePermission("system:config:create")
    public ApiResponse<FrontendConfigRecord> createConfig(@Valid @RequestBody FrontendConfigUpsertRequest request) {
        String configKey = request.getConfigKey().trim();
        ensureConfigKeyUnique(null, configKey);
        SystemConfigDO systemConfig = new SystemConfigDO();
        systemConfig.setConfigKey(configKey);
        systemConfig.setConfigName(request.getConfigName().trim());
        systemConfig.setConfigValue(request.getConfigValue().trim());
        systemConfig.setSensitive(0);
        systemConfig.setStatus(toNativeStatus(request.getStatus(), true));
        systemConfig.setSort(10);
        systemConfig.setRemark(emptyToNull(request.getRemark()));
        systemConfigMapper.insert(systemConfig);
        return ApiResponse.success(toFrontendConfig(getRequiredConfig(systemConfig.getId())));
    }

    @PatchMapping("/configs/{id}")
    @RequirePermission("system:config:update")
    public ApiResponse<FrontendConfigRecord> updateConfig(
        @PathVariable Long id,
        @RequestBody FrontendConfigPatchRequest request
    ) {
        SystemConfigDO current = getRequiredConfig(id);
        String configKey = resolveString(request.getConfigKey(), current.getConfigKey());
        ensureConfigKeyUnique(id, configKey);
        current.setConfigKey(configKey);
        current.setConfigName(resolveString(request.getConfigName(), current.getConfigName()));
        current.setConfigValue(resolveString(request.getConfigValue(), current.getConfigValue()));
        current.setSensitive(0);
        current.setStatus(request.getStatus() != null
            ? toNativeStatus(request.getStatus(), true)
            : defaultInteger(current.getStatus(), ENABLED));
        current.setRemark(request.getRemark() != null ? emptyToNull(request.getRemark()) : current.getRemark());
        systemConfigMapper.updateById(current);
        return ApiResponse.success(toFrontendConfig(getRequiredConfig(id)));
    }

    @DeleteMapping("/configs/{id}")
    @RequirePermission("system:config:update")
    public ApiResponse<Boolean> deleteConfig(@PathVariable Long id) {
        getRequiredConfig(id);
        systemConfigMapper.deleteById(id);
        return ApiResponse.success(true);
    }

    @PostMapping("/configs/refresh-cache")
    @RequirePermission("system:config:update")
    public ApiResponse<FrontendCacheRefreshResult> refreshConfigCache() {
        LambdaQueryWrapper<SystemConfigDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemConfigDO::getStatus, ENABLED);
        dataScopeConditionBuilder.applyCurrentScope(queryWrapper, SystemConfigDO::getTenantId, null);
        long count = Optional.ofNullable(systemConfigMapper.selectCount(queryWrapper)).orElse(0L);
        return ApiResponse.success(new FrontendCacheRefreshResult(
            "system:configs:cache",
            count,
            toIsoInstant(LocalDateTime.now())
        ));
    }

    @GetMapping("/dict/tree")
    @RequirePermission("system:dictionary:query")
    public ApiResponse<List<FrontendDictionaryRecord>> treeDictionary() {
        return ApiResponse.success(buildDictionaryTree(loadDictionaryTypes(), loadDictionaryItems()));
    }

    @GetMapping("/dict/page")
    @RequirePermission("system:dictionary:query")
    public ApiResponse<FrontendPageResult<FrontendDictionaryRecord>> pageDictionary(
        @RequestParam(value = "page", required = false) Long page,
        @RequestParam(value = "size", required = false) Long size,
        @RequestParam(value = "id", required = false) String selectedId,
        @RequestParam(value = "searchKey", required = false) String searchKey
    ) {
        PageQuery pageQuery = buildPageQuery(page, size);
        List<FrontendDictionaryRecord> tree = buildDictionaryTree(loadDictionaryTypes(), loadDictionaryItems());
        List<FrontendDictionaryRecord> flattened = flattenDictionaryTree(tree);
        if (StringUtils.hasText(selectedId)) {
            flattened = collectDictionarySubtree(flattened, selectedId.trim());
        }
        if (StringUtils.hasText(searchKey)) {
            String keyword = searchKey.trim().toLowerCase(Locale.ROOT);
            flattened = flattened.stream()
                .filter(record -> matchesDictionaryKeyword(record, keyword))
                .toList();
        }
        return ApiResponse.success(toFrontendPage(pageQuery, flattened));
    }

    @GetMapping("/dict/{id}")
    @RequirePermission("system:dictionary:query")
    public ApiResponse<FrontendDictionaryRecord> getDictionary(@PathVariable String id) {
        return ApiResponse.success(toFrontendDictionary(resolveDictionaryEntity(id)));
    }

    @PostMapping("/dict")
    @RequirePermission("system:dictionary:create")
    public ApiResponse<FrontendDictionaryRecord> createDictionary(
        @Valid @RequestBody FrontendDictionaryUpsertRequest request
    ) {
        if (!StringUtils.hasText(request.getParentId()) || "0".equals(request.getParentId().trim())) {
            DictionaryTypeSaveRequest nativeRequest = new DictionaryTypeSaveRequest();
            nativeRequest.setTypeCode(request.getDictValue().trim());
            nativeRequest.setTypeName(request.getDictLabel().trim());
            nativeRequest.setStatus(toNativeStatus(request.getStatus(), true));
            nativeRequest.setSort(request.getSortCode());
            nativeRequest.setRemark(writeDictionaryMeta(new DictionaryCompatMeta(
                emptyToNull(request.getName()),
                emptyToNull(request.getCategory()),
                request.getWeight(),
                emptyToNull(request.getDeleteFlag()),
                null
            )));
            Long id = dictionaryTypeService.createDictionaryType(nativeRequest);
            return ApiResponse.success(toFrontendDictionary(DictionaryEntity.type(getRequiredDictionaryType(id))));
        }
        DictionaryParentResolution parentResolution = resolveDictionaryParent(request.getParentId());
        DictionaryItemSaveRequest nativeRequest = new DictionaryItemSaveRequest();
        nativeRequest.setTypeCode(parentResolution.typeCode());
        nativeRequest.setParentId(parentResolution.nativeParentId());
        nativeRequest.setItemLabel(request.getDictLabel().trim());
        nativeRequest.setItemValue(request.getDictValue().trim());
        nativeRequest.setItemTag(null);
        nativeRequest.setSort(request.getSortCode());
        nativeRequest.setStatus(toNativeStatus(request.getStatus(), true));
        nativeRequest.setRemark(writeDictionaryMeta(new DictionaryCompatMeta(
            emptyToNull(request.getName()),
            emptyToNull(request.getCategory()),
            request.getWeight(),
            emptyToNull(request.getDeleteFlag()),
            null
        )));
        Long id = dictionaryItemService.createDictionaryItem(nativeRequest);
        return ApiResponse.success(toFrontendDictionary(DictionaryEntity.item(getRequiredDictionaryItem(id))));
    }

    @PatchMapping("/dict/{id}")
    @RequirePermission("system:dictionary:update")
    public ApiResponse<FrontendDictionaryRecord> updateDictionary(
        @PathVariable String id,
        @RequestBody FrontendDictionaryPatchRequest request
    ) {
        DictionaryEntity entity = resolveDictionaryEntity(id);
        if (entity.isType()) {
            return ApiResponse.success(updateDictionaryType(entity.type(), request));
        }
        return ApiResponse.success(updateDictionaryItem(entity.item(), request));
    }

    @DeleteMapping("/dict/{id}")
    @RequirePermission("system:dictionary:delete")
    public ApiResponse<Boolean> deleteDictionary(@PathVariable String id) {
        DictionaryEntity entity = resolveDictionaryEntity(id);
        if (entity.isType()) {
            deleteDictionaryTypeCascade(entity.type());
        } else {
            deleteDictionaryItemCascade(entity.item());
        }
        return ApiResponse.success(true);
    }

    @PostMapping("/dict/refresh-cache")
    @RequirePermission("system:dictionary:refresh-cache")
    public ApiResponse<FrontendCacheRefreshResult> refreshDictionaryCache() {
        int count = dictionaryCatalogCacheService.refresh(null);
        return ApiResponse.success(new FrontendCacheRefreshResult(
            "system:dictionaries:tree",
            count,
            toIsoInstant(LocalDateTime.now())
        ));
    }

    @GetMapping("/notices")
    @RequirePermission("system:notice:query")
    public ApiResponse<FrontendPageResult<FrontendNoticeRecord>> pageNotices(
        @RequestParam(value = "page", required = false) Long page,
        @RequestParam(value = "size", required = false) Long size,
        @RequestParam(value = "keyword", required = false) String keyword
    ) {
        return ApiResponse.success(toFrontendPage(buildPageQuery(page, size), loadNoticeRecords(keyword)));
    }

    @GetMapping("/notices/{id}")
    @RequirePermission("system:notice:query")
    public ApiResponse<FrontendNoticeRecord> getNotice(@PathVariable Long id) {
        return ApiResponse.success(toFrontendNotice(getRequiredNotice(id)));
    }

    @PostMapping("/notices")
    @RequirePermission("system:notice:create")
    public ApiResponse<FrontendNoticeRecord> createNotice(@Valid @RequestBody FrontendNoticeUpsertRequest request) {
        NoticeDO notice = new NoticeDO();
        fillNotice(notice, toNoticePatch(request), readNoticeMeta(null));
        noticeMapper.insert(notice);
        return ApiResponse.success(toFrontendNotice(getRequiredNotice(notice.getId())));
    }

    @PatchMapping("/notices/{id}")
    @RequirePermission("system:notice:update")
    public ApiResponse<FrontendNoticeRecord> updateNotice(
        @PathVariable Long id,
        @RequestBody FrontendNoticePatchRequest request
    ) {
        NoticeDO notice = getRequiredNotice(id);
        fillNotice(notice, mergeNoticePatch(notice, request), readNoticeMeta(notice.getRemark()));
        noticeMapper.updateById(notice);
        return ApiResponse.success(toFrontendNotice(getRequiredNotice(id)));
    }

    @PatchMapping("/notices/{id}/publish")
    @RequirePermission("system:notice:publish")
    public ApiResponse<FrontendNoticeRecord> publishNotice(
        @PathVariable Long id,
        @RequestBody(required = false) FrontendNoticePublishRequest request
    ) {
        NoticeDO notice = getRequiredNotice(id);
        NoticeCompatMeta meta = readNoticeMeta(notice.getRemark());
        if (request != null && request.getPublisher() != null) {
            meta = new NoticeCompatMeta(emptyToNull(request.getPublisher()), meta.pinned(), meta.legacyRemark());
        }
        notice.setPublishStatus(PUBLISHED);
        notice.setPublishTime(resolvePublishedAt(request == null ? null : request.getPublishedAt(), true));
        notice.setRemark(writeNoticeMeta(meta));
        noticeMapper.updateById(notice);
        return ApiResponse.success(toFrontendNotice(getRequiredNotice(id)));
    }

    @PatchMapping("/notices/{id}/revoke")
    @RequirePermission("system:notice:publish")
    public ApiResponse<FrontendNoticeRecord> revokeNotice(@PathVariable Long id) {
        NoticeDO notice = getRequiredNotice(id);
        notice.setPublishStatus(DRAFT);
        notice.setPublishTime(null);
        noticeMapper.updateById(notice);
        return ApiResponse.success(toFrontendNotice(getRequiredNotice(id)));
    }

    @PatchMapping("/notices/{id}/pin")
    @RequirePermission("system:notice:update")
    public ApiResponse<FrontendNoticeRecord> pinNotice(
        @PathVariable Long id,
        @RequestBody FrontendNoticePinRequest request
    ) {
        NoticeDO notice = getRequiredNotice(id);
        NoticeCompatMeta meta = readNoticeMeta(notice.getRemark());
        notice.setRemark(writeNoticeMeta(new NoticeCompatMeta(meta.publisher(), request.getPinned(), meta.legacyRemark())));
        noticeMapper.updateById(notice);
        return ApiResponse.success(toFrontendNotice(getRequiredNotice(id)));
    }

    @DeleteMapping("/notices/{id}")
    @RequirePermission("system:notice:update")
    public ApiResponse<Boolean> deleteNotice(@PathVariable Long id) {
        getRequiredNotice(id);
        noticeMapper.deleteById(id);
        return ApiResponse.success(true);
    }

    private PageQuery buildPageQuery(Long page, Long size) {
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNo(page);
        pageQuery.setPageSize(size);
        return pageQuery;
    }

    private <T> FrontendPageResult<T> toFrontendPage(PageQuery pageQuery, long total, List<T> records) {
        return new FrontendPageResult<>(total, pageQuery.resolvePageNo(), pageQuery.resolvePageSize(), records);
    }

    private <T> FrontendPageResult<T> toFrontendPage(PageQuery pageQuery, List<T> records) {
        List<T> safeRecords = records == null ? List.of() : records;
        long pageNo = pageQuery.resolvePageNo();
        long pageSize = pageQuery.resolvePageSize();
        int fromIndex = (int) Math.min((pageNo - 1) * pageSize, safeRecords.size());
        int toIndex = (int) Math.min(fromIndex + pageSize, safeRecords.size());
        return new FrontendPageResult<>(safeRecords.size(), pageNo, pageSize, safeRecords.subList(fromIndex, toIndex));
    }

    private FrontendConfigRecord toFrontendConfig(SystemConfigDO systemConfig) {
        return new FrontendConfigRecord(
            String.valueOf(systemConfig.getId()),
            stringify(systemConfig.getTenantId()),
            null,
            systemConfig.getConfigKey(),
            systemConfig.getConfigName(),
            systemConfig.getConfigValue(),
            deriveConfigValueType(systemConfig.getConfigValue()),
            isEnabled(systemConfig.getStatus()),
            systemConfig.getRemark(),
            toIsoInstant(systemConfig.getCreateTime()),
            toIsoInstant(systemConfig.getUpdateTime())
        );
    }

    private String deriveConfigValueType(String configValue) {
        if (!StringUtils.hasText(configValue)) {
            return "string";
        }
        String value = configValue.trim();
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return "boolean";
        }
        if ((value.startsWith("{") && value.endsWith("}")) || (value.startsWith("[") && value.endsWith("]"))) {
            return "json";
        }
        try {
            Double.parseDouble(value);
            return "number";
        } catch (NumberFormatException ignored) {
            return "string";
        }
    }

    private List<DictionaryTypeDO> loadDictionaryTypes() {
        LambdaQueryWrapper<DictionaryTypeDO> queryWrapper = new LambdaQueryWrapper<>();
        dataScopeConditionBuilder.applyCurrentScope(queryWrapper, DictionaryTypeDO::getTenantId, null);
        queryWrapper.orderByAsc(DictionaryTypeDO::getSort)
            .orderByAsc(DictionaryTypeDO::getId);
        return dictionaryTypeMapper.selectList(queryWrapper);
    }

    private List<DictionaryItemDO> loadDictionaryItems() {
        LambdaQueryWrapper<DictionaryItemDO> queryWrapper = new LambdaQueryWrapper<>();
        dataScopeConditionBuilder.applyCurrentScope(queryWrapper, DictionaryItemDO::getTenantId, null);
        queryWrapper.orderByAsc(DictionaryItemDO::getTypeCode)
            .orderByAsc(DictionaryItemDO::getParentId)
            .orderByAsc(DictionaryItemDO::getSort)
            .orderByAsc(DictionaryItemDO::getId);
        return dictionaryItemMapper.selectList(queryWrapper);
    }

    private List<FrontendDictionaryRecord> buildDictionaryTree(
        List<DictionaryTypeDO> dictionaryTypes,
        List<DictionaryItemDO> dictionaryItems
    ) {
        Map<String, DictionaryTypeDO> typesByCode = dictionaryTypes.stream()
            .collect(Collectors.toMap(DictionaryTypeDO::getTypeCode, item -> item));
        Map<Long, List<DictionaryItemDO>> itemsByParentId = dictionaryItems.stream()
            .collect(Collectors.groupingBy(item -> defaultLong(item.getParentId(), 0L)));
        List<FrontendDictionaryRecord> tree = new ArrayList<>();
        for (DictionaryTypeDO dictionaryType : dictionaryTypes) {
            List<FrontendDictionaryRecord> rootItems = itemsByParentId.getOrDefault(0L, List.of()).stream()
                .filter(item -> Objects.equals(dictionaryType.getTypeCode(), item.getTypeCode()))
                .sorted(Comparator.comparing(DictionaryItemDO::getSort, Comparator.nullsLast(Integer::compareTo))
                    .thenComparing(DictionaryItemDO::getId, Comparator.nullsLast(Long::compareTo)))
                .map(item -> buildDictionaryItemNode(item, typesByCode.get(item.getTypeCode()), itemsByParentId))
                .toList();
            tree.add(toFrontendDictionaryType(dictionaryType, rootItems));
        }
        return tree;
    }

    private FrontendDictionaryRecord buildDictionaryItemNode(
        DictionaryItemDO dictionaryItem,
        DictionaryTypeDO dictionaryType,
        Map<Long, List<DictionaryItemDO>> itemsByParentId
    ) {
        List<FrontendDictionaryRecord> children = itemsByParentId.getOrDefault(dictionaryItem.getId(), List.of()).stream()
            .filter(item -> Objects.equals(item.getTypeCode(), dictionaryItem.getTypeCode()))
            .sorted(Comparator.comparing(DictionaryItemDO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(DictionaryItemDO::getId, Comparator.nullsLast(Long::compareTo)))
            .map(item -> buildDictionaryItemNode(item, dictionaryType, itemsByParentId))
            .toList();
        return toFrontendDictionaryItem(dictionaryItem, dictionaryType, children);
    }

    private List<FrontendDictionaryRecord> flattenDictionaryTree(List<FrontendDictionaryRecord> tree) {
        List<FrontendDictionaryRecord> flattened = new ArrayList<>();
        ArrayDeque<FrontendDictionaryRecord> stack = new ArrayDeque<>(tree);
        while (!stack.isEmpty()) {
            FrontendDictionaryRecord current = stack.removeFirst();
            flattened.add(current.withoutChildren());
            if (current.children() != null && !current.children().isEmpty()) {
                List<FrontendDictionaryRecord> children = current.children();
                for (int index = children.size() - 1; index >= 0; index--) {
                    stack.addFirst(children.get(index));
                }
            }
        }
        return flattened;
    }

    private List<FrontendDictionaryRecord> collectDictionarySubtree(List<FrontendDictionaryRecord> records, String selectedId) {
        Map<String, FrontendDictionaryRecord> byId = records.stream()
            .collect(Collectors.toMap(FrontendDictionaryRecord::id, item -> item, (left, right) -> left, LinkedHashMap::new));
        if (!byId.containsKey(selectedId)) {
            return List.of();
        }
        List<FrontendDictionaryRecord> results = new ArrayList<>();
        ArrayDeque<String> queue = new ArrayDeque<>();
        queue.add(selectedId);
        while (!queue.isEmpty()) {
            String currentId = queue.removeFirst();
            FrontendDictionaryRecord current = byId.get(currentId);
            if (current == null) {
                continue;
            }
            results.add(current);
            records.stream()
                .filter(item -> currentId.equals(item.parentId()))
                .map(FrontendDictionaryRecord::id)
                .forEach(queue::addLast);
        }
        return results;
    }

    private boolean matchesDictionaryKeyword(FrontendDictionaryRecord record, String keyword) {
        return containsIgnoreCase(record.name(), keyword)
            || containsIgnoreCase(record.dictLabel(), keyword)
            || containsIgnoreCase(record.dictValue(), keyword)
            || containsIgnoreCase(record.category(), keyword);
    }

    private FrontendDictionaryRecord updateDictionaryType(
        DictionaryTypeDO current,
        FrontendDictionaryPatchRequest request
    ) {
        DictionaryCompatMeta currentMeta = readDictionaryMeta(current.getRemark());
        DictionaryTypeSaveRequest nativeRequest = new DictionaryTypeSaveRequest();
        nativeRequest.setTypeCode(resolveString(request.getDictValue(), current.getTypeCode()));
        nativeRequest.setTypeName(resolveString(request.getDictLabel(), current.getTypeName()));
        nativeRequest.setStatus(request.getStatus() != null
            ? toNativeStatus(request.getStatus(), true)
            : defaultInteger(current.getStatus(), ENABLED));
        nativeRequest.setSort(request.getSortCode() != null ? request.getSortCode() : defaultInteger(current.getSort(), 0));
        nativeRequest.setRemark(writeDictionaryMeta(new DictionaryCompatMeta(
            request.getName() != null ? emptyToNull(request.getName()) : currentMeta.name(),
            request.getCategory() != null ? emptyToNull(request.getCategory()) : currentMeta.category(),
            request.getWeight() != null ? request.getWeight() : currentMeta.weight(),
            request.getDeleteFlag() != null ? emptyToNull(request.getDeleteFlag()) : currentMeta.deleteFlag(),
            currentMeta.legacyRemark()
        )));
        dictionaryTypeService.updateDictionaryType(current.getId(), nativeRequest);
        return toFrontendDictionary(DictionaryEntity.type(getRequiredDictionaryType(current.getId())));
    }

    private FrontendDictionaryRecord updateDictionaryItem(
        DictionaryItemDO current,
        FrontendDictionaryPatchRequest request
    ) {
        DictionaryCompatMeta currentMeta = readDictionaryMeta(current.getRemark());
        DictionaryParentResolution parentResolution = request.getParentId() != null
            ? resolveDictionaryParent(request.getParentId())
            : new DictionaryParentResolution(current.getTypeCode(), defaultLong(current.getParentId(), 0L));
        DictionaryItemSaveRequest nativeRequest = new DictionaryItemSaveRequest();
        nativeRequest.setTypeCode(parentResolution.typeCode());
        nativeRequest.setParentId(parentResolution.nativeParentId());
        nativeRequest.setItemLabel(resolveString(request.getDictLabel(), current.getItemLabel()));
        nativeRequest.setItemValue(resolveString(request.getDictValue(), current.getItemValue()));
        nativeRequest.setItemTag(current.getItemTag());
        nativeRequest.setSort(request.getSortCode() != null ? request.getSortCode() : defaultInteger(current.getSort(), 0));
        nativeRequest.setStatus(request.getStatus() != null
            ? toNativeStatus(request.getStatus(), true)
            : defaultInteger(current.getStatus(), ENABLED));
        nativeRequest.setRemark(writeDictionaryMeta(new DictionaryCompatMeta(
            request.getName() != null ? emptyToNull(request.getName()) : currentMeta.name(),
            request.getCategory() != null ? emptyToNull(request.getCategory()) : currentMeta.category(),
            request.getWeight() != null ? request.getWeight() : currentMeta.weight(),
            request.getDeleteFlag() != null ? emptyToNull(request.getDeleteFlag()) : currentMeta.deleteFlag(),
            currentMeta.legacyRemark()
        )));
        dictionaryItemService.updateDictionaryItem(current.getId(), nativeRequest);
        return toFrontendDictionary(DictionaryEntity.item(getRequiredDictionaryItem(current.getId())));
    }

    private FrontendDictionaryRecord toFrontendDictionary(DictionaryEntity entity) {
        if (entity.isType()) {
            DictionaryTypeDO dictionaryType = entity.type();
            return buildDictionaryTree(List.of(dictionaryType), loadDictionaryItems()).stream()
                .findFirst()
                .orElseGet(() -> toFrontendDictionaryType(dictionaryType, List.of()));
        }
        DictionaryItemDO dictionaryItem = entity.item();
        return toFrontendDictionaryItem(dictionaryItem, getRequiredDictionaryTypeByCode(dictionaryItem.getTypeCode()), List.of());
    }

    private FrontendDictionaryRecord toFrontendDictionaryType(
        DictionaryTypeDO dictionaryType,
        List<FrontendDictionaryRecord> children
    ) {
        DictionaryCompatMeta meta = readDictionaryMeta(dictionaryType.getRemark());
        return new FrontendDictionaryRecord(
            encodeTypeId(dictionaryType.getId()),
            "0",
            defaultInteger(meta.weight(), defaultInteger(dictionaryType.getSort(), 0)),
            defaultString(meta.name(), dictionaryType.getTypeName()),
            stringify(dictionaryType.getTenantId()),
            dictionaryType.getTypeName(),
            dictionaryType.getTypeCode(),
            defaultString(meta.category(), "BIZ"),
            defaultInteger(dictionaryType.getSort(), 0),
            isEnabled(dictionaryType.getStatus()),
            defaultString(meta.deleteFlag(), "NOT_DELETE"),
            toIsoInstant(dictionaryType.getCreateTime()),
            emptyToNull(dictionaryType.getCreateBy()),
            toIsoInstant(dictionaryType.getUpdateTime()),
            emptyToNull(dictionaryType.getUpdateBy()),
            children
        );
    }

    private FrontendDictionaryRecord toFrontendDictionaryItem(
        DictionaryItemDO dictionaryItem,
        DictionaryTypeDO dictionaryType,
        List<FrontendDictionaryRecord> children
    ) {
        DictionaryCompatMeta meta = readDictionaryMeta(dictionaryItem.getRemark());
        return new FrontendDictionaryRecord(
            encodeItemId(dictionaryItem.getId()),
            defaultLong(dictionaryItem.getParentId(), 0L) == 0L
                ? encodeTypeId(dictionaryType.getId())
                : encodeItemId(dictionaryItem.getParentId()),
            defaultInteger(meta.weight(), defaultInteger(dictionaryItem.getSort(), 0)),
            defaultString(meta.name(), dictionaryItem.getItemLabel()),
            stringify(dictionaryItem.getTenantId()),
            dictionaryItem.getItemLabel(),
            dictionaryItem.getItemValue(),
            defaultString(meta.category(), "BIZ"),
            defaultInteger(dictionaryItem.getSort(), 0),
            isEnabled(dictionaryItem.getStatus()),
            defaultString(meta.deleteFlag(), "NOT_DELETE"),
            toIsoInstant(dictionaryItem.getCreateTime()),
            emptyToNull(dictionaryItem.getCreateBy()),
            toIsoInstant(dictionaryItem.getUpdateTime()),
            emptyToNull(dictionaryItem.getUpdateBy()),
            children
        );
    }

    private DictionaryEntity resolveDictionaryEntity(String encodedId) {
        DictionaryRef ref = parseDictionaryRef(encodedId);
        return ref.kind() == DictionaryKind.TYPE
            ? DictionaryEntity.type(getRequiredDictionaryType(ref.id()))
            : DictionaryEntity.item(getRequiredDictionaryItem(ref.id()));
    }

    private DictionaryParentResolution resolveDictionaryParent(String encodedParentId) {
        if (!StringUtils.hasText(encodedParentId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dictionary parent id is invalid");
        }
        String normalized = encodedParentId.trim();
        if ("0".equals(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dictionary parent id is invalid");
        }
        DictionaryRef ref = parseDictionaryRef(normalized);
        if (ref.kind() == DictionaryKind.TYPE) {
            DictionaryTypeDO dictionaryType = getRequiredDictionaryType(ref.id());
            return new DictionaryParentResolution(dictionaryType.getTypeCode(), 0L);
        }
        DictionaryItemDO dictionaryItem = getRequiredDictionaryItem(ref.id());
        return new DictionaryParentResolution(dictionaryItem.getTypeCode(), dictionaryItem.getId());
    }

    private DictionaryRef parseDictionaryRef(String encodedId) {
        int separator = encodedId.indexOf(':');
        if (separator < 1 || separator >= encodedId.length() - 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dictionary id is invalid");
        }
        String kind = encodedId.substring(0, separator);
        String rawId = encodedId.substring(separator + 1);
        try {
            Long id = Long.valueOf(rawId);
            return switch (kind) {
                case "type" -> new DictionaryRef(DictionaryKind.TYPE, id);
                case "item" -> new DictionaryRef(DictionaryKind.ITEM, id);
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dictionary id is invalid");
            };
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dictionary id is invalid", exception);
        }
    }

    private String encodeTypeId(Long id) {
        return "type:" + id;
    }

    private String encodeItemId(Long id) {
        return "item:" + id;
    }

    private void deleteDictionaryTypeCascade(DictionaryTypeDO dictionaryType) {
        LambdaQueryWrapper<DictionaryItemDO> itemQuery = new LambdaQueryWrapper<>();
        itemQuery.eq(DictionaryItemDO::getTypeCode, dictionaryType.getTypeCode());
        List<DictionaryItemDO> items = dictionaryItemMapper.selectList(itemQuery);
        if (!items.isEmpty()) {
            dictionaryItemMapper.deleteByIds(items.stream().map(DictionaryItemDO::getId).toList());
        }
        dictionaryTypeMapper.deleteById(dictionaryType.getId());
        dictionaryCatalogCacheService.evict(List.of(dictionaryType.getTypeCode()));
    }

    private void deleteDictionaryItemCascade(DictionaryItemDO dictionaryItem) {
        List<DictionaryItemDO> items = dictionaryItemMapper.selectList(new LambdaQueryWrapper<DictionaryItemDO>()
            .eq(DictionaryItemDO::getTypeCode, dictionaryItem.getTypeCode())
            .orderByAsc(DictionaryItemDO::getParentId)
            .orderByAsc(DictionaryItemDO::getId));
        ArrayDeque<Long> queue = new ArrayDeque<>();
        List<Long> ids = new ArrayList<>();
        queue.add(dictionaryItem.getId());
        while (!queue.isEmpty()) {
            Long currentId = queue.removeFirst();
            ids.add(currentId);
            items.stream()
                .filter(item -> Objects.equals(defaultLong(item.getParentId(), 0L), currentId))
                .map(DictionaryItemDO::getId)
                .forEach(queue::addLast);
        }
        dictionaryItemMapper.deleteByIds(ids);
        dictionaryCatalogCacheService.evict(List.of(dictionaryItem.getTypeCode()));
    }

    private List<FrontendNoticeRecord> loadNoticeRecords(String keyword) {
        LambdaQueryWrapper<NoticeDO> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String value = keyword.trim();
            queryWrapper.and(wrapper -> wrapper
                .like(NoticeDO::getNoticeTitle, value)
                .or()
                .like(NoticeDO::getNoticeContent, value));
        }
        dataScopeConditionBuilder.applyCurrentScope(queryWrapper, NoticeDO::getTenantId, null);
        queryWrapper.orderByDesc(NoticeDO::getUpdateTime)
            .orderByDesc(NoticeDO::getCreateTime);
        return noticeMapper.selectList(queryWrapper).stream()
            .map(this::toFrontendNotice)
            .sorted(Comparator.comparing(FrontendNoticeRecord::isPinned).reversed()
                .thenComparing(record -> record.publishedAt() == null ? "" : record.publishedAt(), Comparator.reverseOrder())
                .thenComparing(record -> record.updatedAt() == null ? "" : record.updatedAt(), Comparator.reverseOrder()))
            .toList();
    }

    private FrontendNoticePatchRequest toNoticePatch(FrontendNoticeUpsertRequest request) {
        FrontendNoticePatchRequest patchRequest = new FrontendNoticePatchRequest();
        patchRequest.setTitle(request.getTitle());
        patchRequest.setContent(request.getContent());
        patchRequest.setType(request.getType());
        patchRequest.setStatus(request.getStatus());
        patchRequest.setPublisher(request.getPublisher());
        patchRequest.setPublishedAt(request.getPublishedAt());
        return patchRequest;
    }

    private FrontendNoticePatchRequest mergeNoticePatch(NoticeDO notice, FrontendNoticePatchRequest request) {
        NoticeCompatMeta meta = readNoticeMeta(notice.getRemark());
        FrontendNoticePatchRequest merged = new FrontendNoticePatchRequest();
        merged.setTitle(resolveString(request.getTitle(), notice.getNoticeTitle()));
        merged.setContent(resolveString(request.getContent(), notice.getNoticeContent()));
        merged.setType(resolveString(request.getType(), notice.getNoticeType()));
        merged.setStatus(request.getStatus() != null ? request.getStatus() : notice.getPublishStatus() == PUBLISHED);
        merged.setPublisher(request.getPublisher() != null ? request.getPublisher() : meta.publisher());
        merged.setPublishedAt(request.getPublishedAt() != null ? request.getPublishedAt() : toIsoInstant(notice.getPublishTime()));
        return merged;
    }

    private void fillNotice(NoticeDO notice, FrontendNoticePatchRequest request, NoticeCompatMeta currentMeta) {
        notice.setNoticeTitle(request.getTitle().trim());
        notice.setNoticeContent(request.getContent().trim());
        notice.setNoticeType(request.getType().trim());
        notice.setPublishStatus(Boolean.TRUE.equals(request.getStatus()) ? PUBLISHED : DRAFT);
        notice.setPublishTime(resolvePublishedAt(request.getPublishedAt(), Boolean.TRUE.equals(request.getStatus())));
        notice.setSort(defaultInteger(notice.getSort(), 0));
        notice.setRemark(writeNoticeMeta(new NoticeCompatMeta(
            request.getPublisher() != null ? emptyToNull(request.getPublisher()) : currentMeta.publisher(),
            currentMeta.pinned(),
            currentMeta.legacyRemark()
        )));
    }

    private LocalDateTime resolvePublishedAt(String publishedAt, boolean published) {
        if (!StringUtils.hasText(publishedAt)) {
            return published ? LocalDateTime.now() : null;
        }
        return LocalDateTime.ofInstant(java.time.Instant.parse(publishedAt.trim()), ZoneOffset.UTC);
    }

    private FrontendNoticeRecord toFrontendNotice(NoticeDO notice) {
        NoticeCompatMeta meta = readNoticeMeta(notice.getRemark());
        return new FrontendNoticeRecord(
            String.valueOf(notice.getId()),
            stringify(notice.getTenantId()),
            null,
            notice.getNoticeTitle(),
            notice.getNoticeContent(),
            notice.getNoticeType(),
            notice.getPublishStatus() != null && notice.getPublishStatus() == PUBLISHED,
            Boolean.TRUE.equals(meta.pinned()),
            emptyToNull(meta.publisher()),
            toIsoInstant(notice.getPublishTime()),
            toIsoInstant(notice.getCreateTime()),
            toIsoInstant(notice.getUpdateTime())
        );
    }

    private void ensureConfigKeyUnique(Long currentId, String configKey) {
        LambdaQueryWrapper<SystemConfigDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemConfigDO::getConfigKey, configKey);
        if (currentId != null) {
            queryWrapper.ne(SystemConfigDO::getId, currentId);
        }
        Long count = systemConfigMapper.selectCount(queryWrapper);
        if (count != null && count > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "System config key already exists");
        }
    }

    private SystemConfigDO getRequiredConfig(Long id) {
        LambdaQueryWrapper<SystemConfigDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(
            SystemConfigDO::getId,
            SystemConfigDO::getTenantId,
            SystemConfigDO::getConfigKey,
            SystemConfigDO::getConfigName,
            SystemConfigDO::getConfigValue,
            SystemConfigDO::getStatus,
            SystemConfigDO::getSort,
            SystemConfigDO::getRemark,
            SystemConfigDO::getCreateTime,
            SystemConfigDO::getUpdateTime
        );
        queryWrapper.eq(SystemConfigDO::getId, id);
        SystemConfigDO systemConfig = systemConfigMapper.selectOne(queryWrapper);
        if (systemConfig == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Config not found");
        }
        return systemConfig;
    }

    private NoticeDO getRequiredNotice(Long id) {
        NoticeDO notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notice not found");
        }
        return notice;
    }

    private DictionaryTypeDO getRequiredDictionaryType(Long id) {
        DictionaryTypeDO dictionaryType = dictionaryTypeMapper.selectById(id);
        if (dictionaryType == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dictionary type not found");
        }
        return dictionaryType;
    }

    private DictionaryTypeDO getRequiredDictionaryTypeByCode(String typeCode) {
        LambdaQueryWrapper<DictionaryTypeDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DictionaryTypeDO::getTypeCode, typeCode);
        DictionaryTypeDO dictionaryType = dictionaryTypeMapper.selectList(queryWrapper).stream().findFirst().orElse(null);
        if (dictionaryType == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dictionary type not found");
        }
        return dictionaryType;
    }

    private DictionaryItemDO getRequiredDictionaryItem(Long id) {
        DictionaryItemDO dictionaryItem = dictionaryItemMapper.selectById(id);
        if (dictionaryItem == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dictionary item not found");
        }
        return dictionaryItem;
    }

    private NoticeCompatMeta readNoticeMeta(String remark) {
        if (!StringUtils.hasText(remark)) {
            return new NoticeCompatMeta(null, false, null);
        }
        if (!remark.startsWith(META_PREFIX)) {
            return new NoticeCompatMeta(null, false, remark);
        }
        try {
            NoticeCompatMeta meta = objectMapper.readValue(remark.substring(META_PREFIX.length()), NoticeCompatMeta.class);
            return new NoticeCompatMeta(meta.publisher(), Boolean.TRUE.equals(meta.pinned()), meta.legacyRemark());
        } catch (JsonProcessingException ignored) {
            return new NoticeCompatMeta(null, false, remark);
        }
    }

    private String writeNoticeMeta(NoticeCompatMeta meta) {
        if (!StringUtils.hasText(meta.publisher()) && !Boolean.TRUE.equals(meta.pinned()) && !StringUtils.hasText(meta.legacyRemark())) {
            return null;
        }
        try {
            return META_PREFIX + objectMapper.writeValueAsString(meta);
        } catch (JsonProcessingException ignored) {
            return meta.legacyRemark();
        }
    }

    private DictionaryCompatMeta readDictionaryMeta(String remark) {
        if (!StringUtils.hasText(remark)) {
            return new DictionaryCompatMeta(null, null, null, null, null);
        }
        if (!remark.startsWith(META_PREFIX)) {
            return new DictionaryCompatMeta(null, null, null, null, remark);
        }
        try {
            return objectMapper.readValue(remark.substring(META_PREFIX.length()), DictionaryCompatMeta.class);
        } catch (JsonProcessingException ignored) {
            return new DictionaryCompatMeta(null, null, null, null, remark);
        }
    }

    private String writeDictionaryMeta(DictionaryCompatMeta meta) {
        if (!StringUtils.hasText(meta.name())
            && !StringUtils.hasText(meta.category())
            && meta.weight() == null
            && !StringUtils.hasText(meta.deleteFlag())
            && !StringUtils.hasText(meta.legacyRemark())) {
            return null;
        }
        try {
            return META_PREFIX + objectMapper.writeValueAsString(meta);
        } catch (JsonProcessingException ignored) {
            return meta.legacyRemark();
        }
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private boolean isEnabled(Integer status) {
        return status == null || status == ENABLED;
    }

    private Integer toNativeStatus(Boolean status, boolean defaultValue) {
        boolean resolved = status == null ? defaultValue : status;
        return resolved ? ENABLED : DISABLED;
    }

    private Integer defaultInteger(Integer value, Integer fallback) {
        return value == null ? fallback : value;
    }

    private Long defaultLong(Long value, Long fallback) {
        return value == null ? fallback : value;
    }

    private String resolveString(String preferred, String fallback) {
        return preferred != null ? preferred.trim() : fallback;
    }

    private String defaultString(String preferred, String fallback) {
        return StringUtils.hasText(preferred) ? preferred : fallback;
    }

    private String emptyToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String stringify(Long value) {
        return value == null ? null : String.valueOf(value);
    }

    private String toIsoInstant(LocalDateTime value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC).toInstant().toString();
    }

    private record NoticeCompatMeta(String publisher, Boolean pinned, String legacyRemark) {
    }

    private record DictionaryCompatMeta(String name, String category, Integer weight, String deleteFlag, String legacyRemark) {
    }

    private record DictionaryParentResolution(String typeCode, Long nativeParentId) {
    }

    private enum DictionaryKind {
        TYPE,
        ITEM
    }

    private record DictionaryRef(DictionaryKind kind, Long id) {
    }

    private record DictionaryEntity(DictionaryTypeDO type, DictionaryItemDO item) {

        private static DictionaryEntity type(DictionaryTypeDO type) {
            return new DictionaryEntity(type, null);
        }

        private static DictionaryEntity item(DictionaryItemDO item) {
            return new DictionaryEntity(null, item);
        }

        private boolean isType() {
            return type != null;
        }
    }

    public record FrontendPageResult<T>(long total, long current, long size, List<T> records) {
    }

    public record FrontendCacheRefreshResult(String cacheKey, long count, String refreshedAt) {
    }

    public record FrontendConfigRecord(
        String id,
        String tenantId,
        String tenantName,
        String configKey,
        String configName,
        String configValue,
        String valueType,
        boolean status,
        String remark,
        String createdAt,
        String updatedAt
    ) {
    }

    public record FrontendDictionaryRecord(
        String id,
        String parentId,
        int weight,
        String name,
        String tenantId,
        String dictLabel,
        String dictValue,
        String category,
        int sortCode,
        boolean status,
        String deleteFlag,
        String createTime,
        String createUser,
        String updateTime,
        String updateUser,
        List<FrontendDictionaryRecord> children
    ) {

        private FrontendDictionaryRecord withoutChildren() {
            return new FrontendDictionaryRecord(
                id, parentId, weight, name, tenantId, dictLabel, dictValue, category, sortCode, status, deleteFlag,
                createTime, createUser, updateTime, updateUser, List.of()
            );
        }
    }

    public record FrontendNoticeRecord(
        String id,
        String tenantId,
        String tenantName,
        String title,
        String content,
        String type,
        boolean status,
        Boolean isPinned,
        String publisher,
        String publishedAt,
        String createdAt,
        String updatedAt
    ) {
    }

    @Getter
    @Setter
    public static class FrontendConfigUpsertRequest {

        @NotBlank
        private String configKey;

        @NotBlank
        private String configName;

        @NotBlank
        private String configValue;

        private String valueType;

        private Boolean status;

        private String remark;
    }

    @Getter
    @Setter
    public static class FrontendConfigPatchRequest {

        private String configKey;

        private String configName;

        private String configValue;

        private String valueType;

        private Boolean status;

        private String remark;
    }

    @Getter
    @Setter
    public static class FrontendDictionaryUpsertRequest {

        @NotBlank
        private String parentId;

        @NotBlank
        private String name;

        @NotBlank
        private String dictLabel;

        @NotBlank
        private String dictValue;

        @NotBlank
        private String category;

        @NotNull
        private Integer sortCode;

        @NotNull
        private Integer weight;

        private Boolean status;

        private String deleteFlag;
    }

    @Getter
    @Setter
    public static class FrontendDictionaryPatchRequest {

        private String parentId;

        private String name;

        private String dictLabel;

        private String dictValue;

        private String category;

        private Integer sortCode;

        private Integer weight;

        private Boolean status;

        private String deleteFlag;
    }

    @Getter
    @Setter
    public static class FrontendNoticeUpsertRequest {

        @NotBlank
        private String title;

        @NotBlank
        private String content;

        @NotBlank
        private String type;

        private Boolean status;

        private String publisher;

        private String publishedAt;
    }

    @Getter
    @Setter
    public static class FrontendNoticePatchRequest {

        private String title;

        private String content;

        private String type;

        private Boolean status;

        private String publisher;

        private String publishedAt;
    }

    @Getter
    @Setter
    public static class FrontendNoticePublishRequest {

        private String publisher;

        private String publishedAt;
    }

    @Getter
    @Setter
    public static class FrontendNoticePinRequest {

        @NotNull
        private Boolean pinned;
    }
}
