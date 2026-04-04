CREATE TABLE IF NOT EXISTS sys_tenant_profile (
    tenant_id BIGINT NOT NULL,
    tenant_code VARCHAR(100) NOT NULL,
    status_code VARCHAR(32) NULL,
    contact_email VARCHAR(100) NULL,
    remark VARCHAR(500) NULL,
    admin_username VARCHAR(64) NULL,
    admin_nickname VARCHAR(100) NULL,
    create_by VARCHAR(64) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id),
    UNIQUE KEY uk_sys_tenant_profile_code (tenant_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Frontend tenant compatibility profile';
