# ================================================
# Etapa de construcción con Maven
# ================================================
FROM maven:3.8.6-openjdk-8 AS builder

# Configuración básica
ENV MAVEN_OPTS="-Dmaven.repo.local=/root/.m2/repository -Xmx1024m -XX:MaxPermSize=512m -Duser.home=/root"

# Directorio de trabajo
WORKDIR /app

# 1. Copiar archivos de configuración
COPY pom.xml .
COPY src/main/resources/application-docker.properties src/main/resources/application.properties

# 2. Descargar dependencias
RUN mvn -B dependency:resolve

# 3. Copiar el código fuente
COPY src ./src

# 4. Compilar el código
RUN mvn -B clean compile

# 5. Empaquetar la aplicación
RUN mvn -B package -DskipTests

# ================================================
# Etapa de producción
# ================================================
FROM openjdk:8-jre-slim

# Variables de entorno
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xms256m -Xmx512m -Djava.security.egd=file:/dev/./urandom"

# Instalar curl para health checks
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Directorio de trabajo
WORKDIR /app

# Copiar el JAR desde la etapa de construcción
COPY --from=builder /app/target/*.jar app.jar

# Puerto expuesto
EXPOSE 8080

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
