# ================================================
# Etapa de construcción con Maven
# ================================================
FROM maven:3.8.6-openjdk-8 AS builder

# Configuración básica
ENV MAVEN_OPTS="-Dmaven.repo.local=/root/.m2/repository -Xmx1024m -XX:MaxPermSize=512m -Duser.home=/root"
ENV MAVEN_OPTS="$MAVEN_OPTS -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn"

# Directorio de trabajo
WORKDIR /app

# 1. Copiar solo el POM primero para cachear dependencias
COPY pom.xml .

# 2. Descargar dependencias
RUN mvn -B dependency:go-offline || (\
    echo "=== Falló la descarga inicial, limpiando caché... ===" && \
    rm -rf /root/.m2/repository && \
    mvn -B dependency:resolve \
)

# 3. Copiar el código fuente
COPY src ./src

# 4. Copiar propiedades de la aplicación
COPY src/main/resources/application-docker.properties src/main/resources/application.properties

# 5. Compilar el proyecto con información detallada
RUN mvn -B clean package -DskipTests -e -X

# ================================================
# Etapa de producción
# ================================================
FROM openjdk:8-jre-slim

# Variables de entorno
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xms256m -Xmx512m -Djava.security.egd=file:/dev/./urandom -Dfile.encoding=UTF-8"

# Instalar herramientas básicas para troubleshooting
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

# Directorio de trabajo
WORKDIR /app

# Copiar el JAR desde la etapa de construcción
COPY --from=builder /app/target/*.jar app.jar

# Puerto expuesto
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
