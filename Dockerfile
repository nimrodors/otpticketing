FROM openjdk:21-jdk-slim AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apt-get update && apt-get install -y maven
RUN mvn clean package

FROM openjdk:21-jdk-slim

# Másold át a build-elt JAR fájlt (cseréld ki a nevet, ha más a tiéd)
COPY --from=build /app/target/your-app-name.jar /app/app.jar

# Environment variable-ek az application.properties override-jéhez (opcionális, de ajánlott)
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/mydb
ENV SPRING_DATASOURCE_USERNAME=user
ENV SPRING_DATASOURCE_PASSWORD=pass

# Futtasd az appot
CMD ["java", "-jar", "/app/app.jar"]