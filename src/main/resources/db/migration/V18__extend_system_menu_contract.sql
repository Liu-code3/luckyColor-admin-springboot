ALTER TABLE sys_menu
    ADD COLUMN menu_key VARCHAR(150) NULL AFTER route_path,
    ADD COLUMN layout VARCHAR(50) NULL AFTER icon,
    ADD COLUMN redirect VARCHAR(200) NULL AFTER component,
    ADD COLUMN meta TEXT NULL AFTER redirect;

UPDATE sys_menu
SET menu_key = COALESCE(NULLIF(permission_code, ''), NULLIF(route_name, ''), CONCAT('menu:', id))
WHERE menu_key IS NULL OR menu_key = '';

UPDATE sys_menu
SET layout = 'default'
WHERE layout IS NULL OR layout = '';
