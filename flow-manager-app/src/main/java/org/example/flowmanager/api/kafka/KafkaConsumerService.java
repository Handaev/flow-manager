package org.example.flowmanager.api.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.flowmanager.api.dto.ConversionResponseRecord;
import org.example.flowmanager.api.dto.InvalidateSubscriptionUserRecord;
import org.example.flowmanager.api.service.impl.ConversionFileServiceImpl;
import org.example.flowmanager.api.service.impl.ManualCacheServiceImpl;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final ConversionFileServiceImpl conversionFileService;
    private final ObjectMapper objectMapper;
    private final ManualCacheServiceImpl manualCacheService;

    @Transactional
    @KafkaListener(groupId = "${spring.kafka.consumer.group-conversion-id}", topics = "${spring.kafka.consumer.topic-conversion-response}")
    public void handleConversionFile(ConsumerRecord<String, String> record) {
        String recordStr = record.value();
        try {
            ConversionResponseRecord value = objectMapper.readValue(recordStr, ConversionResponseRecord.class);

            conversionFileService.saveConversionFile(value);
        } catch (JsonProcessingException e) {
            log.debug("Error read message: {}", recordStr, e);
        }
    }

    @KafkaListener(groupId = "${spring.kafka.consumer.group-subscription-id}", topics = "${spring.kafka.consumer.topic-subscription-invalidate-cache}")
    public void handleSubscriptionInvalidate(ConsumerRecord<String, String> record) {
        String recordUsersStr = record.value();
        try {
            List<InvalidateSubscriptionUserRecord> invalidateSubscriptionUserRecords = objectMapper.readValue(
                    recordUsersStr,
                    new TypeReference<List<InvalidateSubscriptionUserRecord>>() {}
            );

            manualCacheService.invalidateCacheSubscriptions(invalidateSubscriptionUserRecords);
        } catch (JsonProcessingException e) {
            log.debug("Error read message: {}", recordUsersStr, e);
        }
    }
}