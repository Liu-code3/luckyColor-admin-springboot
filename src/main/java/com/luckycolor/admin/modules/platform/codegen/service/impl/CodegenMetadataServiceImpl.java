package com.luckycolor.admin.modules.platform.codegen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.platform.codegen.dataobject.CodegenColumnDO;
import com.luckycolor.admin.modules.platform.codegen.dataobject.CodegenTableDO;
import com.luckycolor.admin.modules.platform.codegen.mapper.CodegenColumnMapper;
import com.luckycolor.admin.modules.platform.codegen.mapper.CodegenTableMapper;
import com.luckycolor.admin.modules.platform.codegen.service.CodegenMetadataService;
import com.luckycolor.admin.modules.platform.codegen.web.request.CodegenColumnSaveItem;
import com.luckycolor.admin.modules.platform.codegen.web.request.CodegenColumnsSaveRequest;
import com.luckycolor.admin.modules.platform.codegen.web.request.CodegenDiscoveryQuery;
import com.luckycolor.admin.modules.platform.codegen.web.request.CodegenImportRequest;
import com.luckycolor.admin.modules.platform.codegen.web.request.CodegenTablePageQuery;
import com.luckycolor.admin.modules.platform.codegen.web.request.CodegenTableSaveRequest;
import com.luckycolor.admin.modules.platform.codegen.web.response.CodegenColumnResponse;
import com.luckycolor.admin.modules.platform.codegen.web.response.CodegenDiscoveryTableResponse;
import com.luckycolor.admin.modules.platform.codegen.web.response.CodegenTableDetailResponse;
import com.luckycolor.admin.modules.platform.codegen.web.response.CodegenTablePageResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import com.luckycolor.admin.common.config.ConditionalOnPersistenceEnabled;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@ConditionalOnPersistenceEnabled
public class CodegenMetadataServiceImpl implements CodegenMetadataService {

    private static final String DISCOVERY_SQL = """
        SELECT TABLE_NAME AS table_name, TABLE_COMMENT AS table_comment
        FROM INFORMATION_SCHEMA.TABLES
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_TYPE = 'BASE TABLE'
        ORDER BY TABLE_NAME
        """;

    private static final String TABLE_SQL = """
        SELECT TABLE_NAME AS table_name, TABLE_COMMENT AS table_comment
        FROM INFORMATION_SCHEMA.TABLES
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_TYPE = 'BASE TABLE'
          AND TABLE_NAME = ?
        """;

    private static final String COLUMN_SQL = """
        SELECT COLUMN_NAME AS column_name,
               COLUMN_COMMENT AS column_comment,
               DATA_TYPE AS data_type,
               COLUMN_KEY AS column_key,
               IS_NULLABLE AS is_nullable,
               ORDINAL_POSITION AS ordinal_position
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = ?
        ORDER BY ORDINAL_POSITION
        """;

    private static final Set<String> PREFIXES = Set.of("sys_", "biz_", "lc_");

    private final CodegenTableMapper codegenTableMapper;
    private final CodegenColumnMapper codegenColumnMapper;
    private final JdbcTemplate jdbcTemplate;

    public CodegenMetadataServiceImpl(
        CodegenTableMapper codegenTableMapper,
        CodegenColumnMapper codegenColumnMapper,
        JdbcTemplate jdbcTemplate
    ) {
        this.codegenTableMapper = codegenTableMapper;
        this.codegenColumnMapper = codegenColumnMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public PageResult<CodegenDiscoveryTableResponse> pageDiscoveryTables(CodegenDiscoveryQuery query) {
        List<CodegenDiscoveryTableResponse> tables = jdbcTemplate.queryForList(DISCOVERY_SQL).stream()
            .map(this::toDiscoveryResponse)
            .filter(item -> matchesDiscoveryQuery(item, query))
            .toList();
        return slicePage(query, tables);
    }

    @Override
    @Transactional
    public int importTables(Long tenantId, CodegenImportRequest request) {
        List<String> tableNames = request.getTableNames().stream()
            .filter(StringUtils::hasText)
            .map(String::trim)
            .distinct()
            .toList();
        if (tableNames.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Table names are required");
        }
        ensureTablesNotImported(tenantId, tableNames);
        int importedCount = 0;
        for (String tableName : tableNames) {
            Map<String, Object> tableMetadata = querySingleTable(tableName);
            CodegenTableDO codegenTable = new CodegenTableDO();
            codegenTable.setTenantId(tenantId);
            codegenTable.setPhysicalTableName(tableName);
            codegenTable.setTableComment(stringValue(tableMetadata.get("table_comment")));
            codegenTable.setBusinessName(resolveBusinessName(tableName));
            codegenTable.setClassName(resolveClassName(tableName));
            codegenTable.setModuleName(resolveModuleName(tableName));
            codegenTable.setPackageName("com.luckycolor.admin.modules.generated");
            codegenTable.setGenMode("crud");
            List<CodegenColumnDO> columns = buildColumns(tenantId, codegenTable, tableName);
            codegenTable.setColumnCount(columns.size());
            codegenTableMapper.insert(codegenTable);
            columns.forEach(column -> {
                column.setTableId(codegenTable.getId());
                codegenColumnMapper.insert(column);
            });
            importedCount++;
        }
        return importedCount;
    }

    @Override
    public PageResult<CodegenTablePageResponse> pageTables(CodegenTablePageQuery query) {
        LambdaQueryWrapper<CodegenTableDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(
            StringUtils.hasText(query.getPhysicalTableName()),
            CodegenTableDO::getPhysicalTableName,
            trim(query.getPhysicalTableName())
        );
        queryWrapper.like(
            StringUtils.hasText(query.getBusinessName()),
            CodegenTableDO::getBusinessName,
            trim(query.getBusinessName())
        );
        queryWrapper.orderByDesc(CodegenTableDO::getCreateTime);
        PageResult<CodegenTableDO> pageResult = codegenTableMapper.selectPageResult(query, queryWrapper);
        return PageResult.of(pageResult.getList().stream().map(this::toPageResponse).toList(), pageResult.getTotal());
    }

    @Override
    public CodegenTableDetailResponse getTable(Long id) {
        CodegenTableDO codegenTable = getRequiredTable(id);
        return toDetailResponse(codegenTable, listColumns(codegenTable.getId()));
    }

    @Override
    public void updateTable(Long id, CodegenTableSaveRequest request) {
        CodegenTableDO codegenTable = getRequiredTable(id);
        codegenTable.setBusinessName(trim(request.getBusinessName()));
        codegenTable.setClassName(trim(request.getClassName()));
        codegenTable.setModuleName(trim(request.getModuleName()));
        codegenTable.setPackageName(trim(request.getPackageName()));
        codegenTable.setGenMode(trim(request.getGenMode()));
        codegenTable.setRemark(request.getRemark());
        codegenTableMapper.updateById(codegenTable);
    }

    @Override
    public void updateColumns(Long id, CodegenColumnsSaveRequest request) {
        CodegenTableDO codegenTable = getRequiredTable(id);
        Map<Long, CodegenColumnDO> currentColumns = listColumns(codegenTable.getId()).stream()
            .collect(Collectors.toMap(CodegenColumnDO::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
        for (CodegenColumnSaveItem item : request.getColumns()) {
            CodegenColumnDO column = currentColumns.get(item.getId());
            if (column == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Codegen column not found");
            }
            column.setJavaType(trim(item.getJavaType()));
            column.setJavaField(trim(item.getJavaField()));
            column.setHtmlType(trim(item.getHtmlType()));
            column.setQueryType(trim(item.getQueryType()));
            column.setRequired(item.getRequired());
            column.setListVisible(item.getListVisible());
            column.setFormVisible(item.getFormVisible());
            column.setStatus(item.getStatus());
            codegenColumnMapper.updateById(column);
        }
    }

    private void ensureTablesNotImported(Long tenantId, List<String> tableNames) {
        LambdaQueryWrapper<CodegenTableDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CodegenTableDO::getPhysicalTableName, tableNames);
        if (tenantId == null) {
            queryWrapper.isNull(CodegenTableDO::getTenantId);
        } else {
            queryWrapper.eq(CodegenTableDO::getTenantId, tenantId);
        }
        List<CodegenTableDO> existingTables = codegenTableMapper.selectList(queryWrapper);
        if (!existingTables.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Codegen table already imported");
        }
    }

    private Map<String, Object> querySingleTable(String tableName) {
        List<Map<String, Object>> tables = jdbcTemplate.queryForList(TABLE_SQL, tableName);
        if (tables.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Database table not found");
        }
        return tables.get(0);
    }

    private List<CodegenColumnDO> buildColumns(Long tenantId, CodegenTableDO table, String tableName) {
        List<Map<String, Object>> columnRows = jdbcTemplate.queryForList(COLUMN_SQL, tableName);
        List<CodegenColumnDO> columns = new ArrayList<>();
        for (Map<String, Object> row : columnRows) {
            String columnName = stringValue(row.get("column_name"));
            String jdbcType = stringValue(row.get("data_type"));
            String javaType = resolveJavaType(jdbcType);
            CodegenColumnDO column = new CodegenColumnDO();
            column.setTenantId(tenantId);
            column.setTableId(table.getId());
            column.setColumnName(columnName);
            column.setColumnComment(stringValue(row.get("column_comment")));
            column.setJdbcType(jdbcType);
            column.setJavaType(javaType);
            column.setJavaField(toCamelCase(columnName));
            column.setHtmlType(resolveHtmlType(columnName, jdbcType));
            column.setQueryType(resolveQueryType(columnName, jdbcType));
            column.setRequired(isRequired(row));
            column.setListVisible(isListVisible(columnName));
            column.setFormVisible(isFormVisible(columnName));
            column.setStatus(0);
            columns.add(column);
        }
        return columns;
    }

    private boolean matchesDiscoveryQuery(CodegenDiscoveryTableResponse item, CodegenDiscoveryQuery query) {
        return containsIgnoreCase(item.tableName(), trim(query.getTableName()))
            && containsIgnoreCase(item.tableComment(), trim(query.getTableComment()));
    }

    private <T> PageResult<T> slicePage(CodegenDiscoveryQuery query, List<T> items) {
        long pageNo = query.resolvePageNo();
        long pageSize = query.resolvePageSize();
        int fromIndex = (int) Math.min((pageNo - 1) * pageSize, items.size());
        int toIndex = (int) Math.min(fromIndex + pageSize, items.size());
        return PageResult.of(items.subList(fromIndex, toIndex), items.size());
    }

    private CodegenTableDO getRequiredTable(Long id) {
        CodegenTableDO codegenTable = codegenTableMapper.selectById(id);
        if (codegenTable == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Codegen table not found");
        }
        return codegenTable;
    }

    private List<CodegenColumnDO> listColumns(Long tableId) {
        LambdaQueryWrapper<CodegenColumnDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CodegenColumnDO::getTableId, tableId)
            .orderByAsc(CodegenColumnDO::getId);
        return codegenColumnMapper.selectList(queryWrapper);
    }

    private CodegenDiscoveryTableResponse toDiscoveryResponse(Map<String, Object> row) {
        return new CodegenDiscoveryTableResponse(
            stringValue(row.get("table_name")),
            stringValue(row.get("table_comment"))
        );
    }

    private CodegenTablePageResponse toPageResponse(CodegenTableDO codegenTable) {
        return new CodegenTablePageResponse(
            codegenTable.getId(),
            codegenTable.getTenantId(),
            codegenTable.getPhysicalTableName(),
            codegenTable.getTableComment(),
            codegenTable.getBusinessName(),
            codegenTable.getClassName(),
            codegenTable.getModuleName(),
            codegenTable.getPackageName(),
            codegenTable.getGenMode(),
            codegenTable.getColumnCount(),
            codegenTable.getRemark()
        );
    }

    private CodegenTableDetailResponse toDetailResponse(CodegenTableDO codegenTable, List<CodegenColumnDO> columns) {
        return new CodegenTableDetailResponse(
            codegenTable.getId(),
            codegenTable.getTenantId(),
            codegenTable.getPhysicalTableName(),
            codegenTable.getTableComment(),
            codegenTable.getBusinessName(),
            codegenTable.getClassName(),
            codegenTable.getModuleName(),
            codegenTable.getPackageName(),
            codegenTable.getGenMode(),
            codegenTable.getColumnCount(),
            codegenTable.getRemark(),
            columns.stream().map(this::toColumnResponse).toList()
        );
    }

    private CodegenColumnResponse toColumnResponse(CodegenColumnDO column) {
        return new CodegenColumnResponse(
            column.getId(),
            column.getColumnName(),
            column.getColumnComment(),
            column.getJdbcType(),
            column.getJavaType(),
            column.getJavaField(),
            column.getHtmlType(),
            column.getQueryType(),
            column.getRequired(),
            column.getListVisible(),
            column.getFormVisible(),
            column.getStatus()
        );
    }

    private String resolveBusinessName(String tableName) {
        String normalized = stripPrefix(tableName);
        int lastIndex = normalized.lastIndexOf('_');
        if (lastIndex < 0 || lastIndex == normalized.length() - 1) {
            return normalized;
        }
        return normalized.substring(lastIndex + 1);
    }

    private String resolveClassName(String tableName) {
        return toPascalCase(stripPrefix(tableName));
    }

    private String resolveModuleName(String tableName) {
        String normalized = stripPrefix(tableName);
        int firstIndex = normalized.indexOf('_');
        if (firstIndex < 0) {
            return "system";
        }
        return normalized.substring(0, firstIndex);
    }

    private String stripPrefix(String tableName) {
        if (!StringUtils.hasText(tableName)) {
            return tableName;
        }
        for (String prefix : PREFIXES) {
            if (tableName.startsWith(prefix)) {
                return tableName.substring(prefix.length());
            }
        }
        return tableName;
    }

    private String toPascalCase(String value) {
        String camelCase = toCamelCase(value);
        if (!StringUtils.hasText(camelCase)) {
            return camelCase;
        }
        return Character.toUpperCase(camelCase.charAt(0)) + camelCase.substring(1);
    }

    private String toCamelCase(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        String[] segments = value.split("_");
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < segments.length; index++) {
            String segment = segments[index].trim().toLowerCase();
            if (!StringUtils.hasText(segment)) {
                continue;
            }
            if (index == 0) {
                builder.append(segment);
                continue;
            }
            builder.append(Character.toUpperCase(segment.charAt(0)))
                .append(segment.substring(1));
        }
        return builder.toString();
    }

    private String resolveJavaType(String jdbcType) {
        if (!StringUtils.hasText(jdbcType)) {
            return "String";
        }
        return switch (jdbcType.toLowerCase()) {
            case "bigint" -> "Long";
            case "int", "integer", "tinyint", "smallint", "mediumint" -> "Integer";
            case "decimal", "numeric" -> "BigDecimal";
            case "double", "float" -> "Double";
            case "date" -> LocalDate.class.getSimpleName();
            case "datetime", "timestamp" -> LocalDateTime.class.getSimpleName();
            case "bit", "boolean" -> "Boolean";
            default -> "String";
        };
    }

    private String resolveHtmlType(String columnName, String jdbcType) {
        String lowerColumnName = safeLower(columnName);
        String lowerJdbcType = safeLower(jdbcType);
        if (lowerColumnName.contains("status") || lowerColumnName.endsWith("type")) {
            return "select";
        }
        if (lowerColumnName.contains("content") || lowerColumnName.contains("remark")) {
            return "textarea";
        }
        if (lowerJdbcType.equals("date") || lowerJdbcType.equals("datetime") || lowerJdbcType.equals("timestamp")) {
            return "datetime";
        }
        if (lowerJdbcType.equals("int")
            || lowerJdbcType.equals("integer")
            || lowerJdbcType.equals("tinyint")
            || lowerJdbcType.equals("smallint")
            || lowerJdbcType.equals("mediumint")
            || lowerJdbcType.equals("decimal")
            || lowerJdbcType.equals("numeric")
            || lowerJdbcType.equals("double")
            || lowerJdbcType.equals("float")
            || lowerJdbcType.equals("bigint")) {
            return "number";
        }
        return "input";
    }

    private String resolveQueryType(String columnName, String jdbcType) {
        String lowerColumnName = safeLower(columnName);
        String lowerJdbcType = safeLower(jdbcType);
        if (lowerColumnName.contains("status") || lowerColumnName.endsWith("id")) {
            return "EQ";
        }
        if (lowerJdbcType.equals("date") || lowerJdbcType.equals("datetime") || lowerJdbcType.equals("timestamp")) {
            return "BETWEEN";
        }
        return "LIKE";
    }

    private Integer isRequired(Map<String, Object> row) {
        return Objects.equals("NO", stringValue(row.get("is_nullable")))
            && !Objects.equals("PRI", stringValue(row.get("column_key"))) ? 1 : 0;
    }

    private Integer isListVisible(String columnName) {
        String lowerColumnName = safeLower(columnName);
        return isAuditColumn(lowerColumnName) || lowerColumnName.contains("password") ? 0 : 1;
    }

    private Integer isFormVisible(String columnName) {
        String lowerColumnName = safeLower(columnName);
        return lowerColumnName.equals("id") || isAuditColumn(lowerColumnName) ? 0 : 1;
    }

    private boolean isAuditColumn(String columnName) {
        return columnName.equals("create_by")
            || columnName.equals("create_time")
            || columnName.equals("update_by")
            || columnName.equals("update_time");
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        return safeLower(source).contains(keyword.toLowerCase());
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
