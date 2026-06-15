package org.example.flowmanager.api.dto;

public record ConversionRequestRecord(String bucketName,
                                      String fileName,
                                      String toExtension) {
}