CREATE TABLE IF NOT EXISTS sys_tenant_package (
    id BIGINT NOT NULL,
    package_name VARCHAR(100) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    sort INT NOT NULL DEFAULT 0,
    remark VARCHAR(500) NULL,
    create_by VARCHAR(64) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_sys_tenant_package_status (status),
    KEY idx_sys_tenant_package_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tenant package';

CREATE TABLE IF NOT EXISTS sys_tenant (
    id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    package_id BIGINT NOT NULL,
    contact_name VARCHAR(50) NOT NULL,
    contact_mobile VARCHAR(20) NOT NULL,
    account_count INT NOT NULL,
    expire_time DATETIME NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    create_by VARCHAR(64) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_sys_tenant_package_id (package_id),
    KEY idx_sys_tenant_status (status),
    KEY idx_sys_tenant_expire_time (expire_time),
    CONSTRAINT fk_sys_tenant_package_id FOREIGN KEY (package_id) REFERENCES sys_tenant_package (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tenant';

CREATE TABLE IF NOT EXISTS sys_tenant_audit_log (
    id BIGINT NOT NULL,
    tenant_id BIGINT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    content VARCHAR(500) NULL,
    create_by VARCHAR(64) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_sys_tenant_audit_log_tenant_id (tenant_id),
    KEY idx_sys_tenant_audit_log_target (target_type, target_id),
    KEY idx_sys_tenant_audit_log_action (action),
    KEY idx_sys_tenant_audit_log_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tenant audit log';

CREATE TABLE IF NOT EXISTS sys_tenant_bootstrap_record (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    template_code VARCHAR(100) NOT NULL,
    template_name VARCHAR(100) NOT NULL,
    role_codes VARCHAR(500) NULL,
    menu_codes VARCHAR(1000) NULL,
    admin_username VARCHAR(50) NOT NULL,
    admin_nickname VARCHAR(50) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    bootstrap_time DATETIME NOT NULL,
    remark VARCHAR(255) NULL,
    create_by VARCHAR(64) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_tenant_bootstrap_record_tenant_id (tenant_id),
    KEY idx_sys_tenant_bootstrap_record_template_code (template_code),
    KEY idx_sys_tenant_bootstrap_record_status (status),
    KEY idx_sys_tenant_bootstrap_record_bootstrap_time (bootstrap_time),
    CONSTRAINT fk_sys_tenant_bootstrap_record_tenant_id FOREIGN KEY (tenant_id) REFERENCES sys_tenant (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tenant bootstrap record';
