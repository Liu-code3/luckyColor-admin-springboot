package com.luckycolor.admin.modules.platform.storage.service;

import org.springframework.core.io.Resource;

public record StoredFile(
    Resource resource,
    String storedFilename,
    long size,
    String contentType
) {
}
