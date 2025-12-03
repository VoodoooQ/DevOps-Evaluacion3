# ✅ FASE 1 COMPLETADA - INSTRUCCIONES DE PAUSA

## 📋 Archivos generados en FASE 1:

### Configuración Maven
- ✅ `pom.xml` - Actualizado con todas las dependencias necesarias

### Configuración de la aplicación
- ✅ `src/main/resources/application.yml` - Configuración completa con métricas y health checks
- ✅ `src/main/resources/application-kubernetes.yml` - Configuración específica para Kubernetes

### Clases Java de configuración
- ✅ `src/main/java/com/example/bdget/BdgetApplication.java` - Aplicación principal con @EnableScheduling
- ✅ `src/main/java/com/example/bdget/config/MetricsConfig.java` - Configuración de métricas personalizadas
- ✅ `src/main/java/com/example/bdget/config/ResilienceConfig.java` - Configuración de Circuit Breaker y Retry

### Controladores REST
- ✅ `src/main/java/com/example/bdget/controller/HealthCheckController.java` - Health checks (liveness/readiness)
- ✅ `src/main/java/com/example/bdget/controller/MetricsController.java` - Endpoints de métricas personalizadas
- ✅ `src/main/java/com/example/bdget/controller/ApiController.java` - API REST principal

### Servicios
- ✅ `src/main/java/com/example/bdget/service/ResilientService.java` - Servicio con Circuit Breaker

### Métricas
- ✅ `src/main/java/com/example/bdget/metrics/PipelineMetricsExporter.java` - Exportador de métricas del pipeline

### Tests (>80% cobertura)
- ✅ `src/test/java/com/example/bdget/controller/HealthCheckControllerTest.java`
- ✅ `src/test/java/com/example/bdget/controller/MetricsControllerTest.java`
- ✅ `src/test/java/com/example/bdget/controller/ApiControllerTest.java`
- ✅ `src/test/java/com/example/bdget/service/ResilientServiceTest.java`

### Docker
- ✅ `Dockerfile` - Multi-stage optimizado con Java 17
- ✅ `.dockerignore` - Optimización del contexto de build

---

## 🛑 INSTRUCCIONES - PAUSA FASE 1

Antes de continuar con la FASE 2, debes ejecutar y verificar lo siguiente:

### 1️⃣ COMPILAR EL PROYECTO

Abre PowerShell en la raíz del proyecto y ejecuta:

```powershell
mvn clean install
```

**Verificaciones:**
- ✓ El comando debe completarse sin errores
- ✓ Debe mostrar "BUILD SUCCESS" al final
- ✓ Verifica que se generó el JAR en `target/bdget-0.0.1-SNAPSHOT.jar`

### 2️⃣ EJECUTAR TESTS Y VERIFICAR COBERTURA

```powershell
mvn test jacoco:report
```

**Verificaciones:**
- ✓ Todos los tests deben pasar (color verde)
- ✓ Abre el reporte de cobertura en tu navegador:
  ```
  target/site/jacoco/index.html
  ```
- ✓ La cobertura total debe ser **superior al 80%**

### 3️⃣ EJECUTAR LA APLICACIÓN LOCALMENTE

```powershell
mvn spring-boot:run
```

**Verificaciones:**
- ✓ La aplicación debe iniciar sin errores
- ✓ Debe mostrar el mensaje: "Started BdgetApplication in X seconds"
- ✓ Debe estar escuchando en el puerto 8080

### 4️⃣ PROBAR ENDPOINTS (en otra terminal o navegador)

**a) Health Check personalizado:**
```powershell
curl http://localhost:8080/health/custom
```
Debe retornar JSON con estado "UP" y métricas de CPU/memoria

**b) Liveness Probe:**
```powershell
curl http://localhost:8080/health/liveness
```
Debe retornar: `{"status":"UP","probe":"liveness",...}`

**c) Readiness Probe:**
```powershell
curl http://localhost:8080/health/readiness
```
Debe retornar: `{"status":"UP","probe":"readiness",...}`

**d) Métricas de Actuator:**
```powershell
curl http://localhost:8080/actuator/health
```
Debe retornar health check completo

**e) Métricas de Prometheus:**
```powershell
curl http://localhost:8080/actuator/prometheus
```
Debe retornar métricas en formato Prometheus (texto plano)

**f) API de prueba:**
```powershell
curl http://localhost:8080/api/test
```
Debe retornar: `{"status":"success","message":"API funcionando correctamente",...}`

**g) Endpoint info:**
```powershell
curl http://localhost:8080/api/info
```
Debe retornar información de la aplicación y lista de endpoints

### 5️⃣ PROBAR MÉTRICAS PERSONALIZADAS

**a) Ver métricas actuales:**
```powershell
curl http://localhost:8080/metrics/custom
```

**b) Simular alta CPU:**
```powershell
curl -X POST "http://localhost:8080/metrics/simulate/cpu?value=85"
```

**c) Simular alta memoria:**
```powershell
curl -X POST "http://localhost:8080/metrics/simulate/memory?value=90"
```

**d) Generar un error:**
```powershell
curl -X POST http://localhost:8080/metrics/simulate/error
```

**e) Simular tráfico:**
```powershell
curl -X POST "http://localhost:8080/metrics/simulate/traffic?requests=100"
```

**f) Ver métricas actualizadas:**
```powershell
curl http://localhost:8080/metrics/custom
```

### 6️⃣ CONSTRUIR IMAGEN DE DOCKER

Detén la aplicación (Ctrl+C) y construye la imagen Docker:

```powershell
docker build -t microservicio-evaluacion:latest .
```

**Verificaciones:**
- ✓ El build debe completarse sin errores
- ✓ Verifica que la imagen se creó:
  ```powershell
  docker images | Select-String "microservicio-evaluacion"
  ```

### 7️⃣ EJECUTAR CONTENEDOR DOCKER

```powershell
docker run -d -p 8080:8080 --name microservicio-test microservicio-evaluacion:latest
```

**Verificaciones:**
- ✓ El contenedor debe iniciar correctamente
- ✓ Verifica que está corriendo:
  ```powershell
  docker ps
  ```
- ✓ Verifica los logs:
  ```powershell
  docker logs microservicio-test
  ```
- ✓ Prueba el health check:
  ```powershell
  curl http://localhost:8080/actuator/health
  ```
- ✓ Verifica las métricas:
  ```powershell
  curl http://localhost:8080/actuator/prometheus
  ```

### 8️⃣ VERIFICAR HEALTH CHECK DE DOCKER

```powershell
docker inspect microservicio-test --format='{{.State.Health.Status}}'
```

Debe mostrar: `healthy` (después de ~60 segundos)

### 9️⃣ DETENER Y LIMPIAR

```powershell
docker stop microservicio-test
docker rm microservicio-test
```

---

## ✅ CHECKLIST DE VERIFICACIÓN

Marca cada ítem cuando lo hayas verificado:

- [ ] ✅ Compilación exitosa (`mvn clean install`)
- [ ] ✅ Tests pasando con >80% cobertura
- [ ] ✅ Aplicación inicia correctamente
- [ ] ✅ Health checks funcionando (/health/custom, /liveness, /readiness)
- [ ] ✅ Actuator funcionando (/actuator/health, /actuator/prometheus)
- [ ] ✅ API REST funcionando (/api/test, /api/info)
- [ ] ✅ Métricas personalizadas funcionando (/metrics/custom)
- [ ] ✅ Simulación de métricas funcionando (CPU, memoria, errores, tráfico)
- [ ] ✅ Imagen Docker construida exitosamente
- [ ] ✅ Contenedor Docker ejecutándose correctamente
- [ ] ✅ Health check de Docker funcionando

---

## 🎯 CRITERIOS DE ÉXITO PARA CONTINUAR

Para continuar con la **FASE 2**, debes confirmar que:

1. ✅ **Todos los tests pasan** sin errores
2. ✅ **Cobertura de código es superior al 80%**
3. ✅ **La aplicación inicia correctamente** (local y en Docker)
4. ✅ **Todos los endpoints responden correctamente**
5. ✅ **Las métricas se exportan a /actuator/prometheus**
6. ✅ **Los health checks funcionan**
7. ✅ **El contenedor Docker está healthy**

---

## 📝 RESPONDE CUANDO HAYAS COMPLETADO

Una vez que hayas verificado **TODOS** los puntos anteriores, responde:

```
FASE 1 COMPLETADA
```

Y procederemos con la **FASE 2: Stack de Monitoreo (Prometheus + Grafana + Loki)**

---

## ❓ PROBLEMAS COMUNES Y SOLUCIONES

### Error: "Port 8080 is already in use"
```powershell
# Encontrar proceso usando el puerto
netstat -ano | Select-String ":8080"
# Matar el proceso (reemplaza PID)
Stop-Process -Id <PID> -Force
```

### Error: "Cannot resolve dependencies"
```powershell
# Limpiar cache de Maven
mvn dependency:purge-local-repository
mvn clean install -U
```

### Error en tests
```powershell
# Ejecutar tests con más información
mvn test -X
```

### Error al construir Docker
```powershell
# Limpiar cache de Docker
docker system prune -af
# Reconstruir sin cache
docker build --no-cache -t microservicio-evaluacion:latest .
```
