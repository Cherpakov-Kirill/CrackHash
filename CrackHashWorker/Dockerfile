FROM openjdk:17-jdk-slim AS build
WORKDIR /crack-hash-worker
COPY . /crack-hash-worker
RUN ./gradlew bootJar

WORKDIR /
ENTRYPOINT ["java","-jar","/crack-hash-worker/build/libs/worker.jar"]