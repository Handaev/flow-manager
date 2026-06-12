package org.example.flowmanager.api.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.flowmanager.api.dto.ConversionResponseRecord;
import org.example.flowmanager.api.service.impl.ConversionFileServiceImpl;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final ConversionFileServiceImpl conversionFileService;
    private final ObjectMapper objectMapper;

    @Transactional
    @KafkaListener(groupId = "${spring.kafka.consumer.group-id}", topics = "${spring.kafka.consumer.topicResponse}")
    public void handleConversionFile(ConsumerRecord<String, String> record) {
        String recordStr = record.value();
        try {
            ConversionResponseRecord value = objectMapper.readValue(recordStr, ConversionResponseRecord.class);

            conversionFileService.saveConversionFile(value);
        } catch (JsonProcessingException e) {
            log.debug("Error read message: {}", recordStr, e);
        }
    }

}
