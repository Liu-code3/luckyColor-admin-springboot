INSERT IGNORE INTO sys_tenant_package (
    id, package_name, status, sort, remark, create_by, update_by
) VALUES (
    1, 'Default Package', 0, 1, 'Built-in seed package', 'seed', 'seed'
);

INSERT IGNORE INTO sys_tenant (
    id, name, package_id, contact_name, contact_mobile, account_count, expire_time, status, create_by, update_by
) VALUES (
    1, 'LuckyColor Default Tenant', 1, 'System Admin', '13800000000', 200,
    '2099-12-31 23:59:59', 0, 'seed', 'seed'
);

INSERT IGNORE INTO sys_department (
    id, tenant_id, parent_id, department_name, leader, phone, email, sort, status, remark, create_by, update_by
) VALUES
    (100, 1, 0, 'Headquarters', 'System Admin', '13800000000', 'admin@luckycolor.com', 1, 0, 'Built-in root department', 'seed', 'seed'),
    (101, 1, 100, 'Operations', 'Tenant Operator', '13800000001', 'ops@luckycolor.com', 1, 0, 'Built-in operations department', 'seed', 'seed');

INSERT IGNORE INTO sys_menu (
    id, parent_id, menu_name, menu_type, route_name, route_path, component, permission_code, role_codes, icon,
    sort, visible, keep_alive, always_show, status, remark, create_by, update_by
) VALUES
    (1, 0, 'Dashboard', 'MENU', 'Dashboard', '/dashboard', 'dashboard/index', 'dashboard:query', 'ROLE_SUPER_ADMIN,ROLE_TENANT_OPERATOR', 'dashboard', 1, 1, 1, 0, 0, 'Seed dashboard menu', 'seed', 'seed'),
    (10, 0, 'System', 'DIRECTORY', 'System', '/system', 'Layout', NULL, 'ROLE_SUPER_ADMIN', 'setting', 10, 1, 0, 1, 0, 'Seed system directory', 'seed', 'seed'),
    (11, 10, 'System User', 'MENU', 'SystemUser', 'users', 'system/user/index', 'system:user:query', 'ROLE_SUPER_ADMIN', NULL, 1, 1, 1, 0, 0, 'Seed system user menu', 'seed', 'seed'),
    (12, 10, 'System Role', 'MENU', 'SystemRole', 'roles', 'system/role/index', 'system:role:query', 'ROLE_SUPER_ADMIN', NULL, 2, 1, 1, 0, 0, 'Seed system role menu', 'seed', 'seed'),
    (13, 10, 'System Department', 'MENU', 'SystemDepartment', 'departments', 'system/department/index', 'system:department:query', 'ROLE_SUPER_ADMIN', NULL, 3, 1, 1, 0, 0, 'Seed department menu', 'seed', 'seed'),
    (14, 10, 'System Dictionary', 'MENU', 'SystemDictionary', 'dictionaries', 'system/dictionary/index', 'system:dictionary:query', 'ROLE_SUPER_ADMIN', NULL, 4, 1, 1, 0, 0, 'Seed dictionary menu', 'seed', 'seed'),
    (15, 10, 'System Config', 'MENU', 'SystemConfig', 'configs', 'system/config/index', 'system:config:query', 'ROLE_SUPER_ADMIN', NULL, 5, 1, 1, 0, 0, 'Seed config menu', 'seed', 'seed'),
    (16, 10, 'System Notice', 'MENU', 'SystemNotice', 'notices', 'system/notice/index', 'system:notice:query', 'ROLE_SUPER_ADMIN', NULL, 6, 1, 1, 0, 0, 'Seed notice menu', 'seed', 'seed'),
    (17, 10, 'System OperationLog', 'MENU', 'SystemOperationLog', 'operation-logs', 'system/log/index', 'system:operation-log:query', 'ROLE_SUPER_ADMIN', NULL, 7, 1, 1, 0, 0, 'Seed operation log menu', 'seed', 'seed'),
    (18, 10, 'System I18n', 'MENU', 'SystemI18n', 'i18n-resources', 'platform/i18n/index', 'i18n:query', 'ROLE_SUPER_ADMIN', NULL, 8, 1, 1, 0, 0, 'Seed i18n menu', 'seed', 'seed'),
    (19, 10, 'System Watermark', 'MENU', 'SystemWatermark', 'watermark', 'platform/watermark/index', 'watermark:query', 'ROLE_SUPER_ADMIN', NULL, 9, 1, 1, 0, 0, 'Seed watermark menu', 'seed', 'seed'),
    (20, 10, 'System Codegen', 'MENU', 'SystemCodegen', 'codegen', 'platform/codegen/index', 'codegen:query', 'ROLE_SUPER_ADMIN', NULL, 10, 1, 1, 0, 0, 'Seed codegen menu', 'seed', 'seed');

INSERT IGNORE INTO sys_role (
    id, tenant_id, role_code, role_name, status, sort, menu_ids, permission_codes, data_scope, department_id, department_ids,
    remark, create_by, update_by
) VALUES
    (
        1, 1, 'ROLE_SUPER_ADMIN', 'Super Admin', 0, 1,
        '1,10,11,12,13,14,15,16,17,18,19,20',
        'dashboard:query,file:upload,file:download,i18n:query,i18n:create,i18n:update,system:user:query,system:user:create,system:user:update,system:user:delete,system:user:reset-password,system:user:assign-role,system:user:import,system:user:export,system:menu:query,system:menu:create,system:menu:update,system:menu:delete,system:role:query,system:role:create,system:role:update,system:role:authorize,system:department:query,system:department:create,system:department:update,system:department:delete,system:dictionary:query,system:dictionary:create,system:dictionary:update,system:dictionary:delete,system:dictionary:refresh-cache,system:config:query,system:config:create,system:config:update,system:notice:query,system:notice:create,system:notice:update,system:notice:publish,system:operation-log:query,security:audit:query,tenant:query,tenant:create,tenant:update,tenant:package:query,tenant:package:create,tenant:package:update,watermark:query,watermark:update,codegen:query,codegen:create,codegen:update',
        'ALL', NULL, NULL,
        'Built-in super admin role', 'seed', 'seed'
    ),
    (
        2, 1, 'ROLE_TENANT_OPERATOR', 'Tenant Operator', 0, 2,
        '1',
        'dashboard:query,file:upload,file:download,i18n:query,tenant:query,tenant:package:query,watermark:query,watermark:update,codegen:query,codegen:create,codegen:update',
        'CUSTOM', 100, '100,101',
        'Built-in tenant operator role', 'seed', 'seed'
    ),
    (
        3, 1, 'tenant_admin', 'Tenant Admin', 0, 3,
        '1,11,12',
        'dashboard:query,system:user:query,system:user:create,system:user:update,system:role:query,system:role:update',
        'TENANT', NULL, NULL,
        'Built-in bootstrap tenant admin role', 'seed', 'seed'
    ),
    (
        4, 1, 'tenant_member', 'Tenant Member', 0, 4,
        '1',
        'dashboard:query',
        'TENANT', NULL, NULL,
        'Built-in bootstrap tenant member role', 'seed', 'seed'
    );

INSERT IGNORE INTO sys_user (
    id, tenant_id, username, password, nickname, email, mobile, status, role_codes, permission_codes,
    data_scope, department_id, department_ids, scope_tenant_ids, remark, create_by, update_by
) VALUES
    (
        1, 1, 'admin', 'admin123', 'System Admin', 'admin@luckycolor.com', '13800000000', 0,
        'ROLE_SUPER_ADMIN',
        'dashboard:query,file:upload,file:download,i18n:query,i18n:create,i18n:update,system:user:query,system:user:create,system:user:update,system:user:delete,system:user:reset-password,system:user:assign-role,system:user:import,system:user:export,system:menu:query,system:menu:create,system:menu:update,system:menu:delete,system:role:query,system:role:create,system:role:update,system:role:authorize,system:department:query,system:department:create,system:department:update,system:department:delete,system:dictionary:query,system:dictionary:create,system:dictionary:update,system:dictionary:delete,system:dictionary:refresh-cache,system:config:query,system:config:create,system:config:update,system:notice:query,system:notice:create,system:notice:update,system:notice:publish,system:operation-log:query,security:audit:query,tenant:query,tenant:create,tenant:update,tenant:package:query,tenant:package:create,tenant:package:update,watermark:query,watermark:update,codegen:query,codegen:create,codegen:update',
        'ALL', 100, '100,101', '1', 'Built-in seed administrator', 'seed', 'seed'
    ),
    (
        2, 1, 'tenant-operator', 'operator123', 'Tenant Operator', 'operator@luckycolor.com', '13800000001', 0,
        'ROLE_TENANT_OPERATOR',
        'dashboard:query,file:upload,file:download,i18n:query,tenant:query,tenant:package:query,watermark:query,watermark:update,codegen:query,codegen:create,codegen:update',
        'CUSTOM', 100, '100,101', '1', 'Built-in tenant operator', 'seed', 'seed'
    );

INSERT IGNORE INTO sys_dictionary_type (
    id, tenant_id, type_code, type_name, status, sort, remark, create_by, update_by
) VALUES
    (1, 1, 'user_status', 'User Status', 0, 1, 'Built-in user status dictionary', 'seed', 'seed'),
    (2, 1, 'notice_status', 'Notice Status', 0, 2, 'Built-in notice status dictionary', 'seed', 'seed'),
    (3, 1, 'common_yes_no', 'Common Yes No', 0, 3, 'Built-in yes/no dictionary', 'seed', 'seed');

INSERT IGNORE INTO sys_dictionary_item (
    id, tenant_id, type_code, parent_id, item_label, item_value, item_tag, sort, status, remark, create_by, update_by
) VALUES
    (1, 1, 'user_status', 0, 'Enabled', '0', 'success', 1, 0, 'Built-in enabled status', 'seed', 'seed'),
    (2, 1, 'user_status', 0, 'Disabled', '1', 'danger', 2, 0, 'Built-in disabled status', 'seed', 'seed'),
    (3, 1, 'notice_status', 0, 'Draft', '0', 'info', 1, 0, 'Built-in draft status', 'seed', 'seed'),
    (4, 1, 'notice_status', 0, 'Published', '1', 'success', 2, 0, 'Built-in published status', 'seed', 'seed'),
    (5, 1, 'common_yes_no', 0, 'Yes', '1', 'success', 1, 0, 'Built-in yes option', 'seed', 'seed'),
    (6, 1, 'common_yes_no', 0, 'No', '0', 'default', 2, 0, 'Built-in no option', 'seed', 'seed');
