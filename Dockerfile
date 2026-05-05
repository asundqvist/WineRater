FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN ./mvnw clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/06_devops_assignment_1_individual-0.0.1-SNAPSHOT.jar"]

## TO BE UPDATED WITH FRONTEND ##