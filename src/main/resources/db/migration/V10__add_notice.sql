CREATE TABLE IF NOT EXISTS sys_notice (
    id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    notice_title VARCHAR(200) NOT NULL,
    notice_type VARCHAR(64) NOT NULL,
    notice_content TEXT NOT NULL,
    publish_status TINYINT NOT NULL DEFAULT 0,
    publish_time DATETIME NULL,
    sort INT NOT NULL DEFAULT 0,
    remark VARCHAR(255) NULL,
    create_by VARCHAR(64) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_sys_notice_publish_status (publish_status),
    KEY idx_sys_notice_publish_time (publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System notice';
