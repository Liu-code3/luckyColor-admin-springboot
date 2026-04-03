UPDATE sys_menu
SET menu_key = CASE id
    WHEN 1 THEN 'main_analysis'
    WHEN 10 THEN 'main_system'
    WHEN 11 THEN 'main_system_users'
    WHEN 12 THEN 'main_system_role'
    WHEN 13 THEN 'main_system_department'
    WHEN 14 THEN 'icomponent_dict'
    WHEN 15 THEN 'main_system_config'
    WHEN 16 THEN 'main_system_notice'
    WHEN 17 THEN 'main_system_operation_log'
    WHEN 18 THEN 'main_system_i18n'
    WHEN 19 THEN 'main_system_watermark'
    WHEN 20 THEN 'main_system_codegen'
    ELSE menu_key
END
WHERE id IN (1, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
  AND (
      menu_key IS NULL
      OR menu_key = ''
      OR (permission_code IS NOT NULL AND menu_key = permission_code)
      OR (route_name IS NOT NULL AND menu_key = route_name)
      OR menu_key = CONCAT('menu:', id)
  );

UPDATE sys_menu
SET redirect = '/system/users'
WHERE id = 10
  AND (redirect IS NULL OR redirect = '');

UPDATE sys_menu
SET layout = 'default'
WHERE id IN (1, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
  AND (layout IS NULL OR layout = '');

UPDATE sys_menu
SET meta = CAST(
    JSON_OBJECT(
        'title', menu_name,
        'keepAlive', IFNULL(keep_alive, 0) = 1,
        'hidden', IFNULL(visible, 1) <> 1
    ) AS CHAR
)
WHERE id IN (1, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
  AND (meta IS NULL OR meta = '');
