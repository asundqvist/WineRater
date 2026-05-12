FROM eclipse-temurin:21-jdk

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY . .

RUN ./mvnw clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/06_devops_assignment_1_individual-0.0.1-SNAPSHOT.jar"]
