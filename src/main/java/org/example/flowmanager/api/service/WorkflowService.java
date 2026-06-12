package org.example.flowmanager.api.service;

import org.example.flowmanager.dto.FileResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface WorkflowService {

    FileResponseDto processConvert(String toExtension, String bucketName, MultipartFile multipartFile);
}
