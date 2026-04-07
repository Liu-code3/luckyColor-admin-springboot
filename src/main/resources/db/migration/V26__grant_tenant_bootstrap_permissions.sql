UPDATE sys_role
SET permission_codes = TRIM(BOTH ',' FROM CONCAT_WS(
    ',',
    permission_codes,
    IF(FIND_IN_SET('tenant:bootstrap:query', IFNULL(permission_codes, '')) = 0, 'tenant:bootstrap:query', NULL),
    IF(FIND_IN_SET('tenant:bootstrap:execute', IFNULL(permission_codes, '')) = 0, 'tenant:bootstrap:execute', NULL)
))
WHERE tenant_id = 1
  AND role_code = 'ROLE_SUPER_ADMIN';

UPDATE sys_user
SET permission_codes = TRIM(BOTH ',' FROM CONCAT_WS(
    ',',
    permission_codes,
    IF(FIND_IN_SET('tenant:bootstrap:query', IFNULL(permission_codes, '')) = 0, 'tenant:bootstrap:query', NULL),
    IF(FIND_IN_SET('tenant:bootstrap:execute', IFNULL(permission_codes, '')) = 0, 'tenant:bootstrap:execute', NULL)
))
WHERE tenant_id = 1
  AND username = 'admin';
