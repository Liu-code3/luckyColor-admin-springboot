package com.luckycolor.admin.modules.system.dictionary.cache.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luckycolor.admin.infrastructure.cache.core.CacheKeyBuilder;
import com.luckycolor.admin.infrastructure.cache.core.CacheKeyNames;
import com.luckycolor.admin.modules.system.dictionary.cache.config.DictionaryCacheProperties;
import com.luckycolor.admin.modules.system.dictionary.cache.service.DictionaryCatalogCacheService;
import com.luckycolor.admin.modules.system.dictionary.catalog.service.DictionaryCatalogService;
import com.luckycolor.admin.modules.system.dictionary.catalog.web.response.DictionaryCatalogItemResponse;
import com.luckycolor.admin.modules.system.dictionary.catalog.web.response.DictionaryCatalogResponse;
import com.luckycolor.admin.modules.system.dictionary.type.dataobject.DictionaryTypeDO;
import com.luckycolor.admin.modules.system.dictionary.type.mapper.DictionaryTypeMapper;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@ConditionalOnBean(RedisTemplate.class)
public class RedisDictionaryCatalogCacheService implements DictionaryCatalogCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheKeyBuilder cacheKeyBuilder;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<DictionaryCatalogService> dictionaryCatalogServiceProvider;
    private final ObjectProvider<DictionaryTypeMapper> dictionaryTypeMapperProvider;
    private final DictionaryCacheProperties dictionaryCacheProperties;

    public RedisDictionaryCatalogCacheService(
        RedisTemplate<String, Object> redisTemplate,
        CacheKeyBuilder cacheKeyBuilder,
        ObjectMapper objectMapper,
        ObjectProvider<DictionaryCatalogService> dictionaryCatalogServiceProvider,
        ObjectProvider<DictionaryTypeMapper> dictionaryTypeMapperProvider,
        DictionaryCacheProperties dictionaryCacheProperties
    ) {
        this.redisTemplate = redisTemplate;
        this.cacheKeyBuilder = cacheKeyBuilder;
        this.objectMapper = objectMapper;
        this.dictionaryCatalogServiceProvider = dictionaryCatalogServiceProvider;
        this.dictionaryTypeMapperProvider = dictionaryTypeMapperProvider;
        this.dictionaryCacheProperties = dictionaryCacheProperties;
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
        refreshTypeCodes.forEach(typeCode -> writeCatalog(typeCode, loadCatalog(typeCode)));
        return refreshTypeCodes.size();
    }

    @Override
    public void evict(List<String> typeCodes) {
        normalizeTypeCodes(typeCodes).forEach(typeCode -> redisTemplate.delete(buildCacheKey(typeCode)));
    }

    private DictionaryCatalogResponse getOrLoad(String typeCode) {
        if (!StringUtils.hasText(typeCode)) {
            return new DictionaryCatalogResponse(null, null, List.of());
        }
        String cacheKey = buildCacheKey(typeCode.trim());
        Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
        if (cachedValue instanceof String cachedJson && StringUtils.hasText(cachedJson)) {
            try {
                return objectMapper.readValue(cachedJson, DictionaryCatalogResponse.class);
            } catch (JsonProcessingException ignored) {
                redisTemplate.delete(cacheKey);
            }
        }
        DictionaryCatalogResponse catalog = loadCatalog(typeCode.trim());
        writeCatalog(typeCode.trim(), catalog);
        return catalog;
    }

    private void writeCatalog(String typeCode, DictionaryCatalogResponse catalog) {
        try {
            redisTemplate.opsForValue().set(
                buildCacheKey(typeCode),
                objectMapper.writeValueAsString(catalog),
                dictionaryCacheProperties.getCacheExpireIn()
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize dictionary catalog", exception);
        }
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

    private String buildCacheKey(String typeCode) {
        return cacheKeyBuilder.build(CacheKeyNames.SYSTEM_DICTIONARY, typeCode);
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
