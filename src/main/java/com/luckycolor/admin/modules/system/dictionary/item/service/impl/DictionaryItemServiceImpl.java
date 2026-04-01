package com.luckycolor.admin.modules.system.dictionary.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.modules.system.dictionary.item.dataobject.DictionaryItemDO;
import com.luckycolor.admin.modules.system.dictionary.item.mapper.DictionaryItemMapper;
import com.luckycolor.admin.modules.system.dictionary.item.service.DictionaryItemService;
import com.luckycolor.admin.modules.system.dictionary.item.web.request.DictionaryItemSaveRequest;
import com.luckycolor.admin.modules.system.dictionary.item.web.request.DictionaryItemTreeQuery;
import com.luckycolor.admin.modules.system.dictionary.item.web.response.DictionaryItemDetailResponse;
import com.luckycolor.admin.modules.system.dictionary.item.web.response.DictionaryItemTreeResponse;
import com.luckycolor.admin.modules.system.dictionary.type.dataobject.DictionaryTypeDO;
import com.luckycolor.admin.modules.system.dictionary.type.mapper.DictionaryTypeMapper;
import java.util.Comparator;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@ConditionalOnBean(DictionaryItemMapper.class)
public class DictionaryItemServiceImpl implements DictionaryItemService {

    private final DictionaryItemMapper dictionaryItemMapper;
    private final DictionaryTypeMapper dictionaryTypeMapper;
    private final DataScopeConditionBuilder dataScopeConditionBuilder;

    public DictionaryItemServiceImpl(
        DictionaryItemMapper dictionaryItemMapper,
        DictionaryTypeMapper dictionaryTypeMapper,
        DataScopeConditionBuilder dataScopeConditionBuilder
    ) {
        this.dictionaryItemMapper = dictionaryItemMapper;
        this.dictionaryTypeMapper = dictionaryTypeMapper;
        this.dataScopeConditionBuilder = dataScopeConditionBuilder;
    }

    @Override
    public List<DictionaryItemTreeResponse> listDictionaryItemTree(DictionaryItemTreeQuery query) {
        if (!StringUtils.hasText(query.getTypeCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dictionary type code is required");
        }
        List<DictionaryItemDO> items = dictionaryItemMapper.selectList(buildQueryWrapper(query));
        return buildTree(items, 0L);
    }

    @Override
    public DictionaryItemDetailResponse getDictionaryItem(Long id) {
        return toDetailResponse(getRequiredDictionaryItem(id));
    }

    @Override
    public Long createDictionaryItem(DictionaryItemSaveRequest request) {
        validateDictionaryTypeExists(request.getTypeCode());
        validateParentExists(request.getTypeCode(), request.getParentId());
        ensureItemValueUnique(null, request.getTypeCode(), request.getItemValue());
        DictionaryItemDO dictionaryItem = new DictionaryItemDO();
        fillDictionaryItem(dictionaryItem, request);
        dictionaryItemMapper.insert(dictionaryItem);
        return dictionaryItem.getId();
    }

    @Override
    public void updateDictionaryItem(Long id, DictionaryItemSaveRequest request) {
        DictionaryItemDO dictionaryItem = getRequiredDictionaryItem(id);
        validateDictionaryTypeExists(request.getTypeCode());
        validateParentExists(request.getTypeCode(), request.getParentId());
        ensureParentValid(id, request.getParentId(), request.getTypeCode());
        ensureItemValueUnique(id, request.getTypeCode(), request.getItemValue());
        fillDictionaryItem(dictionaryItem, request);
        dictionaryItemMapper.updateById(dictionaryItem);
    }

    @Override
    public void deleteDictionaryItem(Long id) {
        getRequiredDictionaryItem(id);
        LambdaQueryWrapper<DictionaryItemDO> childQuery = new LambdaQueryWrapper<>();
        childQuery.eq(DictionaryItemDO::getParentId, id);
        Long childCount = dictionaryItemMapper.selectCount(childQuery);
        if (childCount != null && childCount > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dictionary item has children and cannot be deleted");
        }
        dictionaryItemMapper.deleteById(id);
    }

    private LambdaQueryWrapper<DictionaryItemDO> buildQueryWrapper(DictionaryItemTreeQuery query) {
        LambdaQueryWrapper<DictionaryItemDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DictionaryItemDO::getTypeCode, query.getTypeCode().trim());
        queryWrapper.like(StringUtils.hasText(query.getItemLabel()), DictionaryItemDO::getItemLabel, query.getItemLabel());
        queryWrapper.eq(query.getStatus() != null, DictionaryItemDO::getStatus, query.getStatus());
        dataScopeConditionBuilder.applyCurrentScope(queryWrapper, DictionaryItemDO::getTenantId, null);
        queryWrapper.orderByAsc(DictionaryItemDO::getParentId)
            .orderByAsc(DictionaryItemDO::getSort)
            .orderByAsc(DictionaryItemDO::getId);
        return queryWrapper;
    }

    private List<DictionaryItemTreeResponse> buildTree(List<DictionaryItemDO> items, Long parentId) {
        return items.stream()
            .filter(item -> normalizeParentId(item.getParentId()).equals(normalizeParentId(parentId)))
            .sorted(Comparator.comparing(DictionaryItemDO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(DictionaryItemDO::getId, Comparator.nullsLast(Long::compareTo)))
            .map(item -> toTreeResponse(item, buildTree(items, item.getId())))
            .toList();
    }

    private DictionaryItemTreeResponse toTreeResponse(DictionaryItemDO dictionaryItem, List<DictionaryItemTreeResponse> children) {
        return new DictionaryItemTreeResponse(
            dictionaryItem.getId(),
            dictionaryItem.getTenantId(),
            dictionaryItem.getTypeCode(),
            normalizeParentId(dictionaryItem.getParentId()),
            dictionaryItem.getItemLabel(),
            dictionaryItem.getItemValue(),
            dictionaryItem.getItemTag(),
            dictionaryItem.getSort(),
            dictionaryItem.getStatus(),
            children
        );
    }

    private DictionaryItemDetailResponse toDetailResponse(DictionaryItemDO dictionaryItem) {
        return new DictionaryItemDetailResponse(
            dictionaryItem.getId(),
            dictionaryItem.getTenantId(),
            dictionaryItem.getTypeCode(),
            normalizeParentId(dictionaryItem.getParentId()),
            dictionaryItem.getItemLabel(),
            dictionaryItem.getItemValue(),
            dictionaryItem.getItemTag(),
            dictionaryItem.getSort(),
            dictionaryItem.getStatus(),
            dictionaryItem.getRemark()
        );
    }

    private DictionaryItemDO getRequiredDictionaryItem(Long id) {
        DictionaryItemDO dictionaryItem = dictionaryItemMapper.selectById(id);
        if (dictionaryItem == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dictionary item not found");
        }
        return dictionaryItem;
    }

    private void validateDictionaryTypeExists(String typeCode) {
        LambdaQueryWrapper<DictionaryTypeDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DictionaryTypeDO::getTypeCode, typeCode.trim());
        DictionaryTypeDO dictionaryType = dictionaryTypeMapper.selectList(queryWrapper).stream().findFirst().orElse(null);
        if (dictionaryType == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dictionary type not found");
        }
    }

    private void validateParentExists(String typeCode, Long parentId) {
        if (normalizeParentId(parentId).equals(0L)) {
            return;
        }
        DictionaryItemDO parentItem = dictionaryItemMapper.selectById(parentId);
        if (parentItem == null || !typeCode.trim().equals(parentItem.getTypeCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent dictionary item not found");
        }
    }

    private void ensureParentValid(Long currentId, Long parentId, String typeCode) {
        Long normalizedParentId = normalizeParentId(parentId);
        if (currentId.equals(normalizedParentId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent dictionary item cannot be self");
        }
        Long currentParentId = normalizedParentId;
        while (!currentParentId.equals(0L)) {
            if (currentId.equals(currentParentId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent dictionary item cannot be child node");
            }
            DictionaryItemDO parentItem = dictionaryItemMapper.selectById(currentParentId);
            if (parentItem == null || !typeCode.trim().equals(parentItem.getTypeCode())) {
                break;
            }
            currentParentId = normalizeParentId(parentItem.getParentId());
        }
    }

    private void ensureItemValueUnique(Long currentId, String typeCode, String itemValue) {
        if (!StringUtils.hasText(itemValue)) {
            return;
        }
        LambdaQueryWrapper<DictionaryItemDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DictionaryItemDO::getTypeCode, typeCode.trim())
            .eq(DictionaryItemDO::getItemValue, itemValue.trim());
        List<DictionaryItemDO> existingItems = dictionaryItemMapper.selectList(queryWrapper);
        boolean duplicated = existingItems.stream()
            .anyMatch(item -> currentId == null || !currentId.equals(item.getId()));
        if (duplicated) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Dictionary item value already exists");
        }
    }

    private void fillDictionaryItem(DictionaryItemDO dictionaryItem, DictionaryItemSaveRequest request) {
        dictionaryItem.setTypeCode(request.getTypeCode().trim());
        dictionaryItem.setParentId(normalizeParentId(request.getParentId()));
        dictionaryItem.setItemLabel(request.getItemLabel());
        dictionaryItem.setItemValue(request.getItemValue());
        dictionaryItem.setItemTag(request.getItemTag());
        dictionaryItem.setSort(request.getSort());
        dictionaryItem.setStatus(request.getStatus());
        dictionaryItem.setRemark(request.getRemark());
    }

    private Long normalizeParentId(Long parentId) {
        return parentId == null ? 0L : parentId;
    }
}
