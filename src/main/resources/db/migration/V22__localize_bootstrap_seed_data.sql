UPDATE sys_tenant_package
SET package_name = '基础版套餐',
    remark = '默认基础租户套餐'
WHERE id = 1
  AND create_by = 'seed';

UPDATE sys_tenant
SET name = '默认租户',
    contact_name = '系统管理员'
WHERE id = 1
  AND create_by = 'seed';

UPDATE sys_tenant_profile
SET remark = '本地初始化默认租户',
    admin_nickname = '系统管理员'
WHERE tenant_id = 1
  AND create_by = 'seed';

UPDATE sys_department
SET department_name = CASE id
        WHEN 100 THEN '总部'
        WHEN 101 THEN '运营支持部'
        ELSE department_name
    END,
    leader = CASE id
        WHEN 100 THEN '系统管理员'
        WHEN 101 THEN '租户运营'
        ELSE leader
    END,
    remark = CASE id
        WHEN 100 THEN '租户总部组织'
        WHEN 101 THEN '负责客户运营与交付支持'
        ELSE remark
    END
WHERE tenant_id = 1
  AND create_by = 'seed'
  AND id IN (100, 101);

UPDATE sys_menu
SET menu_name = CASE id
        WHEN 1 THEN '首页'
        WHEN 10 THEN '系统管理'
        WHEN 11 THEN '用户管理'
        WHEN 12 THEN '角色管理'
        WHEN 13 THEN '部门管理'
        WHEN 14 THEN '数据字典'
        WHEN 15 THEN '配置管理'
        WHEN 16 THEN '公告管理'
        WHEN 17 THEN '操作日志'
        WHEN 18 THEN '国际化'
        WHEN 19 THEN '水印设置'
        WHEN 20 THEN '代码生成'
        WHEN 22 THEN '菜单管理'
        WHEN 30 THEN '租户中心'
        WHEN 31 THEN '租户管理'
        WHEN 32 THEN '租户套餐'
        ELSE menu_name
    END,
    meta = CASE id
        WHEN 1 THEN '{"title":"首页","keepAlive":true,"hidden":false}'
        WHEN 10 THEN '{"title":"系统管理","keepAlive":false,"hidden":false}'
        WHEN 11 THEN '{"title":"用户管理","keepAlive":true,"hidden":false}'
        WHEN 12 THEN '{"title":"角色管理","keepAlive":true,"hidden":false}'
        WHEN 13 THEN '{"title":"部门管理","keepAlive":true,"hidden":false}'
        WHEN 14 THEN '{"title":"数据字典","keepAlive":true,"hidden":false}'
        WHEN 15 THEN '{"title":"配置管理","keepAlive":true,"hidden":false}'
        WHEN 16 THEN '{"title":"公告管理","keepAlive":true,"hidden":false}'
        WHEN 17 THEN '{"title":"操作日志","keepAlive":true,"hidden":false}'
        WHEN 18 THEN '{"title":"国际化","keepAlive":true,"hidden":false}'
        WHEN 19 THEN '{"title":"水印设置","keepAlive":true,"hidden":false}'
        WHEN 20 THEN '{"title":"代码生成","keepAlive":true,"hidden":false}'
        WHEN 22 THEN '{"title":"菜单管理","keepAlive":true,"hidden":false}'
        WHEN 30 THEN '{"title":"租户中心","keepAlive":false,"hidden":false}'
        WHEN 31 THEN '{"title":"租户管理","keepAlive":true,"hidden":false}'
        WHEN 32 THEN '{"title":"租户套餐","keepAlive":true,"hidden":false}'
        ELSE meta
    END
WHERE create_by = 'seed'
  AND id IN (1, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 22, 30, 31, 32);

UPDATE sys_role
SET role_name = CASE id
        WHEN 1 THEN '超级管理员'
        WHEN 2 THEN '租户运营'
        WHEN 3 THEN '租户管理员'
        WHEN 4 THEN '普通成员'
        ELSE role_name
    END,
    remark = CASE id
        WHEN 1 THEN '拥有平台租户内全部管理权限'
        WHEN 2 THEN '负责租户日常运营支持'
        WHEN 3 THEN '负责本租户日常管理'
        WHEN 4 THEN '基础业务使用角色'
        ELSE remark
    END
WHERE tenant_id = 1
  AND create_by = 'seed'
  AND id IN (1, 2, 3, 4);

UPDATE sys_user
SET nickname = CASE id
        WHEN 1 THEN '系统管理员'
        WHEN 2 THEN '租户运营'
        ELSE nickname
    END,
    remark = CASE id
        WHEN 1 THEN '本地初始化默认管理员'
        WHEN 2 THEN '本地初始化租户运营账号'
        ELSE remark
    END
WHERE tenant_id = 1
  AND create_by = 'seed'
  AND id IN (1, 2);

UPDATE sys_dictionary_type
SET type_name = CASE id
        WHEN 1 THEN '用户状态'
        WHEN 2 THEN '公告状态'
        WHEN 3 THEN '是否字典'
        ELSE type_name
    END,
    remark = CASE id
        WHEN 1 THEN '内置用户状态字典'
        WHEN 2 THEN '内置公告状态字典'
        WHEN 3 THEN '内置是否字典'
        ELSE remark
    END
WHERE tenant_id = 1
  AND create_by = 'seed'
  AND id IN (1, 2, 3);

UPDATE sys_dictionary_item
SET item_label = CASE id
        WHEN 1 THEN '启用'
        WHEN 2 THEN '停用'
        WHEN 3 THEN '草稿'
        WHEN 4 THEN '已发布'
        WHEN 5 THEN '是'
        WHEN 6 THEN '否'
        ELSE item_label
    END,
    remark = CASE id
        WHEN 1 THEN '内置启用状态'
        WHEN 2 THEN '内置停用状态'
        WHEN 3 THEN '内置草稿状态'
        WHEN 4 THEN '内置发布状态'
        WHEN 5 THEN '内置是选项'
        WHEN 6 THEN '内置否选项'
        ELSE remark
    END
WHERE tenant_id = 1
  AND create_by = 'seed'
  AND id IN (1, 2, 3, 4, 5, 6);
