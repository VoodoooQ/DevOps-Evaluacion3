# =============================================================================
# DOCKERFILE MULTI-STAGE OPTIMIZADO PARA MICROSERVICIO EVALUACIÓN DEVOPS
# =============================================================================
# Este Dockerfile implementa las mejores prácticas de contenedorización:
# - Multi-stage build para reducir tamaño de imagen final
# - Usuario no-root para seguridad
# - Health checks integrados
# - Optimizaciones de JVM para contenedores
# - Cache de dependencias de Maven
# - Imagen base Alpine (ligera y segura)

# =============================================================================
# STAGE 1: BUILD
# =============================================================================
# Esta etapa compila la aplicación y genera el JAR ejecutable
FROM eclipse-temurin:17-jdk-alpine AS builder

# Información del mantenedor
LABEL maintainer="evaluacion-devops@example.com"
LABEL description="Microservicio con observabilidad completa (Prometheus + Grafana + Loki)"
LABEL version="1.0.0"

# Directorio de trabajo para el build
WORKDIR /app

# Instalar Maven (build tool)
RUN apk add --no-cache maven

# Copiar SOLO pom.xml primero (optimización de cache de Docker)
# Si las dependencias no cambian, Docker reutilizará esta capa
COPY pom.xml .

# Descargar todas las dependencias de Maven (offline mode)
# Esta capa se cachea y solo se reconstruye si pom.xml cambia
RUN mvn dependency:go-offline -B

# Copiar código fuente de la aplicación
COPY src ./src

# Compilar la aplicación y generar el JAR
# -DskipTests: Los tests ya se ejecutan en el pipeline CI/CD
# -B: Batch mode (sin output interactivo)
# -e: Mostrar errores completos
RUN mvn clean package -DskipTests -B -e

# Verificar que el JAR se generó correctamente
RUN ls -la /app/target/*.jar

# =============================================================================
# STAGE 2: RUNTIME
# =============================================================================
# Esta etapa crea la imagen final optimizada solo con lo necesario para ejecutar
FROM eclipse-temurin:17-jre-alpine

# Información de la imagen final
LABEL maintainer="evaluacion-devops@example.com"
LABEL description="Microservicio Evaluación DevOps - Runtime optimizado"
LABEL version="1.0.0"

# Directorio de trabajo para la aplicación
WORKDIR /app

# Instalar herramientas necesarias para health checks y debugging
# wget: Para health checks
# curl: Para debugging y health checks alternativos
RUN apk add --no-cache wget curl

# Copiar el JAR compilado desde la etapa de build
# Esto mantiene la imagen final pequeña (solo JRE + JAR, no JDK ni Maven)
COPY --from=builder /app/target/*.jar app.jar

# =============================================================================
# SEGURIDAD: Crear usuario no-root
# =============================================================================
# Ejecutar la aplicación como usuario no-root mejora la seguridad
# Si el contenedor es comprometido, el atacante no tendrá privilegios root
RUN addgroup -S spring && adduser -S spring -G spring

# Crear directorio para logs
RUN mkdir -p /var/log/app && chown -R spring:spring /var/log/app

# Dar permisos al usuario spring sobre el JAR
RUN chown -R spring:spring /app

# Cambiar al usuario spring (no-root)
USER spring:spring

# =============================================================================
# CONFIGURACIÓN DE PUERTOS Y HEALTH CHECKS
# =============================================================================

# Puerto de la aplicación
EXPOSE 8080

# Puerto de métricas de Actuator (mismo puerto en este caso)
EXPOSE 8080

# Health check de Docker
# Verifica que la aplicación esté respondiendo correctamente
# --interval=30s: Verificar cada 30 segundos
# --timeout=5s: Timeout de 5 segundos para cada check
# --start-period=60s: Esperar 60s antes de comenzar checks (tiempo de inicio)
# --retries=3: 3 fallos consecutivos = contenedor unhealthy
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# =============================================================================
# VARIABLES DE ENTORNO (pueden ser sobreescritas)
# =============================================================================
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV SPRING_PROFILES_ACTIVE=default

# =============================================================================
# ENTRYPOINT: Ejecutar la aplicación
# =============================================================================
# Configuración optimizada de JVM para contenedores:
# -XX:+UseContainerSupport: Detectar límites de memoria del contenedor
# -XX:MaxRAMPercentage=75.0: Usar máximo 75% de memoria disponible
# -Djava.security.egd: Mejorar rendimiento de generación de números aleatorios
# -Dspring.profiles.active: Activar perfil de Spring (puede ser sobreescrito)
ENTRYPOINT ["sh", "-c", "java \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseStringDeduplication \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} \
    ${JAVA_OPTS} \
    -jar app.jar"]

# =============================================================================
# NOTAS DE USO:
# =============================================================================
# Build:
#   docker build -t microservicio-evaluacion:latest .
#
# Run (desarrollo):
#   docker run -p 8080:8080 microservicio-evaluacion:latest
#
# Run (Kubernetes profile):
#   docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=kubernetes microservicio-evaluacion:latest
#
# Run con límites de memoria:
#   docker run -p 8080:8080 -m 512m microservicio-evaluacion:latest
#
# Health check:
#   curl http://localhost:8080/actuator/health
#
# Métricas (Prometheus):
#   curl http://localhost:8080/actuator/prometheus
# =============================================================================
