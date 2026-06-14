package org.example.flowmanager.api.service.scheduler;

import org.example.flowmanager.api.entity.ConversionFile;
import org.example.flowmanager.api.entity.StatusFile;
import org.example.flowmanager.api.service.impl.ConversionFileServiceImpl;
import org.example.flowmanager.api.service.sheduler.ConversionFileScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversionFileSchedulerTest {

    @Mock
    private ConversionFileServiceImpl conversionFileService;

    @InjectMocks
    private ConversionFileScheduler conversionFileScheduler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(conversionFileScheduler, "LIMIT", 10);
    }

    @Test
    void shouldProcessConversionFile_SaveCall() {
        when(conversionFileService.findAllByCreatedAtMoreTenMinutesAndStatusFailed(eq(10), eq(StatusFile.FAILED)))
                .thenReturn(List.of(new ConversionFile()));

        conversionFileScheduler.processConversionFile();

        verify(conversionFileService, times(1)).saveConversionFiles(any());
    }
}