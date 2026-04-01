package com.luckycolor.admin.modules.system.dictionary.type.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.datascope.CurrentDataScopeResolver;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.modules.system.dictionary.item.dataobject.DictionaryItemDO;
import com.luckycolor.admin.modules.system.dictionary.item.mapper.DictionaryItemMapper;
import com.luckycolor.admin.modules.system.dictionary.type.dataobject.DictionaryTypeDO;
import com.luckycolor.admin.modules.system.dictionary.type.mapper.DictionaryTypeMapper;
import com.luckycolor.admin.modules.system.dictionary.type.service.impl.DictionaryTypeServiceImpl;
import com.luckycolor.admin.modules.system.dictionary.type.web.request.DictionaryTypePageQuery;
import com.luckycolor.admin.modules.system.dictionary.type.web.request.DictionaryTypeSaveRequest;
import com.luckycolor.admin.modules.system.dictionary.type.web.response.DictionaryTypeDetailResponse;
import com.luckycolor.admin.modules.system.dictionary.type.web.response.DictionaryTypePageResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

class DictionaryTypeServiceImplTest {

    @Test
    void shouldReturnDictionaryTypePage() {
        DictionaryTypeMapper mapper = Mockito.mock(DictionaryTypeMapper.class);
        when(mapper.selectPageResult(any(), any())).thenReturn(PageResult.of(List.of(dictionaryType()), 1L));
        DictionaryTypeService service = new DictionaryTypeServiceImpl(mapper, noScopeBuilder(), itemMapper());

        PageResult<DictionaryTypePageResponse> result = service.pageDictionaryTypes(new DictionaryTypePageQuery());

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getList()).extracting(DictionaryTypePageResponse::typeCode).containsExactly("user_status");
    }

    @Test
    void shouldReturnDictionaryTypeDetail() {
        DictionaryTypeMapper mapper = Mockito.mock(DictionaryTypeMapper.class);
        when(mapper.selectById(1L)).thenReturn(dictionaryType());
        DictionaryTypeService service = new DictionaryTypeServiceImpl(mapper, noScopeBuilder(), itemMapper());

        DictionaryTypeDetailResponse result = service.getDictionaryType(1L);

        assertThat(result.typeName()).isEqualTo("User Status");
    }

    @Test
    void shouldCreateDictionaryType() {
        DictionaryTypeMapper mapper = Mockito.mock(DictionaryTypeMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of());
        DictionaryTypeService service = new DictionaryTypeServiceImpl(mapper, noScopeBuilder(), itemMapper());

        Long result = service.createDictionaryType(saveRequest());

        assertThat(result).isNull();
        verify(mapper).insert(any(DictionaryTypeDO.class));
    }

    @Test
    void shouldDeleteDictionaryType() {
        DictionaryTypeMapper mapper = Mockito.mock(DictionaryTypeMapper.class);
        when(mapper.selectById(1L)).thenReturn(dictionaryType());
        DictionaryItemMapper itemMapper = itemMapper();
        when(itemMapper.selectCount(any())).thenReturn(0L);
        DictionaryTypeService service = new DictionaryTypeServiceImpl(mapper, noScopeBuilder(), itemMapper);

        service.deleteDictionaryType(1L);

        verify(mapper).deleteById(1L);
    }

    @Test
    void shouldThrowWhenDictionaryTypeNotFound() {
        DictionaryTypeMapper mapper = Mockito.mock(DictionaryTypeMapper.class);
        when(mapper.selectById(99L)).thenReturn(null);
        DictionaryTypeService service = new DictionaryTypeServiceImpl(mapper, noScopeBuilder(), itemMapper());

        assertThatThrownBy(() -> service.getDictionaryType(99L))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("404 NOT_FOUND");
    }

    private DictionaryTypeDO dictionaryType() {
        DictionaryTypeDO dictionaryType = new DictionaryTypeDO();
        dictionaryType.setId(1L);
        dictionaryType.setTenantId(1L);
        dictionaryType.setTypeCode("user_status");
        dictionaryType.setTypeName("User Status");
        dictionaryType.setStatus(0);
        dictionaryType.setSort(1);
        dictionaryType.setRemark("default");
        return dictionaryType;
    }

    private DictionaryTypeSaveRequest saveRequest() {
        DictionaryTypeSaveRequest request = new DictionaryTypeSaveRequest();
        request.setTypeCode("user_status");
        request.setTypeName("User Status");
        request.setStatus(0);
        request.setSort(1);
        request.setRemark("default");
        return request;
    }

    private DataScopeConditionBuilder noScopeBuilder() {
        CurrentDataScopeResolver resolver = Mockito.mock(CurrentDataScopeResolver.class);
        when(resolver.resolveCurrentRule()).thenReturn(java.util.Optional.empty());
        return new DataScopeConditionBuilder(resolver);
    }

    private DictionaryItemMapper itemMapper() {
        DictionaryItemMapper mapper = Mockito.mock(DictionaryItemMapper.class);
        when(mapper.selectList(any())).thenReturn(List.<DictionaryItemDO>of());
        when(mapper.selectCount(any())).thenReturn(0L);
        return mapper;
    }
}
