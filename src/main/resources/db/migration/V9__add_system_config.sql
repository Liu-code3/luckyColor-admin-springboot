CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    config_key VARCHAR(100) NOT NULL,
    config_name VARCHAR(100) NOT NULL,
    config_value VARCHAR(2000) NOT NULL,
    sensitive TINYINT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 0,
    sort INT NOT NULL DEFAULT 0,
    remark VARCHAR(255) NULL,
    create_by VARCHAR(64) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_config_tenant_key (tenant_id, config_key),
    KEY idx_sys_config_status (status),
    KEY idx_sys_config_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System config';
