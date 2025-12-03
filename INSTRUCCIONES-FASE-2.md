# INSTRUCCIONES FASE 2 - STACK DE OBSERVABILIDAD

## 📋 Descripción

Esta fase implementa el stack completo de observabilidad para el microservicio, incluyendo:

- **Prometheus**: Recolección de métricas
- **Grafana**: Visualización de métricas y logs
- **Loki**: Agregación de logs
- **Promtail**: Recolección de logs

## 🚀 Pasos de Ejecución

### 1. Construir la Imagen Docker

```powershell
docker build -t bdget-app:latest .
```

### 2. Iniciar el Stack Completo

```powershell
docker-compose up -d
```

Este comando levantará 5 contenedores:
- `app`: Aplicación Spring Boot (puerto 8080)
- `prometheus`: Servidor de métricas (puerto 9090)
- `grafana`: Servidor de dashboards (puerto 3000)
- `loki`: Agregador de logs (puerto 3100)
- `promtail`: Recolector de logs

### 3. Verificar que Todos los Contenedores Estén Activos

```powershell
docker-compose ps
```

Deberías ver todos los servicios con estado "Up":

```
NAME                IMAGE                    STATUS
app                 bdget-app:latest         Up (healthy)
grafana             grafana/grafana:10.2.2   Up
loki                grafana/loki:2.9.3       Up
prometheus          prom/prometheus:v2.48.0  Up
promtail            grafana/promtail:2.9.3   Up
```

### 4. Verificar Logs de los Servicios

```powershell
# Ver logs de la aplicación
docker-compose logs -f app

# Ver logs de Prometheus
docker-compose logs -f prometheus

# Ver logs de Grafana
docker-compose logs -f grafana
```

## 🔍 Verificación de Componentes

### Aplicación Spring Boot

**URL:** http://localhost:8080

**Endpoints clave:**
- Health check: http://localhost:8080/actuator/health
- Métricas Prometheus: http://localhost:8080/actuator/prometheus
- Estudiantes: http://localhost:8080/api/students

**Verificación:**
```powershell
curl http://localhost:8080/actuator/health
```

**Respuesta esperada:**
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"},
    "ping": {"status": "UP"}
  }
}
```

### Prometheus

**URL:** http://localhost:9090

**Verificación de targets:**
1. Abrir http://localhost:9090/targets
2. Verificar que `spring-boot-app` esté en estado "UP"

**Consultas de prueba:**
```promql
# Memoria JVM utilizada
jvm_memory_used_bytes{job="spring-boot-app"}

# Tasa de peticiones HTTP
rate(http_requests_total{job="spring-boot-app"}[1m])

# Estado del Circuit Breaker
resilience4j_circuitbreaker_state{job="spring-boot-app"}
```

### Grafana

**URL:** http://localhost:3000

**Credenciales:**
- Usuario: `admin`
- Contraseña: `admin123`

**Pasos de verificación:**

1. **Login:**
   - Acceder a http://localhost:3000
   - Usar credenciales admin/admin123

2. **Verificar Datasources:**
   - Menu → Connections → Data Sources
   - Verificar que existan:
     - Prometheus (default)
     - Loki

3. **Probar Datasource Prometheus:**
   - Click en "Prometheus"
   - Scroll down y click en "Save & test"
   - Debería aparecer: "Successfully queried the Prometheus API"

4. **Probar Datasource Loki:**
   - Click en "Loki"
   - Scroll down y click en "Save & test"
   - Debería aparecer: "Data source successfully connected"

5. **Verificar Dashboard:**
   - Menu → Dashboards
   - Debería aparecer el folder "DevOps Evaluation"
   - Click en el folder
   - Abrir "Spring Boot - DevOps Observability Dashboard"

6. **Validar Paneles del Dashboard:**
   - **CPU Usage**: Gauge mostrando uso de CPU
   - **Memory Usage**: Gauge mostrando uso de memoria
   - **HTTP Request Rate**: Gráfico de tasa de peticiones/seg
   - **JVM Heap Memory**: Gráfico de memoria heap (used vs max)
   - **Circuit Breaker State**: Estado actual (CLOSED/OPEN/HALF_OPEN)
   - **Circuit Breaker Failure Rate**: Tasa de fallos
   - **Pipeline - Build Metrics**: Métricas de builds (total, exitosos, fallidos)
   - **Pipeline - Code Coverage**: Porcentaje de cobertura de código
   - **Pipeline - Quality Gate Status**: Estado del quality gate
   - **Application Logs**: Panel de logs de Loki

### Loki

**URL:** http://localhost:3100

**Verificación de ingesta de logs:**

```powershell
# Verificar métricas de Loki
curl http://localhost:3100/metrics

# Verificar que Loki esté listo
curl http://localhost:3100/ready
```

**Consultas desde Grafana:**
1. Menu → Explore
2. Seleccionar datasource "Loki"
3. Ejecutar query: `{job="spring-boot-app"}`
4. Deberías ver logs de la aplicación

### Promtail

**Verificación:**

```powershell
# Ver logs de Promtail para verificar recolección
docker-compose logs promtail
```

Deberías ver mensajes como:
```
level=info msg="Starting Promtail"
level=info msg="Seeked /var/log/app/application.log"
```

## 🧪 Pruebas de Integración

### 1. Generar Tráfico a la Aplicación

```powershell
# Crear estudiantes
curl -X POST http://localhost:8080/api/students `
  -H "Content-Type: application/json" `
  -d '{\"name\":\"Juan Perez\",\"email\":\"juan@test.com\"}'

curl -X POST http://localhost:8080/api/students `
  -H "Content-Type: application/json" `
  -d '{\"name\":\"Maria Lopez\",\"email\":\"maria@test.com\"}'

# Listar estudiantes
curl http://localhost:8080/api/students

# Obtener estudiante específico
curl http://localhost:8080/api/students/1
```

### 2. Verificar Métricas en Prometheus

1. Abrir http://localhost:9090/graph
2. Ejecutar query: `http_requests_total{job="spring-boot-app"}`
3. Deberías ver el contador incrementarse con cada petición

### 3. Verificar Logs en Grafana

1. Abrir el dashboard en Grafana
2. Scroll hasta el panel "Application Logs"
3. Deberías ver logs de las peticiones HTTP recién realizadas

### 4. Probar Circuit Breaker

```powershell
# El Circuit Breaker se activa después de varios fallos consecutivos
# Verificar estado en Grafana panel "Circuit Breaker State"
```

## 📊 Validación de Criterios de Evaluación

### IE1 - Herramientas de Monitoreo (20%)

✅ **Prometheus configurado:**
- Scraping cada 10s del endpoint `/actuator/prometheus`
- Retention de 7 días
- Métricas JVM, HTTP, Circuit Breaker, Pipeline

✅ **Grafana configurado:**
- Datasources automáticos (Prometheus + Loki)
- Dashboard completo con 10 paneles
- Auto-refresh cada 10s

✅ **Loki + Promtail configurados:**
- Agregación de logs centralizada
- Retention de 7 días
- Parsing de logs estructurados

### IE3 - Generación de Dashboards (10%)

✅ **Dashboard "Spring Boot - DevOps Observability Dashboard":**
- 10 paneles de visualización
- Métricas de infraestructura (CPU, memoria)
- Métricas de aplicación (HTTP, JVM)
- Métricas de resiliencia (Circuit Breaker)
- Métricas de pipeline (builds, coverage, quality gate)
- Logs en tiempo real

## 🛑 Detener el Stack

```powershell
docker-compose down
```

Para eliminar también los volúmenes (datos persistentes):

```powershell
docker-compose down -v
```

## 🐛 Troubleshooting

### Problema: Contenedor "app" no está healthy

**Solución:**
```powershell
# Ver logs del contenedor
docker-compose logs app

# Verificar health check
docker inspect app | Select-String -Pattern "Health"
```

### Problema: Prometheus no muestra targets

**Solución:**
1. Verificar que el contenedor "app" esté en la red "observability"
2. Verificar conectividad:
```powershell
docker-compose exec prometheus wget -O- http://app:8080/actuator/prometheus
```

### Problema: Grafana no muestra métricas

**Solución:**
1. Verificar datasource en Grafana: Connections → Data Sources → Prometheus → Save & test
2. Verificar que Prometheus esté scrapeando: http://localhost:9090/targets
3. Ejecutar query manual en Grafana Explore

### Problema: No aparecen logs en Loki

**Solución:**
1. Verificar logs de Promtail:
```powershell
docker-compose logs promtail
```

2. Verificar que la aplicación esté escribiendo logs:
```powershell
docker-compose exec app ls -la /var/log/app/
```

3. Verificar conectividad Promtail → Loki:
```powershell
docker-compose exec promtail wget -O- http://loki:3100/ready
```

## 📁 Archivos de Configuración Creados

```
.
├── docker-compose.yml              # Orquestación de 5 servicios
├── prometheus/
│   └── prometheus.yml              # Configuración de scraping
├── loki/
│   └── loki-config.yml             # Configuración de log aggregation
├── promtail/
│   └── promtail-config.yml         # Configuración de log collection
└── grafana/
    ├── provisioning/
    │   ├── datasources/
    │   │   └── datasources.yml     # Auto-config Prometheus + Loki
    │   └── dashboards/
    │       └── dashboards.yml      # Auto-provisioning de dashboards
    └── dashboards/
        └── spring-boot-dashboard.json  # Dashboard completo
```

## ✅ Checklist de Validación

- [ ] Todos los contenedores están "Up"
- [ ] Aplicación responde en http://localhost:8080/actuator/health
- [ ] Prometheus muestra target "spring-boot-app" en UP
- [ ] Grafana accesible con credenciales admin/admin123
- [ ] Datasources Prometheus y Loki configurados correctamente
- [ ] Dashboard "Spring Boot - DevOps Observability Dashboard" visible
- [ ] Todos los paneles del dashboard muestran datos
- [ ] Logs aparecen en el panel "Application Logs"
- [ ] Métricas de CPU y memoria se actualizan
- [ ] HTTP Request Rate incrementa al generar tráfico

## 📖 Siguiente Fase

Una vez validada la Fase 2, continuar con:

**FASE 3: Despliegue en Kubernetes**
- Crear manifiestos de deployment
- Configurar servicios y configmaps
- Implementar ingress
- Configurar ServiceMonitor para Prometheus Operator
