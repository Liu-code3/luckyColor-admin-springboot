package com.luckycolor.admin.modules.system.dictionary.cache.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.modules.system.dictionary.cache.service.DictionaryCatalogCacheService;
import com.luckycolor.admin.modules.system.dictionary.catalog.service.DictionaryCatalogService;
import com.luckycolor.admin.modules.system.dictionary.catalog.web.response.DictionaryCatalogItemResponse;
import com.luckycolor.admin.modules.system.dictionary.catalog.web.response.DictionaryCatalogResponse;
import com.luckycolor.admin.modules.system.dictionary.type.dataobject.DictionaryTypeDO;
import com.luckycolor.admin.modules.system.dictionary.type.mapper.DictionaryTypeMapper;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@ConditionalOnMissingBean(RedisTemplate.class)
public class InMemoryDictionaryCatalogCacheService implements DictionaryCatalogCacheService {

    private final ObjectProvider<DictionaryCatalogService> dictionaryCatalogServiceProvider;
    private final ObjectProvider<DictionaryTypeMapper> dictionaryTypeMapperProvider;
    private final Map<String, DictionaryCatalogResponse> cache = new ConcurrentHashMap<>();

    public InMemoryDictionaryCatalogCacheService(
        ObjectProvider<DictionaryCatalogService> dictionaryCatalogServiceProvider,
        ObjectProvider<DictionaryTypeMapper> dictionaryTypeMapperProvider
    ) {
        this.dictionaryCatalogServiceProvider = dictionaryCatalogServiceProvider;
        this.dictionaryTypeMapperProvider = dictionaryTypeMapperProvider;
    }

    @Override
    public List<DictionaryCatalogItemResponse> listItemsByType(String typeCode) {
        return getOrLoad(typeCode).items();
    }

    @Override
    public List<DictionaryCatalogResponse> listCatalog(List<String> typeCodes) {
        return normalizeTypeCodes(typeCodes).stream()
            .map(this::getOrLoad)
            .filter(response -> StringUtils.hasText(response.typeName()) || !response.items().isEmpty())
            .toList();
    }

    @Override
    public int refresh(List<String> typeCodes) {
        List<String> refreshTypeCodes = resolveRefreshTypeCodes(typeCodes);
        refreshTypeCodes.forEach(typeCode -> cache.put(typeCode, loadCatalog(typeCode)));
        return refreshTypeCodes.size();
    }

    @Override
    public void evict(List<String> typeCodes) {
        normalizeTypeCodes(typeCodes).forEach(cache::remove);
    }

    private DictionaryCatalogResponse getOrLoad(String typeCode) {
        return cache.computeIfAbsent(typeCode, this::loadCatalog);
    }

    private DictionaryCatalogResponse loadCatalog(String typeCode) {
        DictionaryCatalogService dictionaryCatalogService = dictionaryCatalogServiceProvider.getIfAvailable();
        if (dictionaryCatalogService == null) {
            return new DictionaryCatalogResponse(typeCode, null, List.of());
        }
        return dictionaryCatalogService.listCatalog(List.of(typeCode)).stream()
            .findFirst()
            .orElse(new DictionaryCatalogResponse(typeCode, null, List.of()));
    }

    private List<String> resolveRefreshTypeCodes(List<String> typeCodes) {
        List<String> normalizedTypeCodes = normalizeTypeCodes(typeCodes);
        if (!normalizedTypeCodes.isEmpty()) {
            return normalizedTypeCodes;
        }
        DictionaryTypeMapper dictionaryTypeMapper = dictionaryTypeMapperProvider.getIfAvailable();
        if (dictionaryTypeMapper == null) {
            return List.of();
        }
        LambdaQueryWrapper<DictionaryTypeDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DictionaryTypeDO::getStatus, 0)
            .orderByAsc(DictionaryTypeDO::getSort)
            .orderByAsc(DictionaryTypeDO::getId);
        return dictionaryTypeMapper.selectList(queryWrapper).stream()
            .map(DictionaryTypeDO::getTypeCode)
            .toList();
    }

    private List<String> normalizeTypeCodes(List<String> typeCodes) {
        if (typeCodes == null || typeCodes.isEmpty()) {
            return List.of();
        }
        return typeCodes.stream()
            .filter(StringUtils::hasText)
            .map(String::trim)
            .distinct()
            .toList();
    }
}
