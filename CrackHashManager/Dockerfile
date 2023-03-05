FROM openjdk:17-jdk-slim AS build
WORKDIR /crack-hash-manager
COPY . /crack-hash-manager
RUN ./gradlew bootJar

WORKDIR /
ENTRYPOINT ["java","-jar","/crack-hash-manager/build/libs/manager.jar"]