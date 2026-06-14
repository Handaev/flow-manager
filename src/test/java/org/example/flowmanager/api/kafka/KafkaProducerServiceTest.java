package org.example.flowmanager.api.kafka;

import org.example.flowmanager.api.dto.ConversionRequestRecord;
import org.example.flowmanager.api.entity.ConversionFileOutbox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    private static final String TOPIC = "conversion-request";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(kafkaProducerService, "TOPIC_REQUEST", TOPIC);
    }

    @Test
    public void shouldSendEventSuccessfully() {
        ConversionFileOutbox outbox = new ConversionFileOutbox();
        outbox.setBucketName("user-media-bucket");
        outbox.setName("video_presentation");
        outbox.setToExtension("pdf");

        ArgumentCaptor<ConversionRequestRecord> eventCaptor = ArgumentCaptor.forClass(ConversionRequestRecord.class);

        kafkaProducerService.sendEvent(outbox);

        verify(kafkaTemplate, times(1)).send(eq(TOPIC), eventCaptor.capture());

        ConversionRequestRecord sentEvent = eventCaptor.getValue();
        assertNotNull(sentEvent);
        assertEquals("user-media-bucket", sentEvent.bucketName());
        assertEquals("video_presentation", sentEvent.fileName());
        assertEquals("pdf", sentEvent.toExtension());
    }
}
