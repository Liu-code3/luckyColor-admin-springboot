package com.luckycolor.admin.modules.platform.codegen.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.platform.codegen.dataobject.CodegenColumnDO;
import com.luckycolor.admin.modules.platform.codegen.dataobject.CodegenTableDO;
import com.luckycolor.admin.modules.platform.codegen.mapper.CodegenColumnMapper;
import com.luckycolor.admin.modules.platform.codegen.mapper.CodegenTableMapper;
import com.luckycolor.admin.modules.platform.codegen.service.impl.CodegenMetadataServiceImpl;
import com.luckycolor.admin.modules.platform.codegen.web.request.CodegenColumnSaveItem;
import com.luckycolor.admin.modules.platform.codegen.web.request.CodegenColumnsSaveRequest;
import com.luckycolor.admin.modules.platform.codegen.web.request.CodegenDiscoveryQuery;
import com.luckycolor.admin.modules.platform.codegen.web.request.CodegenImportRequest;
import com.luckycolor.admin.modules.platform.codegen.web.request.CodegenTablePageQuery;
import com.luckycolor.admin.modules.platform.codegen.web.response.CodegenTableDetailResponse;
import com.luckycolor.admin.modules.platform.codegen.web.response.CodegenTablePageResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

class CodegenMetadataServiceImplTest {

    @Test
    void shouldReturnDiscoveryTablesPage() {
        CodegenTableMapper tableMapper = Mockito.mock(CodegenTableMapper.class);
        CodegenColumnMapper columnMapper = Mockito.mock(CodegenColumnMapper.class);
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(
            Map.of("table_name", "sys_user", "table_comment", "System User"),
            Map.of("table_name", "sys_role", "table_comment", "System Role")
        ));
        CodegenMetadataService service = new CodegenMetadataServiceImpl(tableMapper, columnMapper, jdbcTemplate);

        CodegenDiscoveryQuery query = new CodegenDiscoveryQuery();
        query.setTableName("user");
        PageResult<?> result = service.pageDiscoveryTables(query);

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getList()).hasSize(1);
    }

    @Test
    void shouldImportMetadata() {
        CodegenTableMapper tableMapper = Mockito.mock(CodegenTableMapper.class);
        CodegenColumnMapper columnMapper = Mockito.mock(CodegenColumnMapper.class);
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        when(tableMapper.selectList(any())).thenReturn(List.of());
        when(jdbcTemplate.queryForList(anyString(), eq("sys_user"))).thenReturn(List.of(
            Map.of("table_name", "sys_user", "table_comment", "System User")
        ));
        when(jdbcTemplate.queryForList(anyString(), eq("sys_user"))).thenReturn(List.of(
            Map.of(
                "column_name", "user_name",
                "column_comment", "User Name",
                "data_type", "varchar",
                "column_key", "",
                "is_nullable", "NO",
                "ordinal_position", 1
            ),
            Map.of(
                "column_name", "status",
                "column_comment", "Status",
                "data_type", "tinyint",
                "column_key", "",
                "is_nullable", "YES",
                "ordinal_position", 2
            )
        ));
        Mockito.doAnswer(invocation -> {
            CodegenTableDO table = invocation.getArgument(0);
            table.setId(1L);
            return 1;
        }).when(tableMapper).insert(any(CodegenTableDO.class));
        CodegenMetadataService service = new CodegenMetadataServiceImpl(tableMapper, columnMapper, jdbcTemplate);

        CodegenImportRequest request = new CodegenImportRequest();
        request.setTableNames(List.of("sys_user"));
        int imported = service.importTables(1L, request);

        assertThat(imported).isEqualTo(1);
        verify(tableMapper).insert(any(CodegenTableDO.class));
        verify(columnMapper, times(2)).insert(any(CodegenColumnDO.class));
    }

    @Test
    void shouldReturnImportedPage() {
        CodegenTableMapper tableMapper = Mockito.mock(CodegenTableMapper.class);
        CodegenColumnMapper columnMapper = Mockito.mock(CodegenColumnMapper.class);
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        when(tableMapper.selectPageResult(any(), any())).thenReturn(PageResult.of(List.of(table()), 1L));
        CodegenMetadataService service = new CodegenMetadataServiceImpl(tableMapper, columnMapper, jdbcTemplate);

        PageResult<CodegenTablePageResponse> result = service.pageTables(new CodegenTablePageQuery());

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getList().get(0).className()).isEqualTo("User");
    }

    @Test
    void shouldReturnTableDetail() {
        CodegenTableMapper tableMapper = Mockito.mock(CodegenTableMapper.class);
        CodegenColumnMapper columnMapper = Mockito.mock(CodegenColumnMapper.class);
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        when(tableMapper.selectById(1L)).thenReturn(table());
        when(columnMapper.selectList(any())).thenReturn(List.of(column()));
        CodegenMetadataService service = new CodegenMetadataServiceImpl(tableMapper, columnMapper, jdbcTemplate);

        CodegenTableDetailResponse detail = service.getTable(1L);

        assertThat(detail.columns()).hasSize(1);
        assertThat(detail.columns().get(0).javaField()).isEqualTo("userName");
    }

    @Test
    void shouldUpdateColumns() {
        CodegenTableMapper tableMapper = Mockito.mock(CodegenTableMapper.class);
        CodegenColumnMapper columnMapper = Mockito.mock(CodegenColumnMapper.class);
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        CodegenColumnDO existingColumn = column();
        when(tableMapper.selectById(1L)).thenReturn(table());
        when(columnMapper.selectList(any())).thenReturn(List.of(existingColumn));
        CodegenMetadataService service = new CodegenMetadataServiceImpl(tableMapper, columnMapper, jdbcTemplate);

        CodegenColumnsSaveRequest request = new CodegenColumnsSaveRequest();
        CodegenColumnSaveItem item = new CodegenColumnSaveItem();
        item.setId(11L);
        item.setJavaType("String");
        item.setJavaField("userDisplayName");
        item.setHtmlType("input");
        item.setQueryType("LIKE");
        item.setRequired(1);
        item.setListVisible(1);
        item.setFormVisible(1);
        item.setStatus(0);
        request.setColumns(List.of(item));

        service.updateColumns(1L, request);

        verify(columnMapper).updateById(existingColumn);
        assertThat(existingColumn.getJavaField()).isEqualTo("userDisplayName");
    }

    @Test
    void shouldThrowWhenTableMissing() {
        CodegenTableMapper tableMapper = Mockito.mock(CodegenTableMapper.class);
        CodegenColumnMapper columnMapper = Mockito.mock(CodegenColumnMapper.class);
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        when(tableMapper.selectById(anyLong())).thenReturn(null);
        CodegenMetadataService service = new CodegenMetadataServiceImpl(tableMapper, columnMapper, jdbcTemplate);

        assertThatThrownBy(() -> service.getTable(99L))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("404 NOT_FOUND");
    }

    private CodegenTableDO table() {
        CodegenTableDO table = new CodegenTableDO();
        table.setId(1L);
        table.setTenantId(1L);
        table.setPhysicalTableName("sys_user");
        table.setTableComment("System User");
        table.setBusinessName("user");
        table.setClassName("User");
        table.setModuleName("system");
        table.setPackageName("com.luckycolor.admin.modules.generated");
        table.setGenMode("crud");
        table.setColumnCount(1);
        table.setRemark("default");
        return table;
    }

    private CodegenColumnDO column() {
        CodegenColumnDO column = new CodegenColumnDO();
        column.setId(11L);
        column.setTableId(1L);
        column.setColumnName("user_name");
        column.setColumnComment("User Name");
        column.setJdbcType("varchar");
        column.setJavaType("String");
        column.setJavaField("userName");
        column.setHtmlType("input");
        column.setQueryType("LIKE");
        column.setRequired(1);
        column.setListVisible(1);
        column.setFormVisible(1);
        column.setStatus(0);
        return column;
    }
}
