# ================================================
# Etapa de construcción con Maven
# ================================================
FROM maven:3.8.6-openjdk-8 AS builder

# Configuración básica
ENV MAVEN_OPTS="-Dmaven.repo.local=/root/.m2/repository -Xmx1024m -XX:MaxPermSize=512m -Duser.home=/root -Dmaven.wagon.http.retryHandler.count=3"
ENV MAVEN_OPTS="$MAVEN_OPTS -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn"

# Directorio de trabajo
WORKDIR /app

# 1. Copiar solo el pom.xml
COPY pom.xml .

# 2. Mostrar información del entorno
RUN echo "=== Versión de Java ===" && \
    java -version && \
    echo "=== Versión de Maven ===" && \
    mvn -version && \
    echo "=== Variables de entorno ===" && \
    env | sort

# 3. Descargar dependencias con reintentos y manejo de errores
RUN set -e; \
    echo "=== Descargando dependencias (intento 1) ===" && \
    mvn -B dependency:go-offline || ( \
        echo "=== Falló el primer intento, limpiando caché... ===" && \
        rm -rf /root/.m2/repository && \
        echo "=== Reintentando descarga de dependencias... ===" && \
        mvn -B dependency:go-offline || ( \
            echo "=== Falló el segundo intento, intentando con resolución simple... ===" && \
            mvn -B dependency:resolve \
        ) \
    )

# 4. Copiar el código fuente
COPY src ./src

# 5. Compilar el código con información detallada
RUN echo "=== Compilando el código... ===" && \
    mvn -B clean compile -e -X && \
    echo "=== Mostrando árbol de dependencias... ===" && \
    mvn -B dependency:tree

# 6. Empaquetar la aplicación
RUN echo "=== Empaquetando la aplicación... ===" && \
    mvn -B package -DskipTests

# ================================================
# Etapa de producción
# ================================================
FROM openjdk:8-jre-slim

# Variables de entorno
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xms256m -Xmx512m -Djava.security.egd=file:/dev/./urandom"

# Instalar herramientas de diagnóstico
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
