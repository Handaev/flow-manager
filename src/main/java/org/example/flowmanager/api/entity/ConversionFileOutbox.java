package org.example.flowmanager.api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import org.hibernate.annotations.ColumnDefault;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "file_conversion_outbox")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ConversionFileOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @EqualsAndHashCode.Include
    @Column(name = "bucket_name", nullable = false)
    private String bucketName;

    @EqualsAndHashCode.Include
    @Column(nullable = false)
    private String name;

    @EqualsAndHashCode.Include
    @Column(name = "to_extension", nullable = false)
    private String toExtension;

    @EqualsAndHashCode.Include
    @ColumnDefault("false")
    @Column(name = "is_sent", nullable = false)
    private boolean sent;
}
