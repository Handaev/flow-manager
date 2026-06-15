package org.example.flowmanager.api.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.flowmanager.api.entity.ConversionFileOutbox;
import org.example.flowmanager.api.repository.ConversionFileOutboxRepository;
import org.example.flowmanager.api.service.ConversionFileOutboxService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConversionFileOutboxServiceImpl implements ConversionFileOutboxService {

    private final ConversionFileOutboxRepository conversionFileOutboxRepository;

    public void saveFileConversionOutbox(ConversionFileOutbox conversionFileOutbox) {
        conversionFileOutboxRepository
                .findByNameAndBucketNameAndToExtension(
                        conversionFileOutbox.getName(),
                        conversionFileOutbox.getBucketName(),
                        conversionFileOutbox.getToExtension())
                .orElseGet(() -> conversionFileOutboxRepository.save(conversionFileOutbox));
    }
}