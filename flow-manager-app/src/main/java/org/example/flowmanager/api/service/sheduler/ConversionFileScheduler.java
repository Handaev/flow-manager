package org.example.flowmanager.api.service.sheduler;

import lombok.RequiredArgsConstructor;
import org.example.flowmanager.api.entity.ConversionFile;
import org.example.flowmanager.api.entity.StatusFile;
import org.example.flowmanager.api.service.impl.ConversionFileServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConversionFileScheduler {

    private final ConversionFileServiceImpl conversionFileService;

    @Value("${server.scheduler.limit}")
    private int LIMIT;

    @Scheduled(fixedRateString = "${server.scheduler.fixedRateSetStatusFailed}")
    public void processConversionFile() {
        List<ConversionFile> conversionFiles = conversionFileService.findAllByCreatedAtMoreTenMinutesAndStatusFailed(LIMIT, StatusFile.FAILED);
        conversionFileService.saveConversionFiles(conversionFiles);
    }
}