package org.example.flowmanager.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.nio.file.Files;

@Setter
@Getter
@NoArgsConstructor
public class ConversionMultipartFile implements MultipartFile {

    private Long id;

    private String name;

    private String originalFileName;

    private byte[] content;

    private String path;

    private String fromExtension;

    private String toExtension;

    private String contentType;

    @Override
    public String getOriginalFilename() {
        return originalFileName;
    }

    @Override
    public boolean isEmpty() {
        return Objects.isNull(content) || content.length == 0;
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @Override
    public byte[] getBytes() {
        return content;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(File dest) throws IllegalStateException, IOException {
        Files.write(dest.toPath(), this.content);
    }
}