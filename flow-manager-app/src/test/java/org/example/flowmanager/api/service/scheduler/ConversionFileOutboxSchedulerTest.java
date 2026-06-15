package org.example.flowmanager.api.service.scheduler;

import org.example.flowmanager.api.entity.ConversionFileOutbox;
import org.example.flowmanager.api.kafka.KafkaProducerService;
import org.example.flowmanager.api.repository.ConversionFileOutboxRepository;
import org.example.flowmanager.api.service.sheduler.ConversionFileOutboxScheduler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversionFileOutboxSchedulerTest {

    @Mock
    private ConversionFileOutboxRepository conversionFileOutboxRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private ConversionFileOutboxScheduler outboxScheduler;

    @Test
    void shouldProcessUnsafeEventsSuccessfully() {
        ConversionFileOutbox event1 = new ConversionFileOutbox();
        event1.setName("event1");
        event1.setBucketName("bucket1");
        event1.setSent(false);
        ConversionFileOutbox event2 = new ConversionFileOutbox();
        event2.setName("event2");
        event2.setBucketName("bucket2");
        event2.setSent(false);
        List<ConversionFileOutbox> mockEvents = List.of(event1, event2);

        when(conversionFileOutboxRepository.findAllBySentFalse()).thenReturn(mockEvents);

        outboxScheduler.processOutbox();

        verify(kafkaProducerService, times(1)).sendEvent(event1);
        verify(kafkaProducerService, times(1)).sendEvent(event2);

        assertTrue(event1.isSent());
        assertTrue(event2.isSent());

        verify(conversionFileOutboxRepository, times(1)).save(event1);
        verify(conversionFileOutboxRepository, times(1)).save(event2);
    }

    @Test
    void shouldDoNothingWhenNoUnsafeEventsFound() {
        when(conversionFileOutboxRepository.findAllBySentFalse()).thenReturn(Collections.emptyList());

        outboxScheduler.processOutbox();

        verify(kafkaProducerService, never()).sendEvent(any(ConversionFileOutbox.class));
        verify(conversionFileOutboxRepository, never()).save(any(ConversionFileOutbox.class));
    }
}
