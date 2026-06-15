package org.example.flowmanager.api.service.sheduler;

import lombok.RequiredArgsConstructor;
import org.example.flowmanager.api.kafka.KafkaProducerService;
import org.example.flowmanager.api.repository.ConversionFileOutboxRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ConversionFileOutboxScheduler {

    private final ConversionFileOutboxRepository conversionFileOutboxRepository;
    private final KafkaProducerService kafkaProducerService;

    @Transactional
    @Scheduled(fixedRateString = "${server.scheduler.fixedRateWriteOutbox}")
    public void processOutbox() {
        conversionFileOutboxRepository.findAllBySentFalse()
                .forEach(fileOutbox -> {
                    kafkaProducerService.sendEvent(fileOutbox);
                    fileOutbox.setSent(true);
                    conversionFileOutboxRepository.save(fileOutbox);
                });
    }
}