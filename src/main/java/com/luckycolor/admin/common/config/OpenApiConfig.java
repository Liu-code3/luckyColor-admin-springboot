package com.luckycolor.admin.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

@Configuration
public class OpenApiConfig {

    private final BuildProperties buildProperties;

    public OpenApiConfig(@Nullable BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Bean
    OpenAPI luckyColorOpenApi() {
        final String schemeName = "bearerAuth";
        return new OpenAPI()
            .info(new Info()
                .title("LuckyColor 后台管理系统接口文档")
                .description("LuckyColor 后台管理系统 Spring Boot 版 API 文档")
                .version(resolveVersion()))
            .addSecurityItem(new SecurityRequirement().addList(schemeName))
            .components(new Components().addSecuritySchemes(
                schemeName,
                new SecurityScheme()
                    .name(schemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }

    @Bean
    OpenApiCustomizer openApiChineseCustomizer() {
        return new ChineseOpenApiCustomizer();
    }

    private String resolveVersion() {
        return buildProperties == null ? "unknown" : buildProperties.getVersion();
    }
}

final class ChineseOpenApiCustomizer implements OpenApiCustomizer {

    private static final Map<String, String> TEXT_MAP = Map.ofEntries(
        Map.entry("LuckyColor Admin Spring Boot API", "LuckyColor 后台管理系统接口文档"),
        Map.entry("Spring Boot rewrite for the LuckyColor admin backend", "LuckyColor 后台管理系统 Spring Boot 版 API 文档"),
        Map.entry("Tenants", "租户管理"),
        Map.entry("Tenant Packages", "租户套餐"),
        Map.entry("Tenant Audit Logs", "租户审计日志"),
        Map.entry("Tenant Bootstrap", "租户初始化"),
        Map.entry("Authentication", "认证鉴权"),
        Map.entry("Watermark", "水印配置"),
        Map.entry("Security Audit Logs", "安全审计日志"),
        Map.entry("System Users", "系统用户"),
        Map.entry("File Storage", "文件存储"),
        Map.entry("Dashboard", "仪表盘"),
        Map.entry("User Preferences", "用户偏好"),
        Map.entry("I18n Resources", "国际化资源"),
        Map.entry("Platform Info", "平台信息"),
        Map.entry("System Menus", "系统菜单"),
        Map.entry("System Departments", "系统部门"),
        Map.entry("Dictionary Types", "字典类型"),
        Map.entry("System Roles", "系统角色"),
        Map.entry("System Configs", "系统参数"),
        Map.entry("Code Generation", "代码生成"),
        Map.entry("Dictionary Items", "字典项"),
        Map.entry("Dictionary Catalog", "字典目录"),
        Map.entry("Operation Logs", "操作日志"),
        Map.entry("System Notices", "系统通知"),
        Map.entry("Tenant management APIs", "租户管理接口"),
        Map.entry("Tenant package management APIs", "租户套餐管理接口"),
        Map.entry("Tenant audit log query APIs", "租户审计日志查询接口"),
        Map.entry("Tenant bootstrap template and execution APIs", "租户初始化模板与执行接口"),
        Map.entry("Login and current-session APIs", "登录与当前会话接口"),
        Map.entry("Watermark configuration APIs", "水印配置接口"),
        Map.entry("Security audit log query APIs", "安全审计日志查询接口"),
        Map.entry("System user management APIs", "系统用户管理接口"),
        Map.entry("Upload and download file APIs", "文件上传下载接口"),
        Map.entry("Dashboard overview and visit analytics APIs", "仪表盘概览与访问分析接口"),
        Map.entry("Current user preference APIs", "当前用户偏好接口"),
        Map.entry("Internationalization resource management APIs", "国际化资源管理接口"),
        Map.entry("Platform health and version APIs", "平台健康与版本接口"),
        Map.entry("System menu management APIs", "系统菜单管理接口"),
        Map.entry("Department management APIs", "部门管理接口"),
        Map.entry("Dictionary type management APIs", "字典类型管理接口"),
        Map.entry("Role and permission assignment APIs", "角色与权限分配接口"),
        Map.entry("System configuration management APIs", "系统参数管理接口"),
        Map.entry("Code generation metadata APIs", "代码生成元数据接口"),
        Map.entry("Dictionary item management APIs", "字典项管理接口"),
        Map.entry("Dictionary catalog and cache APIs", "字典目录与缓存接口"),
        Map.entry("Operation log query APIs", "操作日志查询接口"),
        Map.entry("Notice management APIs", "通知公告管理接口"),
        Map.entry("Get application version information", "获取应用版本信息"),
        Map.entry("Page security audit logs", "分页查询安全审计日志"),
        Map.entry("Get current watermark configuration", "获取当前水印配置"),
        Map.entry("Update current watermark configuration", "更新当前水印配置"),
        Map.entry("Page tenants", "分页查询租户"),
        Map.entry("Get tenant detail", "获取租户详情"),
        Map.entry("Create tenant", "创建租户"),
        Map.entry("Update tenant", "更新租户"),
        Map.entry("Update tenant status", "更新租户状态"),
        Map.entry("Update tenant expiration time", "更新租户过期时间"),
        Map.entry("Get application health status", "获取应用健康状态"),
        Map.entry("List department tree", "获取部门树"),
        Map.entry("Get department detail", "获取部门详情"),
        Map.entry("Create department", "创建部门"),
        Map.entry("Update department", "更新部门"),
        Map.entry("Update department status", "更新部门状态"),
        Map.entry("Delete department", "删除部门"),
        Map.entry("Page tenant audit logs", "分页查询租户审计日志"),
        Map.entry("Page tenant packages", "分页查询租户套餐"),
        Map.entry("Get tenant package detail", "获取租户套餐详情"),
        Map.entry("Create tenant package", "创建租户套餐"),
        Map.entry("Update tenant package", "更新租户套餐"),
        Map.entry("Update tenant package status", "更新租户套餐状态"),
        Map.entry("Get current user preferences", "获取当前用户偏好"),
        Map.entry("Update current user preferences", "更新当前用户偏好"),
        Map.entry("List tenant bootstrap templates", "获取租户初始化模板列表"),
        Map.entry("Page tenant bootstrap records", "分页查询租户初始化记录"),
        Map.entry("Bootstrap a tenant", "执行租户初始化"),
        Map.entry("Get login captcha", "获取登录验证码"),
        Map.entry("Login with username and password", "使用用户名密码登录"),
        Map.entry("Logout current session", "退出当前会话"),
        Map.entry("Get current user profile", "获取当前用户信息"),
        Map.entry("Get current user permissions", "获取当前用户权限"),
        Map.entry("Get current user routes", "获取当前用户路由"),
        Map.entry("Get current user access snapshot", "获取当前用户访问快照"),
        Map.entry("Page system users", "分页查询系统用户"),
        Map.entry("Get system user detail", "获取系统用户详情"),
        Map.entry("List available role options", "获取可选角色列表"),
        Map.entry("Preview exported system users", "预览导出系统用户"),
        Map.entry("Create system user", "创建系统用户"),
        Map.entry("Update system user", "更新系统用户"),
        Map.entry("Update system user status", "更新系统用户状态"),
        Map.entry("Delete system user", "删除系统用户"),
        Map.entry("Reset system user password", "重置系统用户密码"),
        Map.entry("Assign roles to system user", "为系统用户分配角色"),
        Map.entry("Export system users as CSV", "导出系统用户 CSV"),
        Map.entry("Import system users from file", "从文件导入系统用户"),
        Map.entry("Page dictionary types", "分页查询字典类型"),
        Map.entry("Get dictionary type detail", "获取字典类型详情"),
        Map.entry("Create dictionary type", "创建字典类型"),
        Map.entry("Update dictionary type", "更新字典类型"),
        Map.entry("Delete dictionary type", "删除字典类型"),
        Map.entry("Get dashboard overview", "获取仪表盘概览"),
        Map.entry("Get dashboard visit trend", "获取仪表盘访问趋势"),
        Map.entry("Get recent visits", "获取最近访问记录"),
        Map.entry("Page system configs", "分页查询系统参数"),
        Map.entry("Get system config detail", "获取系统参数详情"),
        Map.entry("Create system config", "创建系统参数"),
        Map.entry("Update system config", "更新系统参数"),
        Map.entry("List items by dictionary type", "按字典类型获取字典项"),
        Map.entry("Batch query dictionary catalog", "批量查询字典目录"),
        Map.entry("Refresh dictionary cache", "刷新字典缓存"),
        Map.entry("Page i18n resources", "分页查询国际化资源"),
        Map.entry("Get i18n resource detail", "获取国际化资源详情"),
        Map.entry("Create i18n resource", "创建国际化资源"),
        Map.entry("Update i18n resource", "更新国际化资源"),
        Map.entry("Update i18n resource status", "更新国际化资源状态"),
        Map.entry("Increase i18n resource version", "递增国际化资源版本"),
        Map.entry("List dictionary item tree", "获取字典项树"),
        Map.entry("Get dictionary item detail", "获取字典项详情"),
        Map.entry("Create dictionary item", "创建字典项"),
        Map.entry("Update dictionary item", "更新字典项"),
        Map.entry("Delete dictionary item", "删除字典项"),
        Map.entry("Discover database tables for code generation", "发现可用于代码生成的数据表"),
        Map.entry("Import tables into code generation metadata", "导入数据表到代码生成元数据"),
        Map.entry("Page code generation tables", "分页查询代码生成数据表"),
        Map.entry("Get code generation table detail", "获取代码生成数据表详情"),
        Map.entry("Update code generation table metadata", "更新代码生成数据表元数据"),
        Map.entry("Update code generation columns", "更新代码生成字段配置"),
        Map.entry("Page notices", "分页查询通知公告"),
        Map.entry("Get notice detail", "获取通知公告详情"),
        Map.entry("Create notice", "创建通知公告"),
        Map.entry("Update notice", "更新通知公告"),
        Map.entry("Publish notice", "发布通知公告"),
        Map.entry("List system menu tree", "获取系统菜单树"),
        Map.entry("Get system menu detail", "获取系统菜单详情"),
        Map.entry("Create system menu", "创建系统菜单"),
        Map.entry("Update system menu", "更新系统菜单"),
        Map.entry("Update system menu status", "更新系统菜单状态"),
        Map.entry("Delete system menu", "删除系统菜单"),
        Map.entry("Page operation logs", "分页查询操作日志"),
        Map.entry("Upload a file", "上传文件"),
        Map.entry("Download a file by storage path", "按存储路径下载文件"),
        Map.entry("Page system roles", "分页查询系统角色"),
        Map.entry("Get system role detail", "获取系统角色详情"),
        Map.entry("Create system role", "创建系统角色"),
        Map.entry("Update system role", "更新系统角色"),
        Map.entry("Update system role status", "更新系统角色状态"),
        Map.entry("Get role authority snapshot", "获取角色权限快照"),
        Map.entry("Update role authority", "更新角色权限"),
        Map.entry("Standard API response envelope", "通用接口响应体"),
        Map.entry("Business status code", "业务状态码"),
        Map.entry("Response message", "响应消息"),
        Map.entry("Business payload", "业务数据"),
        Map.entry("Response timestamp in UTC", "响应时间（UTC）"),
        Map.entry("Generic paged result", "通用分页结果"),
        Map.entry("Current page records", "当前页记录"),
        Map.entry("Total record count", "记录总数"),
        Map.entry("Page number, starts from 1", "页码，从 1 开始"),
        Map.entry("Page size, max 200", "每页条数，最大 200"),
        Map.entry("Tenant list item", "租户列表项"),
        Map.entry("Tenant ID", "租户 ID"),
        Map.entry("Tenant name", "租户名称"),
        Map.entry("Tenant package ID", "租户套餐 ID"),
        Map.entry("Contact name", "联系人"),
        Map.entry("Contact mobile", "联系电话"),
        Map.entry("Allowed account count", "可用账号数量"),
        Map.entry("Expiration time", "过期时间"),
        Map.entry("Status: 0 enabled, 1 disabled", "状态：0 启用，1 停用"),
        Map.entry("Code generation table paging query", "代码生成数据表分页查询条件"),
        Map.entry("Filter by physical table name", "按物理表名筛选"),
        Map.entry("Filter by business name", "按业务名称筛选"),
        Map.entry("System user list item", "系统用户列表项"),
        Map.entry("User ID", "用户 ID"),
        Map.entry("Username", "用户名"),
        Map.entry("Nickname", "昵称"),
        Map.entry("Email", "邮箱"),
        Map.entry("Mobile number", "手机号"),
        Map.entry("Department ID", "部门 ID"),
        Map.entry("Role codes", "角色编码列表"),
        Map.entry("System user detail", "系统用户详情"),
        Map.entry("Primary department ID", "主部门 ID"),
        Map.entry("Permission codes", "权限编码列表"),
        Map.entry("Data scope strategy", "数据权限范围策略"),
        Map.entry("Department scope IDs", "部门权限范围 ID 列表"),
        Map.entry("Tenant scope IDs", "租户权限范围 ID 列表"),
        Map.entry("Remark", "备注"),
        Map.entry("Tenant detail", "租户详情"),
        Map.entry("Create or update system user request", "创建或更新系统用户请求"),
        Map.entry("Password, required when creating", "密码，创建时必填"),
        Map.entry("Email address", "邮箱地址"),
        Map.entry("System user paging query", "系统用户分页查询条件"),
        Map.entry("Filter by username", "按用户名筛选"),
        Map.entry("Filter by nickname", "按昵称筛选"),
        Map.entry("Filter by mobile number", "按手机号筛选"),
        Map.entry("Filter by status: 0 enabled, 1 disabled", "按状态筛选：0 启用，1 停用"),
        Map.entry("Create or update tenant request", "创建或更新租户请求"),
        Map.entry("Tenant paging query", "租户分页查询条件"),
        Map.entry("Filter by tenant name", "按租户名称筛选"),
        Map.entry("Filter by package ID", "按套餐 ID 筛选"),
        Map.entry("Login captcha payload", "登录验证码响应数据"),
        Map.entry("Captcha key", "验证码键"),
        Map.entry("Captcha image as base64 data URL", "Base64 Data URL 格式的验证码图片"),
        Map.entry("Current user profile", "当前用户信息"),
        Map.entry("Current user ID", "当前用户 ID"),
        Map.entry("Current username", "当前用户名"),
        Map.entry("Current nickname", "当前用户昵称"),
        Map.entry("Current tenant ID", "当前租户 ID"),
        Map.entry("Current user roles", "当前用户角色"),
        Map.entry("Current user role and permission snapshot", "当前用户角色与权限快照"),
        Map.entry("Current user permissions", "当前用户权限"),
        Map.entry("Login result payload", "登录结果数据"),
        Map.entry("JWT access token", "JWT 访问令牌"),
        Map.entry("Token type", "令牌类型"),
        Map.entry("Token expiration in seconds", "令牌过期时间（秒）"),
        Map.entry("Current user access snapshot", "当前用户访问快照"),
        Map.entry("Accessible route codes", "可访问路由编码"),
        Map.entry("Frontend home path", "前端首页路径"),
        Map.entry("Uploaded file metadata", "已上传文件元数据"),
        Map.entry("Original filename", "原始文件名"),
        Map.entry("Stored filename", "存储后的文件名"),
        Map.entry("Relative storage path", "相对存储路径"),
        Map.entry("File size in bytes", "文件大小（字节）"),
        Map.entry("Detected content type", "识别出的内容类型"),
        Map.entry("Download URL", "下载地址"),
        Map.entry("Username and password login request", "用户名密码登录请求"),
        Map.entry("Login username", "登录用户名"),
        Map.entry("Login password", "登录密码"),
        Map.entry("Captcha key returned by captcha API", "验证码接口返回的键"),
        Map.entry("Captcha code entered by user", "用户输入的验证码"),
        Map.entry("Client IP, populated by server", "客户端 IP，由服务端填充"),
        Map.entry("System config paging query", "系统参数分页查询条件"),
        Map.entry("Filter by config key", "按参数键筛选"),
        Map.entry("Filter by config name", "按参数名称筛选"),
        Map.entry("Filter by sensitivity flag: 0 no, 1 yes", "按敏感标识筛选：0 否，1 是"),
        Map.entry("ok", "成功"),
        Map.entry("LuckyColor Demo", "LuckyColor 演示租户"),
        Map.entry("System Admin", "系统管理员"),
        Map.entry("Site Title", "网站标题"),
        Map.entry("System User", "系统用户"),
        Map.entry("Authentication required", "需要登录认证"),
        Map.entry("Permission denied", "无权限访问"),
        Map.entry("Validation failed", "请求参数校验失败"),
        Map.entry("User created successfully", "创建用户成功"),
        Map.entry("CSV export stream", "CSV 导出文件流"),
        Map.entry("Import completed successfully", "导入成功"),
        Map.entry("Missing file or invalid import file", "缺少上传文件或导入文件不合法"),
        Map.entry("Upload successful", "上传成功"),
        Map.entry("Missing file or invalid multipart request", "缺少上传文件或 multipart 请求不合法"),
        Map.entry("Binary file stream", "二进制文件流"),
        Map.entry("Missing or invalid path parameter", "缺少 path 参数或参数不合法"),
        Map.entry("Captcha generated successfully", "验证码生成成功"),
        Map.entry("Captcha service unavailable", "验证码服务不可用"),
        Map.entry("Login successful", "登录成功"),
        Map.entry("Invalid request body or validation failed", "请求体不合法或参数校验失败"),
        Map.entry("Username, password, or captcha incorrect", "用户名、密码或验证码错误"),
        Map.entry("Logout successful", "退出登录成功"),
        Map.entry("Profile loaded", "用户信息获取成功"),
        Map.entry("Permission snapshot loaded", "权限快照获取成功"),
        Map.entry("Route list loaded", "路由列表获取成功"),
        Map.entry("Access snapshot loaded", "访问快照获取成功"),
        Map.entry("Dashboard overview loaded", "仪表盘概览获取成功"),
        Map.entry("Dashboard visit trend loaded", "访问趋势获取成功"),
        Map.entry("Recent visits loaded", "最近访问记录获取成功"),
        Map.entry("Operation logs loaded", "操作日志获取成功"),
        Map.entry("Security audit logs loaded", "安全审计日志获取成功"),
        Map.entry("Tenant audit logs loaded", "租户审计日志获取成功"),
        Map.entry("Current user preferences loaded", "当前用户偏好获取成功"),
        Map.entry("Current user preferences updated", "当前用户偏好更新成功"),
        Map.entry("Current watermark configuration loaded", "当前水印配置获取成功"),
        Map.entry("Current watermark configuration updated", "当前水印配置更新成功"),
        Map.entry("Tenant bootstrap templates loaded", "租户初始化模板获取成功"),
        Map.entry("Tenant bootstrap records loaded", "租户初始化记录获取成功"),
        Map.entry("Tenant bootstrapped successfully", "租户初始化执行成功"),
        Map.entry("Dictionary types loaded", "字典类型列表获取成功"),
        Map.entry("Dictionary type detail loaded", "字典类型详情获取成功"),
        Map.entry("Dictionary type created successfully", "字典类型创建成功"),
        Map.entry("Dictionary type updated successfully", "字典类型更新成功"),
        Map.entry("Dictionary type deleted successfully", "字典类型删除成功"),
        Map.entry("Dictionary item tree loaded", "字典项树获取成功"),
        Map.entry("Dictionary item detail loaded", "字典项详情获取成功"),
        Map.entry("Dictionary item created successfully", "字典项创建成功"),
        Map.entry("Dictionary item updated successfully", "字典项更新成功"),
        Map.entry("Dictionary item deleted successfully", "字典项删除成功"),
        Map.entry("Dictionary items loaded", "字典项列表获取成功"),
        Map.entry("Dictionary catalog loaded", "字典目录获取成功"),
        Map.entry("Dictionary cache refreshed", "字典缓存刷新成功"),
        Map.entry("I18n resources loaded", "国际化资源列表获取成功"),
        Map.entry("I18n resource detail loaded", "国际化资源详情获取成功"),
        Map.entry("I18n resource created successfully", "国际化资源创建成功"),
        Map.entry("I18n resource updated successfully", "国际化资源更新成功"),
        Map.entry("I18n resource status updated", "国际化资源状态更新成功"),
        Map.entry("I18n resource version increased", "国际化资源版本递增成功"),
        Map.entry("Discovery table list loaded", "待生成数据表列表获取成功"),
        Map.entry("Tables imported successfully", "数据表导入成功"),
        Map.entry("Code generation tables loaded", "代码生成数据表列表获取成功"),
        Map.entry("Code generation table detail loaded", "代码生成数据表详情获取成功"),
        Map.entry("Code generation table metadata updated", "代码生成数据表元数据更新成功"),
        Map.entry("Code generation columns updated", "代码生成字段配置更新成功"),
        Map.entry("System config detail loaded", "系统参数详情获取成功"),
        Map.entry("System config created successfully", "系统参数创建成功"),
        Map.entry("System config not found", "系统参数不存在"),
        Map.entry("System config key already exists", "系统参数键已存在"),
        Map.entry("Tenant not found", "租户不存在"),
        Map.entry("Dictionary type not found", "字典类型不存在"),
        Map.entry("Dictionary item not found", "字典项不存在"),
        Map.entry("I18n resource not found", "国际化资源不存在"),
        Map.entry("Code generation table not found", "代码生成数据表不存在"),
        Map.entry("File not found", "文件不存在"),
        Map.entry("Username already exists", "用户名已存在")
    );

    @Override
    public void customise(OpenAPI openApi) {
        if (openApi.getInfo() != null) {
            openApi.getInfo()
                .title(translateText(openApi.getInfo().getTitle()))
                .description(translateText(openApi.getInfo().getDescription()));
        }
        if (openApi.getTags() != null) {
            openApi.setTags(openApi.getTags().stream().map(this::translateTag).collect(Collectors.toList()));
        }
        if (openApi.getPaths() != null) {
            openApi.getPaths().values().forEach(this::translatePathItem);
        }
        if (openApi.getComponents() != null && openApi.getComponents().getSchemas() != null) {
            openApi.getComponents().getSchemas().values().forEach(schema -> translateSchema(schema, newVisitedSet()));
        }
        if (openApi.getComponents() != null && openApi.getComponents().getExamples() != null) {
            openApi.getComponents().getExamples().values().forEach(this::translateExample);
        }
    }

    private Tag translateTag(Tag tag) {
        if (tag == null) {
            return null;
        }
        tag.setName(translateText(tag.getName()));
        tag.setDescription(translateText(tag.getDescription()));
        return tag;
    }

    private void translatePathItem(PathItem pathItem) {
        if (pathItem == null) {
            return;
        }
        pathItem.readOperations().forEach(operation -> {
            operation.setSummary(translateText(operation.getSummary()));
            operation.setDescription(translateText(operation.getDescription()));
            if (operation.getTags() != null) {
                operation.setTags(operation.getTags().stream().map(this::translateText).collect(Collectors.toList()));
            }
            if (operation.getParameters() != null) {
                operation.getParameters().forEach(this::translateParameter);
            }
            translateRequestBody(operation.getRequestBody());
            translateResponses(operation.getResponses());
        });
    }

    private void translateParameter(Parameter parameter) {
        if (parameter == null) {
            return;
        }
        parameter.setDescription(translateText(parameter.getDescription()));
        if (parameter.getExample() != null) {
            parameter.setExample(translateObject(parameter.getExample()));
        }
        if (parameter.getExamples() != null) {
            parameter.getExamples().values().forEach(this::translateExample);
        }
        if (parameter.getSchema() != null) {
            translateSchema(parameter.getSchema(), newVisitedSet());
        }
    }

    private void translateRequestBody(RequestBody requestBody) {
        if (requestBody == null) {
            return;
        }
        requestBody.setDescription(translateText(requestBody.getDescription()));
        translateContent(requestBody.getContent());
    }

    private void translateResponses(ApiResponses responses) {
        if (responses == null) {
            return;
        }
        responses.values().forEach(response -> {
            response.setDescription(translateText(response.getDescription()));
            translateContent(response.getContent());
        });
    }

    private void translateContent(Content content) {
        if (content == null) {
            return;
        }
        content.values().forEach(mediaType -> {
            if (mediaType.getExample() != null) {
                mediaType.setExample(translateObject(mediaType.getExample()));
            }
            if (mediaType.getExamples() != null) {
                mediaType.getExamples().values().forEach(this::translateExample);
            }
            if (mediaType.getSchema() != null) {
                translateSchema(mediaType.getSchema(), newVisitedSet());
            }
        });
    }

    private void translateExample(Example example) {
        if (example == null) {
            return;
        }
        example.setSummary(translateText(example.getSummary()));
        example.setDescription(translateText(example.getDescription()));
        example.setValue(translateObject(example.getValue()));
    }

    private void translateSchema(Schema<?> schema, Set<Schema<?>> visited) {
        if (schema == null || visited.contains(schema)) {
            return;
        }
        visited.add(schema);
        schema.setTitle(translateText(schema.getTitle()));
        schema.setDescription(translateText(schema.getDescription()));
        if (schema.getExample() != null) {
            schema.setExample(translateObject(schema.getExample()));
        }
        if (schema.getProperties() != null) {
            schema.getProperties().values().forEach(property -> {
                if (property instanceof Schema<?> nestedSchema) {
                    translateSchema(nestedSchema, visited);
                }
            });
        }
        if (schema.getItems() != null) {
            translateSchema(schema.getItems(), visited);
        }
        if (schema.getAdditionalProperties() instanceof Schema<?> additionalSchema) {
            translateSchema(additionalSchema, visited);
        }
        translateSchemaList(schema.getAllOf(), visited);
        translateSchemaList(schema.getOneOf(), visited);
        translateSchemaList(schema.getAnyOf(), visited);
        if (schema.getNot() != null) {
            translateSchema(schema.getNot(), visited);
        }
    }

    private void translateSchemaList(List<Schema> schemas, Set<Schema<?>> visited) {
        if (schemas == null) {
            return;
        }
        schemas.forEach(schema -> translateSchema(schema, visited));
    }

    private Object translateObject(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String text) {
            return translateText(text);
        }
        if (value instanceof Map<?, ?> map) {
            return map.entrySet().stream().collect(Collectors.toMap(
                entry -> entry.getKey(),
                entry -> translateObject(entry.getValue()),
                (left, right) -> right
            ));
        }
        if (value instanceof List<?> list) {
            return list.stream().map(this::translateObject).collect(Collectors.toList());
        }
        return value;
    }

    private String translateText(String text) {
        if (text == null) {
            return null;
        }
        return TEXT_MAP.getOrDefault(text, text);
    }

    private Set<Schema<?>> newVisitedSet() {
        return Collections.newSetFromMap(new IdentityHashMap<>());
    }
}
