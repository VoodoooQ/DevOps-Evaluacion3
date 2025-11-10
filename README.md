# 🚀 Spring Boot - Pipeline CI/CD DevOps

API REST con **Spring Boot 3.3.7** y **Java 17** para gestión de estudiantes. Pipeline CI/CD completo con GitHub Actions, análisis de seguridad (Snyk), pruebas automatizadas (JUnit + JaCoCo) y despliegue con Docker Compose.

**Stack**: Java 17 | Spring Boot 3.3.7 | H2 Database | Maven | Docker | GitHub Actions | Snyk

---

## ⚡ Inicio Rápido

```bash
# Docker Compose (Recomendado)
docker-compose up -d
docker-compose logs -f springboot-app

# Maven (Desarrollo)
mvn clean test              # Ejecutar tests
mvn spring-boot:run         # Ejecutar aplicación
```

### 🌐 Acceso

- **API REST**: `http://localhost:8080/students`
  - `GET /students` - Listar todos
  - `POST /students` - Crear nuevo
  - `GET /students/{id}` - Obtener por ID
  - `PUT /students/{id}` - Actualizar
  - `DELETE /students/{id}` - Eliminar

- **Consola H2**: `http://localhost:8080/h2-console/`
  - **JDBC URL**: `jdbc:h2:mem:testdb`
  - **Usuario**: `SA`
  - **Password**: _(vacío)_

- **Health Check**: El contenedor tiene healthcheck automático en `/students`

---

## 📊 Pipeline CI/CD - Quality Gates

El pipeline ejecuta **5 etapas secuenciales** con **quality gates bloqueantes**:

| # | Etapa | Herramienta | Bloquea Pipeline | Genera Artifact |
|---|-------|-------------|------------------|-----------------|
| 1️⃣ | **Tests Unitarios** | JUnit + JaCoCo | ✅ Si falla cualquier test | `test-results`, `coverage-report` |
| 2️⃣ | **Seguridad** | Snyk | ✅ Si hay vulnerabilidades HIGH/CRITICAL | `snyk-security-report` |
| 3️⃣ | **Build Docker** | Docker multi-stage | ✅ Si falla el build | Imagen en Docker Hub |
| 4️⃣ | **Deploy Staging** | Docker Compose | ✅ Si falla el despliegue | Logs del deployment |
| 5️⃣ | **Notificaciones** | GitHub Actions | - | Estado del pipeline |

### 🛡️ Quality Gate de Seguridad (CRÍTICO)

**El pipeline se BLOQUEA automáticamente si Snyk detecta:**
- ❌ Vulnerabilidades **CRITICAL**
- ❌ Vulnerabilidades **HIGH**

**Proceso de validación:**
1. Snyk escanea todas las dependencias Maven
2. Genera `snyk-report.json` (siempre disponible como artifact)
3. Script analiza el JSON y cuenta vulnerabilidades por severidad
4. Si `CRITICAL > 0` o `HIGH > 0` → **exit 1** (pipeline falla)
5. Solo continúa si pasa el umbral de seguridad

**Para revisar vulnerabilidades:**
```bash
# En GitHub Actions
Actions → Workflow run → Artifacts → snyk-security-report
```

### ✅ Quality Gate de Tests

**El pipeline se BLOQUEA si:**
- ❌ Algún test unitario falla
- ❌ Cobertura de código < 50% (JaCoCo)

**Artifacts generados:**
- `test-results`: Reportes de Surefire (XML)
- `coverage-report`: Reporte HTML de JaCoCo

---

## 🔐 Configuración de Seguridad

### 1. Secrets de GitHub (Requeridos)

`Settings → Secrets and variables → Actions → New repository secret`:

| Secret | Descripción | Ejemplo |
|--------|-------------|---------|
| `DOCKER_USERNAME` | Usuario de Docker Hub | `tu-usuario` |
| `DOCKER_PASSWORD` | Token de Docker Hub | `dckr_pat_xxxxx` |
| `SNYK_TOKEN` | Token de Snyk | `xxxxx-xxxx-xxxx` |

### 2. Branch Protection Rules (OBLIGATORIO para producción)

`Settings → Branches → Add rule`:

```
Branch name pattern: main

☑ Require a pull request before merging
  ☑ Require approvals (1)
  
☑ Require status checks to pass before merging
  ☑ Require branches to be up to date before merging
  Status checks:
    - Pruebas Unitarias
    - Análisis de Seguridad
    - Construcción de Imagen Docker
    
☑ Do not allow bypassing the above settings
```

**Esto garantiza que:**
- ✅ Ningún código puede mergearse al `main` sin pasar los tests
- ✅ Ningún código vulnerable (HIGH/CRITICAL) puede llegar a producción
- ✅ Todo cambio requiere revisión de código (PR)

---

## 🐳 Docker Compose

### Ejecutar

```bash
# Levantar servicios
docker-compose up -d

# Ver logs en tiempo real
docker-compose logs -f springboot-app

# Verificar estado de salud
docker-compose ps

# Detener servicios
docker-compose down
```

### Healthcheck

El servicio principal tiene **healthcheck automático**:
- **Endpoint**: `GET http://localhost:8080/students`
- **Intervalo**: Cada 30 segundos
- **Timeout**: 10 segundos
- **Reintentos**: 3 intentos
- **Start period**: 60 segundos (tiempo para que Spring Boot inicie)

**Verificar salud del contenedor:**
```bash
docker inspect springboot-app-container | grep -A 10 Health
```

### Persistencia de Datos (Opcional)

Por defecto usa **H2 en memoria** (`jdbc:h2:mem:testdb`).

**Para habilitar persistencia:**

1. Editar `docker-compose.yml`:
```yaml
environment:
  - SPRING_DATASOURCE_URL=jdbc:h2:file:/data/testdb  # Cambiar a file
  
volumes:
  - app-data:/data  # Descomentar esta línea
```

2. Reiniciar:
```bash
docker-compose down
docker-compose up -d
```

---

## 📦 Artifacts del Pipeline

Cada ejecución del pipeline genera artifacts descargables:

| Artifact | Contenido | Cuándo revisarlo |
|----------|-----------|------------------|
| `test-results` | XML con resultados de tests (Surefire) | Si fallan tests |
| `coverage-report` | HTML con cobertura de código (JaCoCo) | Ver cobertura detallada |
| `snyk-security-report` | JSON con vulnerabilidades encontradas | Si falla análisis de seguridad |

**Acceder a artifacts:**
```
GitHub → Actions → [Workflow run] → Artifacts (abajo)
```

---

## 🔍 Guía para el Evaluador

### 1. Verificar Pipeline CI/CD

✅ **Ir a**: `https://github.com/VoodoooQ/Evaluacion2-DevOps/actions`

**Revisar:**
- ✅ Todos los jobs ejecutan en secuencia
- ✅ Job "Análisis de Seguridad" tiene paso "Validar vulnerabilidades (QUALITY GATE)"
- ✅ Pipeline falla si hay vulnerabilidades graves (buscar runs fallidos con ❌)
- ✅ Artifacts disponibles en cada run

### 2. Verificar Dependabot

✅ **Ir a**: `Security → Dependabot`

**Revisar:**
- ✅ Configuración activa para Maven y Docker
- ✅ Chequeo semanal configurado
- ✅ Pull requests automáticos para actualizaciones

### 3. Probar Localmente con Docker Compose

```bash
# Clonar repositorio
git clone https://github.com/VoodoooQ/Evaluacion2-DevOps.git
cd Evaluacion2-DevOps

# Levantar con Docker Compose
docker-compose up -d

# Verificar healthcheck (esperar ~60s)
docker-compose ps
# Debe mostrar: "healthy" en STATUS

# Probar API
curl http://localhost:8080/students

# Ver consola H2
# Browser: http://localhost:8080/h2-console/
# JDBC URL: jdbc:h2:mem:testdb
# User: SA, Password: (vacío)

# Ver logs
docker-compose logs -f springboot-app

# Detener
docker-compose down
```

### 4. Verificar Reportes de Seguridad

✅ **Snyk Report**:
1. Ir a cualquier workflow run exitoso
2. Descargar artifact `snyk-security-report`
3. Abrir `snyk-report.json`
4. Revisar campo `vulnerabilities[]`

### 5. Verificar Cobertura de Tests

✅ **Coverage Report**:
1. Descargar artifact `coverage-report`
2. Abrir `index.html` en navegador
3. Verificar cobertura ≥ 50%

---

## �️ Estructura del Proyecto

```
.
├── .github/
│   ├── workflows/
│   │   └── ci-cd-pipeline.yml      # Pipeline completo con quality gates
│   └── dependabot.yml               # Actualización automática de deps
├── src/
│   ├── main/java/com/example/bdget/
│   │   ├── BdgetApplication.java
│   │   ├── controller/
│   │   ├── model/
│   │   ├── repository/
│   │   └── service/
│   └── test/                        # Tests unitarios (JUnit + Mockito)
├── Dockerfile                       # Multi-stage build optimizado
├── docker-compose.yml               # Orquestación con healthcheck
├── pom.xml                          # Dependencias Maven + JaCoCo
└── README.md
```

---

## 👥 Autor

**Maximiliano Andres Diaz Caro**
