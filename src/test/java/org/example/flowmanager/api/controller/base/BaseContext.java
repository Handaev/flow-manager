package org.example.flowmanager.api.controller.base;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseContext {

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));
    static final MinIOContainer minio = new MinIOContainer("minio/minio:RELEASE.2025-09-07T16-13-09Z");

    static {
        postgres.start();
        kafka.start();
        minio.start();
    }

    private static void initMinioBuckets(String s3Url, String user, String password) {
        try {
            MinioClient initClient = MinioClient.builder()
                    .endpoint(s3Url)
                    .credentials(user, password)
                    .build();

            String[] buckets = {"aaaa", "pdf"};

            for (String bucket : buckets) {
                boolean exists = initClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
                if (!exists) {
                    initClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error init buckets", e);
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.consumer.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.producer.bootstrap-servers", kafka::getBootstrapServers);

        String s3Url = minio.getS3URL();
        String user = minio.getUserName();
        String password = minio.getPassword();

        initMinioBuckets(s3Url, user, password);

        registry.add("spring.cloud.aws.s3.endpoint", () -> s3Url);
        registry.add("spring.cloud.aws.credentials.access-key", () -> user);
        registry.add("spring.cloud.aws.credentials.secret-key", () -> password);
        registry.add("spring.cloud.aws.region.static", () -> "us-east-1");
    }
}
