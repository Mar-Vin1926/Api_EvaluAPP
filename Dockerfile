# ================================================
# Etapa de construcción
# ================================================
FROM maven:3.8.6-openjdk-8 AS builder

# Configurar Maven para mejor rendimiento
ENV MAVEN_OPTS="-Dmaven.repo.local=/root/.m2/repository -Dmaven.wagon.http.retryHandler.count=3 -Dmaven.wagon.http.retryHandler.interval=1000 -Dmaven.wagon.http.retryHandler.requestSentEnabled=true -Dmaven.wagon.rto=10000 -Dmaven.wagon.http.retryHandler.requestSentEnabled=true -Dmaven.wagon.http.retryHandler.requestSentEnabled=true"

# Directorio de trabajo
WORKDIR /app

# Copiar solo el pom.xml primero para cachear dependencias
COPY pom.xml .

# Descargar dependencias
RUN mvn -B dependency:go-offline || \
    (rm -rf ~/.m2/repository && mvn -B dependency:go-offline)

# Copiar el código fuente
COPY src ./src

# Construir la aplicación
RUN mvn -B clean package -DskipTests \
    -Dmaven.test.skip=true \
    -Dmaven.javadoc.skip=true \
    -Dmaven.source.skip=true \
    -Dmaven.install.skip=true \
    -Dmaven.site.skip=true \
    -Dmaven.clean.skip=true \
    -Dmaven.deploy.skip=true \
    -Dmaven.dependency.skip=true

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
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
