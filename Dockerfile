# ==============================================================================
# Dockerfile - VitalBite Documental Microservice (Spring Boot 4 / Java 17)
# Optimizado para DigitalOcean App Platform
# ==============================================================================

# ========== STAGE 1: BUILD ==========
# Maven con JDK 17 (debe coincidir con <java.version> del pom.xml)
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copiar pom.xml y resolver dependencias (aprovecha la caché de capas)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests -B

# ========== STAGE 2: RUNTIME ==========
# Solo JRE 17 (más liviano que el JDK completo; no se necesita compilar aquí)
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

LABEL maintainer="VitalBite Team"
LABEL description="VitalBite Documental Microservice - Production"

# curl solo para el healthcheck
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Usuario no-root
RUN useradd -m -u 1001 appuser

# Copiar solo el JAR (application.properties ya va dentro del JAR)
COPY --from=builder --chown=appuser:appuser /app/target/*.jar app.jar

USER appuser

# Puerto de Spring Boot (el context-path /api/v1 no afecta al puerto)
EXPOSE 8082

# Valores por defecto; se sobrescriben en DigitalOcean.
# MaxRAMPercentage se adapta a la memoria real del contenedor
# (un -Xmx fijo puede superar el límite del plan y matar el proceso).
ENV SPRING_PROFILES_ACTIVE=production \
    LOG_LEVEL=INFO \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8082/api/v1/actuator/health || exit 1

# exec: la JVM recibe SIGTERM y se apaga de forma ordenada
ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS} -jar /app/app.jar"]