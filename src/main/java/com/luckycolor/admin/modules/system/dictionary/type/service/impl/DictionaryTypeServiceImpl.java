package com.luckycolor.admin.modules.system.dictionary.type.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.modules.system.dictionary.type.dataobject.DictionaryTypeDO;
import com.luckycolor.admin.modules.system.dictionary.type.mapper.DictionaryTypeMapper;
import com.luckycolor.admin.modules.system.dictionary.type.service.DictionaryTypeService;
import com.luckycolor.admin.modules.system.dictionary.type.web.request.DictionaryTypePageQuery;
import com.luckycolor.admin.modules.system.dictionary.type.web.request.DictionaryTypeSaveRequest;
import com.luckycolor.admin.modules.system.dictionary.type.web.response.DictionaryTypeDetailResponse;
import com.luckycolor.admin.modules.system.dictionary.type.web.response.DictionaryTypePageResponse;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@ConditionalOnBean(DictionaryTypeMapper.class)
public class DictionaryTypeServiceImpl implements DictionaryTypeService {

    private final DictionaryTypeMapper dictionaryTypeMapper;
    private final DataScopeConditionBuilder dataScopeConditionBuilder;

    public DictionaryTypeServiceImpl(
        DictionaryTypeMapper dictionaryTypeMapper,
        DataScopeConditionBuilder dataScopeConditionBuilder
    ) {
        this.dictionaryTypeMapper = dictionaryTypeMapper;
        this.dataScopeConditionBuilder = dataScopeConditionBuilder;
    }

    @Override
    public PageResult<DictionaryTypePageResponse> pageDictionaryTypes(DictionaryTypePageQuery query) {
        PageResult<DictionaryTypeDO> pageResult = dictionaryTypeMapper.selectPageResult(query, buildQueryWrapper(query));
        return PageResult.of(pageResult.getList().stream().map(this::toPageResponse).toList(), pageResult.getTotal());
    }

    @Override
    public DictionaryTypeDetailResponse getDictionaryType(Long id) {
        return toDetailResponse(getRequiredDictionaryType(id));
    }

    @Override
    public Long createDictionaryType(DictionaryTypeSaveRequest request) {
        ensureTypeCodeUnique(null, request.getTypeCode());
        DictionaryTypeDO dictionaryType = new DictionaryTypeDO();
        fillDictionaryType(dictionaryType, request);
        dictionaryTypeMapper.insert(dictionaryType);
        return dictionaryType.getId();
    }

    @Override
    public void updateDictionaryType(Long id, DictionaryTypeSaveRequest request) {
        DictionaryTypeDO dictionaryType = getRequiredDictionaryType(id);
        ensureTypeCodeUnique(id, request.getTypeCode());
        fillDictionaryType(dictionaryType, request);
        dictionaryTypeMapper.updateById(dictionaryType);
    }

    @Override
    public void deleteDictionaryType(Long id) {
        getRequiredDictionaryType(id);
        dictionaryTypeMapper.deleteById(id);
    }

    private LambdaQueryWrapper<DictionaryTypeDO> buildQueryWrapper(DictionaryTypePageQuery query) {
        LambdaQueryWrapper<DictionaryTypeDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(query.getTypeCode()), DictionaryTypeDO::getTypeCode, query.getTypeCode());
        queryWrapper.like(StringUtils.hasText(query.getTypeName()), DictionaryTypeDO::getTypeName, query.getTypeName());
        queryWrapper.eq(query.getStatus() != null, DictionaryTypeDO::getStatus, query.getStatus());
        dataScopeConditionBuilder.applyCurrentScope(queryWrapper, DictionaryTypeDO::getTenantId, null);
        queryWrapper.orderByAsc(DictionaryTypeDO::getSort)
            .orderByDesc(DictionaryTypeDO::getCreateTime);
        return queryWrapper;
    }

    private DictionaryTypeDO getRequiredDictionaryType(Long id) {
        DictionaryTypeDO dictionaryType = dictionaryTypeMapper.selectById(id);
        if (dictionaryType == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dictionary type not found");
        }
        return dictionaryType;
    }

    private void ensureTypeCodeUnique(Long currentId, String typeCode) {
        if (!StringUtils.hasText(typeCode)) {
            return;
        }
        LambdaQueryWrapper<DictionaryTypeDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DictionaryTypeDO::getTypeCode, typeCode.trim());
        List<DictionaryTypeDO> existingTypes = dictionaryTypeMapper.selectList(queryWrapper);
        boolean duplicated = existingTypes.stream()
            .anyMatch(item -> currentId == null || !currentId.equals(item.getId()));
        if (duplicated) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Dictionary type code already exists");
        }
    }

    private void fillDictionaryType(DictionaryTypeDO dictionaryType, DictionaryTypeSaveRequest request) {
        dictionaryType.setTypeCode(request.getTypeCode());
        dictionaryType.setTypeName(request.getTypeName());
        dictionaryType.setStatus(request.getStatus());
        dictionaryType.setSort(request.getSort());
        dictionaryType.setRemark(request.getRemark());
    }

    private DictionaryTypePageResponse toPageResponse(DictionaryTypeDO dictionaryType) {
        return new DictionaryTypePageResponse(
            dictionaryType.getId(),
            dictionaryType.getTenantId(),
            dictionaryType.getTypeCode(),
            dictionaryType.getTypeName(),
            dictionaryType.getStatus(),
            dictionaryType.getSort(),
            dictionaryType.getRemark()
        );
    }

    private DictionaryTypeDetailResponse toDetailResponse(DictionaryTypeDO dictionaryType) {
        return new DictionaryTypeDetailResponse(
            dictionaryType.getId(),
            dictionaryType.getTenantId(),
            dictionaryType.getTypeCode(),
            dictionaryType.getTypeName(),
            dictionaryType.getStatus(),
            dictionaryType.getSort(),
            dictionaryType.getRemark()
        );
    }
}
