package com.luckycolor.admin.modules.system.dictionary.catalog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.modules.system.dictionary.catalog.service.DictionaryCatalogService;
import com.luckycolor.admin.modules.system.dictionary.catalog.web.response.DictionaryCatalogItemResponse;
import com.luckycolor.admin.modules.system.dictionary.catalog.web.response.DictionaryCatalogResponse;
import com.luckycolor.admin.modules.system.dictionary.item.dataobject.DictionaryItemDO;
import com.luckycolor.admin.modules.system.dictionary.item.mapper.DictionaryItemMapper;
import com.luckycolor.admin.modules.system.dictionary.type.dataobject.DictionaryTypeDO;
import com.luckycolor.admin.modules.system.dictionary.type.mapper.DictionaryTypeMapper;
import java.util.Comparator;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@ConditionalOnBean(DictionaryItemMapper.class)
public class DictionaryCatalogServiceImpl implements DictionaryCatalogService {

    private final DictionaryTypeMapper dictionaryTypeMapper;
    private final DictionaryItemMapper dictionaryItemMapper;

    public DictionaryCatalogServiceImpl(
        DictionaryTypeMapper dictionaryTypeMapper,
        DictionaryItemMapper dictionaryItemMapper
    ) {
        this.dictionaryTypeMapper = dictionaryTypeMapper;
        this.dictionaryItemMapper = dictionaryItemMapper;
    }

    @Override
    public List<DictionaryCatalogItemResponse> listItemsByType(String typeCode) {
        if (!StringUtils.hasText(typeCode)) {
            return List.of();
        }
        DictionaryTypeDO dictionaryType = findEnabledType(typeCode);
        if (dictionaryType == null) {
            return List.of();
        }
        return buildTree(findEnabledItems(List.of(typeCode.trim())), 0L);
    }

    @Override
    public List<DictionaryCatalogResponse> listCatalog(List<String> typeCodes) {
        if (typeCodes == null || typeCodes.isEmpty()) {
            return List.of();
        }
        List<String> normalizedTypeCodes = typeCodes.stream()
            .filter(StringUtils::hasText)
            .map(String::trim)
            .distinct()
            .toList();
        if (normalizedTypeCodes.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<DictionaryTypeDO> typeQuery = new LambdaQueryWrapper<>();
        typeQuery.eq(DictionaryTypeDO::getStatus, 0)
            .in(DictionaryTypeDO::getTypeCode, normalizedTypeCodes)
            .orderByAsc(DictionaryTypeDO::getSort)
            .orderByAsc(DictionaryTypeDO::getId);
        List<DictionaryTypeDO> dictionaryTypes = dictionaryTypeMapper.selectList(typeQuery);
        if (dictionaryTypes.isEmpty()) {
            return List.of();
        }
        List<String> existingTypeCodes = dictionaryTypes.stream().map(DictionaryTypeDO::getTypeCode).toList();
        List<DictionaryItemDO> items = findEnabledItems(existingTypeCodes);
        return dictionaryTypes.stream()
            .map(type -> new DictionaryCatalogResponse(
                type.getTypeCode(),
                type.getTypeName(),
                buildTree(items.stream().filter(item -> type.getTypeCode().equals(item.getTypeCode())).toList(), 0L)
            ))
            .toList();
    }

    private DictionaryTypeDO findEnabledType(String typeCode) {
        LambdaQueryWrapper<DictionaryTypeDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DictionaryTypeDO::getTypeCode, typeCode.trim())
            .eq(DictionaryTypeDO::getStatus, 0);
        return dictionaryTypeMapper.selectList(queryWrapper).stream().findFirst().orElse(null);
    }

    private List<DictionaryItemDO> findEnabledItems(List<String> typeCodes) {
        LambdaQueryWrapper<DictionaryItemDO> itemQuery = new LambdaQueryWrapper<>();
        itemQuery.eq(DictionaryItemDO::getStatus, 0)
            .in(DictionaryItemDO::getTypeCode, typeCodes)
            .orderByAsc(DictionaryItemDO::getParentId)
            .orderByAsc(DictionaryItemDO::getSort)
            .orderByAsc(DictionaryItemDO::getId);
        return dictionaryItemMapper.selectList(itemQuery);
    }

    private List<DictionaryCatalogItemResponse> buildTree(List<DictionaryItemDO> items, Long parentId) {
        return items.stream()
            .filter(item -> normalizeParentId(item.getParentId()).equals(normalizeParentId(parentId)))
            .sorted(Comparator.comparing(DictionaryItemDO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(DictionaryItemDO::getId, Comparator.nullsLast(Long::compareTo)))
            .map(item -> new DictionaryCatalogItemResponse(
                item.getId(),
                normalizeParentId(item.getParentId()),
                item.getItemLabel(),
                item.getItemValue(),
                item.getItemTag(),
                buildTree(items, item.getId())
            ))
            .toList();
    }

    private Long normalizeParentId(Long parentId) {
        return parentId == null ? 0L : parentId;
    }
}
