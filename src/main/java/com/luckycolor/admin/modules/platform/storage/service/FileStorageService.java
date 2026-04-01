package com.luckycolor.admin.modules.platform.storage.service;

import com.luckycolor.admin.modules.platform.storage.web.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    FileUploadResponse upload(MultipartFile file);

    StoredFile download(String relativePath);
}
