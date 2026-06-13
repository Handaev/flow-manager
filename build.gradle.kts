plugins {
    java
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.5"
    id("org.openapi.generator") version "7.4.0"
}

group = "org.example"
version = "0.0.1-SNAPSHOT"
description = "flow-manager"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2025.1.1"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.postgresql:postgresql:42.7.11")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

    implementation("org.mapstruct:mapstruct:1.6.2")
    implementation("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    implementation("io.awspring.cloud:spring-cloud-aws-starter-s3:3.1.1")

    implementation("org.springframework.kafka:spring-kafka:3.2.4")
    implementation("org.apache.kafka:kafka-clients:3.7.0")

    implementation("io.minio:minio:9.0.0")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

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

    testCompileOnly("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("$rootDir/src/main/resources/static/openapi/openapi-flow-manager.yaml")
    outputDir.set("$buildDir/generated/openapi")

    apiPackage.set("org.example.flowmanager.controller")
    modelPackage.set("org.example.flowmanager.dto")
    modelNameSuffix.set("Dto")

    configOptions.set(mapOf(
        "interfaceOnly" to "true",
        "useSpringBoot3" to "true",
        "openApiNullable" to "false",
        "skipDefaultInterface" to "true",
        "useTags" to "true"
    ))
}

sourceSets {
    main {
        java {
            srcDir("$buildDir/generated/openapi/src/main/java")
        }
    }
}

tasks.compileJava {
    dependsOn(tasks.openApiGenerate)
}


tasks.withType<Test> {
    useJUnitPlatform()
}
