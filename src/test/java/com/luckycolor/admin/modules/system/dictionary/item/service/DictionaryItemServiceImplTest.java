package com.luckycolor.admin.modules.system.dictionary.item.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.luckycolor.admin.infrastructure.security.datascope.CurrentDataScopeResolver;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.modules.system.dictionary.item.dataobject.DictionaryItemDO;
import com.luckycolor.admin.modules.system.dictionary.item.mapper.DictionaryItemMapper;
import com.luckycolor.admin.modules.system.dictionary.item.service.impl.DictionaryItemServiceImpl;
import com.luckycolor.admin.modules.system.dictionary.item.web.request.DictionaryItemSaveRequest;
import com.luckycolor.admin.modules.system.dictionary.item.web.request.DictionaryItemTreeQuery;
import com.luckycolor.admin.modules.system.dictionary.item.web.response.DictionaryItemDetailResponse;
import com.luckycolor.admin.modules.system.dictionary.item.web.response.DictionaryItemTreeResponse;
import com.luckycolor.admin.modules.system.dictionary.type.dataobject.DictionaryTypeDO;
import com.luckycolor.admin.modules.system.dictionary.type.mapper.DictionaryTypeMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

class DictionaryItemServiceImplTest {

    @Test
    void shouldReturnDictionaryItemTree() {
        DictionaryItemMapper mapper = Mockito.mock(DictionaryItemMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of(
            item(2L, 1L, "Disabled", "1", 2),
            item(1L, 0L, "Status", "root", 1),
            item(3L, 1L, "Enabled", "0", 1)
        ));
        DictionaryItemService service = new DictionaryItemServiceImpl(mapper, typeMapper(), noScopeBuilder());
        DictionaryItemTreeQuery query = new DictionaryItemTreeQuery();
        query.setTypeCode("user_status");

        List<DictionaryItemTreeResponse> result = service.listDictionaryItemTree(query);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).children()).extracting(DictionaryItemTreeResponse::itemLabel)
            .containsExactly("Enabled", "Disabled");
    }

    @Test
    void shouldReturnDictionaryItemDetail() {
        DictionaryItemMapper mapper = Mockito.mock(DictionaryItemMapper.class);
        DictionaryItemDO dictionaryItem = item(1L, 0L, "Enabled", "0", 1);
        dictionaryItem.setRemark("default");
        when(mapper.selectById(1L)).thenReturn(dictionaryItem);
        DictionaryItemService service = new DictionaryItemServiceImpl(mapper, typeMapper(), noScopeBuilder());

        DictionaryItemDetailResponse result = service.getDictionaryItem(1L);

        assertThat(result.itemLabel()).isEqualTo("Enabled");
        assertThat(result.remark()).isEqualTo("default");
    }

    @Test
    void shouldCreateDictionaryItem() {
        DictionaryItemMapper mapper = Mockito.mock(DictionaryItemMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of());
        DictionaryItemService service = new DictionaryItemServiceImpl(mapper, typeMapper(), noScopeBuilder());

        Long result = service.createDictionaryItem(saveRequest());

        assertThat(result).isNull();
        verify(mapper).insert(any(DictionaryItemDO.class));
    }

    @Test
    void shouldRejectDeleteWhenItemHasChildren() {
        DictionaryItemMapper mapper = Mockito.mock(DictionaryItemMapper.class);
        when(mapper.selectById(1L)).thenReturn(item(1L, 0L, "Enabled", "0", 1));
        when(mapper.selectCount(any())).thenReturn(1L);
        DictionaryItemService service = new DictionaryItemServiceImpl(mapper, typeMapper(), noScopeBuilder());

        assertThatThrownBy(() -> service.deleteDictionaryItem(1L))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST");

        verify(mapper, never()).deleteById(1L);
    }

    private DictionaryItemDO item(Long id, Long parentId, String itemLabel, String itemValue, Integer sort) {
        DictionaryItemDO dictionaryItem = new DictionaryItemDO();
        dictionaryItem.setId(id);
        dictionaryItem.setTenantId(1L);
        dictionaryItem.setTypeCode("user_status");
        dictionaryItem.setParentId(parentId);
        dictionaryItem.setItemLabel(itemLabel);
        dictionaryItem.setItemValue(itemValue);
        dictionaryItem.setItemTag("tag");
        dictionaryItem.setSort(sort);
        dictionaryItem.setStatus(0);
        return dictionaryItem;
    }

    private DictionaryItemSaveRequest saveRequest() {
        DictionaryItemSaveRequest request = new DictionaryItemSaveRequest();
        request.setTypeCode("user_status");
        request.setParentId(0L);
        request.setItemLabel("Enabled");
        request.setItemValue("0");
        request.setItemTag("success");
        request.setSort(1);
        request.setStatus(0);
        request.setRemark("default");
        return request;
    }

    private DictionaryTypeMapper typeMapper() {
        DictionaryTypeMapper mapper = Mockito.mock(DictionaryTypeMapper.class);
        DictionaryTypeDO dictionaryType = new DictionaryTypeDO();
        dictionaryType.setId(1L);
        dictionaryType.setTypeCode("user_status");
        dictionaryType.setTypeName("User Status");
        dictionaryType.setStatus(0);
        when(mapper.selectList(any())).thenReturn(List.of(dictionaryType));
        return mapper;
    }

    private DataScopeConditionBuilder noScopeBuilder() {
        CurrentDataScopeResolver resolver = Mockito.mock(CurrentDataScopeResolver.class);
        when(resolver.resolveCurrentRule()).thenReturn(java.util.Optional.empty());
        return new DataScopeConditionBuilder(resolver);
    }
}
