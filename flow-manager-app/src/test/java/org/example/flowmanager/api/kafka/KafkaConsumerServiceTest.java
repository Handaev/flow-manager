package org.example.flowmanager.api.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.flowmanager.api.dto.ConversionResponseRecord;
import org.example.flowmanager.api.service.impl.ConversionFileServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class KafkaConsumerServiceTest {

    @Mock
    private ConversionFileServiceImpl conversionFileService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private KafkaConsumerService kafkaConsumerService;

    @Test
    public void handleConversionFile_Successfully() throws JsonProcessingException {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("conversion", 0, 1, "key",
                "{\"id\": \"4055475d-8a39-4219-8fa3-c12f42bb8ce3\", \"bucketName\": \"pdf\", \"fileName\": \"3.pdf\"}"
        );
        ConversionResponseRecord value = new ConversionResponseRecord(
                "4055475d-8a39-4219-8fa3-c12f42bb8ce3",
                "pdf",
                "3.pdf"
        );
        String recordStr = record.value();
        when(objectMapper.readValue(recordStr, ConversionResponseRecord.class)).thenReturn(value);

        kafkaConsumerService.handleConversionFile(record);

        verify(conversionFileService, times(1)).saveConversionFile(value);
    }

    @Test
    public void handleConversionFile_JsonException() throws JsonProcessingException {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("conversion", 0, 1, "key",
                "{id: broken-json}"
        );
        String recordStr = record.value();

        when(objectMapper.readValue(recordStr, ConversionResponseRecord.class))
                .thenThrow(new JsonProcessingException("Invalid JSON structure") {});

        kafkaConsumerService.handleConversionFile(record);

        verify(conversionFileService, never()).saveConversionFile(any(ConversionResponseRecord.class));
    }
}
