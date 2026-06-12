FROM gradle:9.4.1-jdk17 as build

WORKDIR /app

COPY settings.gradle.kts ./
COPY gradle ./gradle
COPY gradlew ./

COPY build.gradle.kts ./
COPY src ./src

RUN chmod +x gradlew
RUN ./gradlew bootJar


FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/build/libs/*SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]

EXPOSE 8080