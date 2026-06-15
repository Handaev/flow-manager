package org.example.flowmanager.api.service;

import org.example.flowmanager.api.dto.ConversionMultipartFile;
import org.springframework.core.io.Resource;

import java.io.IOException;

public interface MinioService {

    void upload(ConversionMultipartFile file);

    Resource download(String path, String fileName) throws IOException;
}