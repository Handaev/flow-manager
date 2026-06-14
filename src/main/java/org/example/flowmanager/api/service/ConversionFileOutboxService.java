package org.example.flowmanager.api.service;

import org.example.flowmanager.api.entity.ConversionFileOutbox;

public interface ConversionFileOutboxService {

    void saveFileConversionOutbox(ConversionFileOutbox conversionFileOutbox);
}