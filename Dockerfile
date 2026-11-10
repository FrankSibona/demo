# Etapa 1: Build de la aplicación
FROM eclipse-temurin:25-jdk-noble AS builder
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Etapa 2: Imagen final ligera con JRE 25
FROM eclipse-temurin:25-jre-noble
WORKDIR /app

# Usuario no root por seguridad
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

COPY --from=builder /app/target/demo-0.0.1-SNAPSHOT.jar app.jar

# Render asigna el puerto mediante la variable de entorno PORT (por defecto 8080)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
