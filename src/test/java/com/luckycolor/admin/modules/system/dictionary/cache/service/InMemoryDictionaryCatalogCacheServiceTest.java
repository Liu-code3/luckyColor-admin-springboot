package com.luckycolor.admin.modules.system.dictionary.cache.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.luckycolor.admin.modules.system.dictionary.cache.service.impl.InMemoryDictionaryCatalogCacheService;
import com.luckycolor.admin.modules.system.dictionary.catalog.service.DictionaryCatalogService;
import com.luckycolor.admin.modules.system.dictionary.catalog.web.response.DictionaryCatalogItemResponse;
import com.luckycolor.admin.modules.system.dictionary.catalog.web.response.DictionaryCatalogResponse;
import com.luckycolor.admin.modules.system.dictionary.type.dataobject.DictionaryTypeDO;
import com.luckycolor.admin.modules.system.dictionary.type.mapper.DictionaryTypeMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;

class InMemoryDictionaryCatalogCacheServiceTest {

    @Test
    void shouldCacheCatalogAfterFirstLoad() {
        DictionaryCatalogService catalogService = Mockito.mock(DictionaryCatalogService.class);
        DictionaryTypeMapper typeMapper = Mockito.mock(DictionaryTypeMapper.class);
        when(catalogService.listCatalog(List.of("user_status"))).thenReturn(List.of(catalog("user_status")));
        DictionaryCatalogCacheService cacheService = new InMemoryDictionaryCatalogCacheService(
            provider(catalogService),
            provider(typeMapper)
        );

        List<DictionaryCatalogResponse> first = cacheService.listCatalog(List.of("user_status"));
        List<DictionaryCatalogResponse> second = cacheService.listCatalog(List.of("user_status"));

        assertThat(first).hasSize(1);
        assertThat(second).hasSize(1);
        verify(catalogService, times(1)).listCatalog(List.of("user_status"));
    }

    @Test
    void shouldRefreshAllActiveDictionaryTypes() {
        DictionaryCatalogService catalogService = Mockito.mock(DictionaryCatalogService.class);
        DictionaryTypeMapper typeMapper = Mockito.mock(DictionaryTypeMapper.class);
        when(typeMapper.selectList(Mockito.any())).thenReturn(List.of(type("user_status"), type("gender")));
        when(catalogService.listCatalog(List.of("user_status"))).thenReturn(List.of(catalog("user_status")));
        when(catalogService.listCatalog(List.of("gender"))).thenReturn(List.of(catalog("gender")));
        DictionaryCatalogCacheService cacheService = new InMemoryDictionaryCatalogCacheService(
            provider(catalogService),
            provider(typeMapper)
        );

        int result = cacheService.refresh(null);

        assertThat(result).isEqualTo(2);
    }

    private DictionaryTypeDO type(String typeCode) {
        DictionaryTypeDO dictionaryType = new DictionaryTypeDO();
        dictionaryType.setTypeCode(typeCode);
        return dictionaryType;
    }

    private DictionaryCatalogResponse catalog(String typeCode) {
        return new DictionaryCatalogResponse(
            typeCode,
            typeCode,
            List.of(new DictionaryCatalogItemResponse(1L, 0L, "Enabled", "0", "success", List.of()))
        );
    }

    private <T> ObjectProvider<T> provider(T bean) {
        return new ObjectProvider<>() {
            @Override
            public T getObject(Object... args) {
                return bean;
            }

            @Override
            public T getIfAvailable() {
                return bean;
            }

            @Override
            public T getIfUnique() {
                return bean;
            }

            @Override
            public T getObject() {
                return bean;
            }
        };
    }
}
