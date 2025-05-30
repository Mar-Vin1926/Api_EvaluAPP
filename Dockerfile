# ================================================
# Etapa de construcción
# ================================================
FROM eclipse-temurin:8-jdk-alpine AS builder

# Configuración del usuario no root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Directorio de trabajo
WORKDIR /app

# Copiar solo los archivos necesarios para la instalación de dependencias
COPY --chown=spring:spring .mvn/ .mvn/
COPY --chown=spring:spring mvnw pom.xml ./

# Descargar dependencias
RUN ./mvnw dependency:go-offline -B

# Copiar el código fuente
COPY --chown=spring:spring src ./src

# Construir la aplicación
RUN ./mvnw clean package -DskipTests

# ================================================
# Etapa de producción
# ================================================
FROM eclipse-temurin:8-jre-alpine

# Variables de entorno
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:MaxRAM=512m -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=prod"

# Crear usuario no root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Directorio de trabajo
WORKDIR /app

# Copiar el JAR desde la etapa de construcción
COPY --from=builder --chown=spring:spring /app/target/*.jar app.jar

# Puerto expuesto (para documentación, no es necesario exponer en Cloud Run)
EXPOSE 8080

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
