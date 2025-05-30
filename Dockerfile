# ================================================
# Etapa de construcción con Maven
# ================================================
FROM maven:3.8.6-openjdk-8 AS builder

# Configuración básica
ENV MAVEN_OPTS="-Dmaven.repo.local=/root/.m2/repository -Xmx1024m -XX:MaxPermSize=512m -Duser.home=/root -Dmaven.wagon.http.retryHandler.count=3"

# Directorio de trabajo
WORKDIR /app

# 1. Copiar solo el pom.xml
COPY pom.xml .

# 2. Descargar dependencias con reintentos y manejo de errores
RUN \
    echo "=== Descargando dependencias (intento 1) ===" && \
    mvn -B dependency:go-offline || ( \
        echo "=== Falló el primer intento, limpiando caché... ===" && \
        rm -rf /root/.m2/repository && \
        echo "=== Reintentando descarga de dependencias... ===" && \
        mvn -B dependency:go-offline \
    )

# 3. Copiar el código fuente
COPY src ./src

# 4. Compilar el código con información de depuración
RUN echo "=== Compilando el código... ===" && \
    mvn -B clean compile && \
    echo "=== Mostrando árbol de dependencias... ===" && \
    mvn -B dependency:tree

# 5. Empaquetar la aplicación
RUN echo "=== Empaquetando la aplicación... ===" && \
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
