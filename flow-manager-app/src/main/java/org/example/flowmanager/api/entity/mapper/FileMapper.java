package org.example.flowmanager.api.entity.mapper;

import org.example.flowmanager.api.dto.ConversionMultipartFile;
import org.example.flowmanager.api.entity.ConversionFile;
import org.example.flowmanager.api.entity.ConversionFileOutbox;
import org.example.flowmanager.dto.FileResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FileMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", expression = "java(multipartFile.getOriginalFilename())")
    @Mapping(target = "originalFileName", expression = "java(multipartFile.getOriginalFilename())")
    @Mapping(target = "content", expression = "java(multipartFile.isEmpty() ? null : multipartFile.getBytes())")
    @Mapping(target = "contentType", source = "multipartFile.contentType")
    @Mapping(target = "toExtension", source = "toExtension")
    @Mapping(target = "path", source = "bucketName")
    @Mapping(target = "fromExtension", expression = "java(multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf(\".\") + 1))")
    ConversionMultipartFile toConversionMultipartFile(MultipartFile multipartFile, String toExtension, String bucketName) throws IOException;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "fromExtension", source = "multipartFile.fromExtension")
    @Mapping(target = "toExtension", source = "multipartFile.toExtension")
    @Mapping(target = "path", expression = "java(multipartFile.getPath() + \"/\" + multipartFile.getOriginalFilename())")
    ConversionFile toConversionFile(ConversionMultipartFile multipartFile);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "conversionFile.name")
    @Mapping(target = "sent", ignore = true)
    @Mapping(target = "bucketName", expression = "java(conversionFile.getPath().contains(\"/\") ? conversionFile.getPath().substring(0, conversionFile.getPath().indexOf(\"/\")) : conversionFile.getPath())")
    ConversionFileOutbox toFileConversionOutbox(ConversionFile conversionFile);

    @Mapping(target = "status", expression = "java(conversionFile.getStatus() != null ? conversionFile.getStatus().name() : null)")
    FileResponseDto toFileResponseDto(ConversionFile conversionFile);
}