CREATE TABLE IF NOT EXISTS sys_dictionary_type (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    type_code VARCHAR(100) NOT NULL,
    type_name VARCHAR(100) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    sort INT NOT NULL DEFAULT 0,
    remark VARCHAR(255) NULL,
    create_by VARCHAR(64) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_dictionary_type_tenant_code (tenant_id, type_code),
    KEY idx_sys_dictionary_type_status (status),
    KEY idx_sys_dictionary_type_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System dictionary type';
