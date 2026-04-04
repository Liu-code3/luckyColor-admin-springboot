UPDATE sys_user
SET password = '$2b$10$GpBeIWgfwZt8WkEG8.nCc.CB9GPGYPLgrIx5OpcCS5F5lUZrG89vC'
WHERE tenant_id = 1 AND username = 'admin';

UPDATE sys_user
SET password = '$2b$10$ByL3bDD6.G4YumMjJ6AkgeosOYzlf/grj3nN0OMgjOLio30J0ksTG'
WHERE tenant_id = 1 AND username = 'tenant-operator';
