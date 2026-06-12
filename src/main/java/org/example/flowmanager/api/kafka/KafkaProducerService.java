package org.example.flowmanager.api.kafka;

import lombok.RequiredArgsConstructor;
import org.example.flowmanager.api.dto.ConversionRequestRecord;
import org.example.flowmanager.api.entity.ConversionFileOutbox;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.producer.topicRequest}")
    private String TOPIC_REQUEST;

    public void sendEvent(ConversionFileOutbox outbox) {
        ConversionRequestRecord event = new ConversionRequestRecord(
                outbox.getBucketName(),
                outbox.getName(),
                outbox.getToExtension()
        );
        kafkaTemplate.send(TOPIC_REQUEST, event);
    }
}
