package org.example.flowmanager.api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.PrePersist;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Objects;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "conversion_file")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ConversionFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @EqualsAndHashCode.Include
    @Column(nullable = false)
    private String name;

    @EqualsAndHashCode.Include
    @Column(name = "from_extension", nullable = false)
    private String fromExtension;

    @EqualsAndHashCode.Include
    @Column(nullable = false)
    private String path;

    @EqualsAndHashCode.Include
    @Column(name = "to_extension")
    private String toExtension;

    @Builder.Default
    @EqualsAndHashCode.Include
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusFile status = StatusFile.IN_PROCESSING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (Objects.isNull(this.status)) {
            this.status = StatusFile.IN_PROCESSING;
        }
        this.createdAt = LocalDateTime.now();
    }
}