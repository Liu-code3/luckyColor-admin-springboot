CREATE TABLE IF NOT EXISTS sys_watermark_config (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    enabled TINYINT NOT NULL DEFAULT 0,
    content VARCHAR(128) NOT NULL,
    color VARCHAR(32) NOT NULL DEFAULT '#000000',
    font_size INT NOT NULL DEFAULT 16,
    opacity_percent INT NOT NULL DEFAULT 15,
    rotate_degree INT NOT NULL DEFAULT -22,
    gap_x INT NOT NULL DEFAULT 120,
    gap_y INT NOT NULL DEFAULT 120,
    create_by VARCHAR(64) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_watermark_config_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tenant watermark config';
