package org.example.flowmanager.api.repository.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.example.flowmanager.api.entity.ConversionFile;
import org.example.flowmanager.api.repository.utils.ConstantRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ConversionFileRepositoryJpa {

    @PersistenceContext
    private EntityManager entityManager;

    public ConversionFile findConversionFileByBucketNameAndFileName(String bucketName, String name, String toExtension) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ConversionFile> query = builder.createQuery(ConversionFile.class);
        Root<ConversionFile> root = query.from(ConversionFile.class);

        query.select(root).where(
                builder.like(root.get(ConstantRepository.FIELDS_PATH), bucketName + ConstantRepository.PERCENT),
                builder.like(root.get(ConstantRepository.FIELDS_NAME), name + ConstantRepository.PERCENT),
                builder.equal(root.get(ConstantRepository.FIELDS_TO_EXTENSION), toExtension)
        );
        return entityManager.createQuery(query).getSingleResult();
    }

}
