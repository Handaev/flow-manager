package org.example.flowmanager.api.controller;

import lombok.RequiredArgsConstructor;
import org.example.flowmanager.api.service.impl.ConversionFileServiceImpl;
import org.example.flowmanager.api.service.impl.WorkflowServiceImpl;
import org.example.flowmanager.controller.FilesApi;
import org.example.flowmanager.dto.FileResponseDto;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequiredArgsConstructor
public class ConversionFileController implements FilesApi {

    private final WorkflowServiceImpl workflowService;
    private final ConversionFileServiceImpl conversionFileService;

    @Override
    public ResponseEntity<Resource> getConvertedFile(Long fileId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(conversionFileService.findConvertedFileByFileId(fileId));
    }

    @Override
    public ResponseEntity<FileResponseDto> getFileStatus(Long fileId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(conversionFileService.findStatusByFileId(fileId));
    }

    @Override
    public ResponseEntity<FileResponseDto> fileUploadAndConvert(String toExtension, String bucketName, MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workflowService.processConvert(toExtension, bucketName, file));
    }
}