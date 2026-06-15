package org.example.flowmanager.api.service.impl;

import org.example.flowmanager.api.entity.ConversionFileOutbox;
import org.example.flowmanager.api.repository.ConversionFileOutboxRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConversionFileOutboxServiceImplTest {

    @Mock
    private ConversionFileOutboxRepository conversionFileOutboxRepository;

    @InjectMocks
    private ConversionFileOutboxServiceImpl conversionFileOutboxServiceImpl;

    @Test
    public void saveFileConversionOutbox_successfully(){
        ConversionFileOutbox conversionFileOutbox = new ConversionFileOutbox();

        conversionFileOutboxServiceImpl.saveFileConversionOutbox(conversionFileOutbox);

        verify(conversionFileOutboxRepository, times(1)).save(conversionFileOutbox);
    }

    @Test
    public void saveFileConversionOutbox_shouldReturnNull(){
        ConversionFileOutbox conversionFileOutbox = new ConversionFileOutbox();
        conversionFileOutbox.setBucketName("bucketName");
        conversionFileOutbox.setToExtension("toExtension");
        conversionFileOutbox.setName("name");

        when(conversionFileOutboxRepository.findByNameAndBucketNameAndToExtension(any(), any(), any()))
                .thenReturn(Optional.of(conversionFileOutbox));
        conversionFileOutboxServiceImpl.saveFileConversionOutbox(conversionFileOutbox);

        verify(conversionFileOutboxRepository, never()).save(conversionFileOutbox);
    }
}