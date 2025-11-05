# English: Build stage with Maven and JDK 23
FROM maven:3.9-eclipse-temurin-23 AS build
WORKDIR /app

# English: copy only pom first for better layer caching
COPY pom.xml ./
RUN mvn -q -e -B -DskipTests dependency:go-offline

# English: copy sources and build
COPY src ./src
RUN mvn -q -e -B -DskipTests package

# English: Run stage with JDK 23 (use JRE if you have a tag available)
FROM eclipse-temurin:23-jdk
WORKDIR /opt/app

# English: copy Spring Boot fat JAR. Adjust name if your finalName differs.
COPY --from=build /app/target/*.jar app.jar

# English: run with minimal, JVM ergonomics can be tuned later
EXPOSE 8080
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]
