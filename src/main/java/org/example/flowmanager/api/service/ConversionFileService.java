package org.example.flowmanager.api.service;

import org.example.flowmanager.api.dto.ConversionResponseRecord;
import org.example.flowmanager.api.entity.ConversionFile;

public interface ConversionFileService {

    void saveConversionFile(ConversionFile conversionFile);

    void saveConversionFile(ConversionResponseRecord value);
}
