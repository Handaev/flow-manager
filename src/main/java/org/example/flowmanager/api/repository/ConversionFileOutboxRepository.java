package org.example.flowmanager.api.repository;

import org.example.flowmanager.api.entity.ConversionFileOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversionFileOutboxRepository extends JpaRepository<ConversionFileOutbox, Long> {

    List<ConversionFileOutbox> findAllBySentFalse();

    Optional<ConversionFileOutbox> findByNameAndBucketNameAndToExtension(String name, String bucketName, String extension);
}
