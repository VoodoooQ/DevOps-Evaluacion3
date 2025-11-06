# 🚀 Spring Boot - Pipeline CI/CD

API REST con **Spring Boot 3.3.7** y **Java 17** para gestión de estudiantes. Pipeline CI/CD completo con GitHub Actions, análisis de seguridad (Snyk), pruebas automatizadas (JUnit + JaCoCo) y despliegue con Docker Compose.

**Stack**: Java 17 | Spring Boot 3.3.7 | H2 Database | Maven | Docker | GitHub Actions

## ⚡ Inicio Rápido

```bash
# Docker Compose (Recomendado)
docker-compose up -d

# Maven
mvn spring-boot:run
```

**Endpoints**: 
- API: `http://localhost:8080/students` (GET, POST, PUT, DELETE)
- H2 Console: `http://localhost:8080/h2-console/` (JDBC: `jdbc:h2:mem:testdb`, User: `SA`)


## 📊 Pipeline CI/CD

**5 Etapas Automáticas**: Tests → Seguridad (Snyk) → Build Docker → Deploy (Docker Compose) → Notificaciones

| Etapa | Acción | Bloquea |
|-------|--------|---------|
| **Tests** | JUnit + JaCoCo (≥50% coverage) | ✅ |
| **Seguridad** | Snyk (CVEs HIGH/CRITICAL) | ✅ |
| **Build** | Docker multi-stage → Docker Hub | ✅ |
| **Deploy** | Docker Compose + smoke tests | ✅ |
| **Notify** | Estado del pipeline | ✅ |

**Trazabilidad**: Cada imagen tiene tag `sha-{commit}` para rastrear exactamente qué código está desplegado.

## ⚙️ Configuración GitHub

### Secrets Requeridos
`Settings → Secrets and variables → Actions`:
- `DOCKER_USERNAME`: Usuario de Docker Hub
- `DOCKER_PASSWORD`: Token de Docker Hub
- `SNYK_TOKEN`: Token de Snyk

### Subir Código
```bash
git init
git add .
git commit -m "feat: Implementación inicial"
git remote add origin https://github.com/TU-USUARIO/repo.git
git push -u origin main
```


## 📁 Estructura

```
├── .github/workflows/ci-cd-pipeline.yml    # Pipeline CI/CD
├── src/main/java/com/example/bdget/       # Código fuente
├── Dockerfile                              # Multi-stage build
├── docker-compose.yml                      # Orquestación
└── pom.xml                                 # Dependencias Maven
```

---

## 👥 Autor

**Maximiliano Andres Diaz Caro** | 
