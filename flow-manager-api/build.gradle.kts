plugins {
    java
    id("org.openapi.generator") version "7.4.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:3.2.5")
    implementation("org.springframework.boot:spring-boot-starter-validation:3.2.5")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
}

openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("$rootDir/flow-manager-app/src/main/resources/static/openapi/openapi-flow-manager.yaml")
    outputDir.set(layout.buildDirectory.dir("generated/openapi").map { it.asFile.absolutePath })

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
            srcDir(tasks.openApiGenerate.map { "${it.outputDir.get()}/src/main/java" })
        }
    }
}

tasks.compileJava {
    dependsOn(tasks.openApiGenerate)
}
