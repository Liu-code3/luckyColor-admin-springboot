package com.luckycolor.admin.modules.platform.i18n.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.datascope.CurrentDataScopeResolver;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.modules.platform.i18n.dataobject.I18nResourceDO;
import com.luckycolor.admin.modules.platform.i18n.mapper.I18nResourceMapper;
import com.luckycolor.admin.modules.platform.i18n.service.impl.I18nResourceServiceImpl;
import com.luckycolor.admin.modules.platform.i18n.web.request.I18nResourcePageQuery;
import com.luckycolor.admin.modules.platform.i18n.web.request.I18nResourceSaveRequest;
import com.luckycolor.admin.modules.platform.i18n.web.request.I18nResourceStatusRequest;
import com.luckycolor.admin.modules.platform.i18n.web.response.I18nResourceDetailResponse;
import com.luckycolor.admin.modules.platform.i18n.web.response.I18nResourcePageResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

class I18nResourceServiceImplTest {

    @Test
    void shouldReturnResourcePage() {
        I18nResourceMapper mapper = Mockito.mock(I18nResourceMapper.class);
        when(mapper.selectPageResult(any(), any())).thenReturn(PageResult.of(List.of(resource()), 1L));
        I18nResourceService service = new I18nResourceServiceImpl(mapper, noScopeBuilder());

        PageResult<I18nResourcePageResponse> result = service.pageResources(new I18nResourcePageQuery());

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getList()).extracting(I18nResourcePageResponse::resourceKey).containsExactly("user.login.title");
    }

    @Test
    void shouldReturnResourceDetail() {
        I18nResourceMapper mapper = Mockito.mock(I18nResourceMapper.class);
        when(mapper.selectById(1L)).thenReturn(resource());
        I18nResourceService service = new I18nResourceServiceImpl(mapper, noScopeBuilder());

        I18nResourceDetailResponse result = service.getResource(1L);

        assertThat(result.resourceValue()).isEqualTo("Login");
    }

    @Test
    void shouldCreateResource() {
        I18nResourceMapper mapper = Mockito.mock(I18nResourceMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of());
        I18nResourceService service = new I18nResourceServiceImpl(mapper, noScopeBuilder());

        Long result = service.createResource(saveRequest());

        assertThat(result).isNull();
        verify(mapper).insert(any(I18nResourceDO.class));
    }

    @Test
    void shouldUpdateStatusAndVersion() {
        I18nResourceMapper mapper = Mockito.mock(I18nResourceMapper.class);
        I18nResourceDO resource = resource();
        when(mapper.selectById(1L)).thenReturn(resource);
        I18nResourceService service = new I18nResourceServiceImpl(mapper, noScopeBuilder());
        I18nResourceStatusRequest statusRequest = new I18nResourceStatusRequest();
        statusRequest.setStatus(1);

        service.updateStatus(1L, statusRequest);
        service.bumpVersion(1L);

        assertThat(resource.getStatus()).isEqualTo(1);
        assertThat(resource.getVersion()).isEqualTo(2);
        verify(mapper, times(2)).updateById(resource);
    }

    @Test
    void shouldThrowWhenResourceNotFound() {
        I18nResourceMapper mapper = Mockito.mock(I18nResourceMapper.class);
        when(mapper.selectById(99L)).thenReturn(null);
        I18nResourceService service = new I18nResourceServiceImpl(mapper, noScopeBuilder());

        assertThatThrownBy(() -> service.getResource(99L))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("404 NOT_FOUND");
    }

    private I18nResourceDO resource() {
        I18nResourceDO resource = new I18nResourceDO();
        resource.setId(1L);
        resource.setTenantId(1L);
        resource.setLocale("en-US");
        resource.setNamespace("auth");
        resource.setResourceKey("user.login.title");
        resource.setResourceValue("Login");
        resource.setVersion(1);
        resource.setStatus(0);
        resource.setRemark("default");
        return resource;
    }

    private I18nResourceSaveRequest saveRequest() {
        I18nResourceSaveRequest request = new I18nResourceSaveRequest();
        request.setLocale("en-US");
        request.setNamespace("auth");
        request.setResourceKey("user.login.title");
        request.setResourceValue("Login");
        request.setStatus(0);
        request.setRemark("default");
        return request;
    }

    private DataScopeConditionBuilder noScopeBuilder() {
        CurrentDataScopeResolver resolver = Mockito.mock(CurrentDataScopeResolver.class);
        when(resolver.resolveCurrentRule()).thenReturn(java.util.Optional.empty());
        return new DataScopeConditionBuilder(resolver);
    }
}
