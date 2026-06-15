plugins {
    java
    id("org.springframework.boot") version "3.2.5"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(enforcedPlatform("org.springframework.boot:spring-boot-dependencies:3.2.5"))
    implementation(enforcedPlatform("org.springframework.cloud:spring-cloud-dependencies:2023.0.1"))

    implementation(project(":flow-manager-api"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.postgresql:postgresql:42.7.11")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

    implementation("org.springframework.kafka:spring-kafka:3.2.4")
    implementation("org.apache.kafka:kafka-clients:3.7.0")

    implementation("io.awspring.cloud:spring-cloud-aws-starter-s3:3.1.1")
    implementation("io.minio:minio:9.0.0")

    implementation("org.mapstruct:mapstruct:1.6.2")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:testcontainers:1.21.4")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:kafka:1.21.4")
    testImplementation("org.testcontainers:minio:1.19.7")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.awaitility:awaitility")
    testImplementation("org.apache.commons:commons-compress:1.24.0")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")

    testCompileOnly("org.projectlombok:lombok:1.18.32")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.32")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
