INSERT IGNORE INTO sys_tenant_profile (
    tenant_id, tenant_code, status_code, contact_email, remark, admin_username, admin_nickname, create_by, update_by
) VALUES (
    1, 'tenant_001', 'ACTIVE', 'admin@luckycolor.com', 'Built-in frontend compatibility tenant profile', 'admin', 'System Admin', 'seed', 'seed'
);

INSERT IGNORE INTO sys_menu (
    id, parent_id, menu_name, menu_type, route_name, route_path, menu_key, component, redirect, meta, permission_code,
    role_codes, icon, layout, sort, visible, keep_alive, always_show, status, remark, create_by, update_by
) VALUES
    (
        22, 10, 'System Menu', 'MENU', 'SystemMenu', 'menu', 'main_system_menu', 'system/menu/index', NULL,
        '{"title":"System Menu","keepAlive":true,"hidden":false}', 'system:menu:query', 'ROLE_SUPER_ADMIN',
        'solar:list-tree-linear', 'default', 3, 1, 1, 0, 0, 'Frontend compatibility system menu route', 'seed', 'seed'
    ),
    (
        30, 0, 'Tenant Center', 'DIRECTORY', 'TenantCenter', '/tenantCenter', 'main_system_tenant_root', 'sys/index', '/tenantCenter/tenant',
        '{"title":"Tenant Center","keepAlive":false,"hidden":false}', NULL, 'ROLE_SUPER_ADMIN,ROLE_TENANT_OPERATOR',
        'solar:buildings-2-linear', 'default', 30, 1, 0, 1, 0, 'Frontend compatibility tenant root', 'seed', 'seed'
    ),
    (
        31, 30, 'Tenant Management', 'MENU', 'TenantManagement', 'tenant', 'main_system_tenant', 'sys/tenant/index', NULL,
        '{"title":"Tenant Management","keepAlive":true,"hidden":false}', 'tenant:query', 'ROLE_SUPER_ADMIN,ROLE_TENANT_OPERATOR',
        'solar:buildings-linear', 'default', 1, 1, 1, 0, 0, 'Frontend compatibility tenant page', 'seed', 'seed'
    ),
    (
        32, 30, 'Tenant Package', 'MENU', 'TenantPackage', 'tenantPackage', 'main_system_tenant_package', 'sys/tenantPackage/index', NULL,
        '{"title":"Tenant Package","keepAlive":true,"hidden":false}', 'tenant:package:query', 'ROLE_SUPER_ADMIN,ROLE_TENANT_OPERATOR',
        'solar:box-linear', 'default', 2, 1, 1, 0, 0, 'Frontend compatibility tenant package page', 'seed', 'seed'
    );

UPDATE sys_role
SET menu_ids = TRIM(BOTH ',' FROM CONCAT_WS(
    ',',
    menu_ids,
    IF(FIND_IN_SET('22', IFNULL(menu_ids, '')) = 0, '22', NULL),
    IF(FIND_IN_SET('30', IFNULL(menu_ids, '')) = 0, '30', NULL),
    IF(FIND_IN_SET('31', IFNULL(menu_ids, '')) = 0, '31', NULL),
    IF(FIND_IN_SET('32', IFNULL(menu_ids, '')) = 0, '32', NULL)
))
WHERE id = 1;

UPDATE sys_role
SET menu_ids = TRIM(BOTH ',' FROM CONCAT_WS(
    ',',
    menu_ids,
    IF(FIND_IN_SET('30', IFNULL(menu_ids, '')) = 0, '30', NULL),
    IF(FIND_IN_SET('31', IFNULL(menu_ids, '')) = 0, '31', NULL),
    IF(FIND_IN_SET('32', IFNULL(menu_ids, '')) = 0, '32', NULL)
))
WHERE id = 2;
