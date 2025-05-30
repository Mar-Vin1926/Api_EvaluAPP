# ================================================
# Etapa de construcción con Maven
# ================================================
FROM maven:3.8.6-openjdk-8 AS builder

# Mostrar información de depuración
RUN mvn --version && \
    java -version && \
    echo "M2_HOME: $M2_HOME" && \
    echo "MAVEN_HOME: $MAVEN_HOME" && \
    echo "PATH: $PATH"

# Configurar Maven para mejor rendimiento
ENV MAVEN_OPTS="-Dmaven.repo.local=/root/.m2/repository -Xmx1024m -XX:MaxPermSize=512m"

# Crear directorio de trabajo
WORKDIR /app

# Copiar solo el pom.xml primero
COPY pom.xml .

# Descargar dependencias con reintentos
RUN mvn -B dependency:go-offline || \
    (echo "Primer intento fallido, reintentando..." && \
     mvn -B dependency:go-offline) || \
    (echo "Segundo intento fallido, limpiando y reintentando..." && \
     rm -rf ~/.m2/repository && \
     mvn -B dependency:go-offline)

# Copiar el código fuente
COPY src ./src

# Construir la aplicación con logs detallados
RUN mvn -B clean compile dependency:tree && \
    mvn -B package -DskipTests

# ================================================
# Etapa de producción
# ================================================
FROM openjdk:8-jre-slim

# Variables de entorno
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xms256m -Xmx512m -Djava.security.egd=file:/dev/./urandom"

# Directorio de trabajo
WORKDIR /app

# Copiar el JAR desde la etapa de construcción
COPY --from=builder /app/target/*.jar app.jar

# Puerto expuesto
EXPOSE 8080

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
