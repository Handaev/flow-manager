package org.example.flowmanager.api.controller.base;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseContext {

    @LocalServerPort
    protected Integer port;

    @Autowired
    protected StringRedisTemplate redisTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.0"));
    static final MinIOContainer minio = new MinIOContainer("minio/minio:RELEASE.2025-09-07T16-13-09Z");
    static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

    static {
        postgres.start();
        kafka.start();
        minio.start();
        redis.start();
    }

    @BeforeEach
    void setUpRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    protected void executeRedisJsonScript(String resourcePath) {
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            String jsonContent = resource.getContentAsString(StandardCharsets.UTF_8);

            Map<String, Object> redisData = objectMapper.readValue(
                    jsonContent,
                    new TypeReference<Map<String, Object>>() {}
            );

            redisData.forEach((key, value) -> {
                try {
                    String jsonString = objectMapper.writeValueAsString(value);
                    redisTemplate.opsForValue().set(key, jsonString);
                } catch (Exception e) {
                    throw new RuntimeException("Ошибка сериализации значения для ключа: " + key, e);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Не удалось загрузить Redis JSON скрипт: " + resourcePath, e);
        }
    }

    private static void initMinioBuckets(String s3Url, String user, String password) {
        try {
            MinioClient initClient = MinioClient.builder()
                    .endpoint(s3Url)
                    .credentials(user, password)
                    .build();

            String[] buckets = {"sources", "pdf"};

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

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);

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
