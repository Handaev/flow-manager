package org.example.flowmanager.api.repository;

import org.example.flowmanager.api.entity.ConversionFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversionFileRepository extends JpaRepository<ConversionFile, Long> {

    Optional<ConversionFile> findByNameAndPathAndToExtension(String name, String path, String toExtension);

    @Query(value = "select * from conversion_file c where c.created_at < now() - interval '10 minutes' and c.status = :status limit :limit", nativeQuery = true)
    List<ConversionFile> findAllByCreatedAtMoreTenMinutesAndStatusFailed(int limit, String status) ;

}