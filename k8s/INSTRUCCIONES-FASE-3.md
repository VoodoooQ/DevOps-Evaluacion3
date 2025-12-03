# FASE 3: ORQUESTACIÓN CON KUBERNETES

## IE2: Configuración de orquestación con Kubernetes (20%)

---

## 📋 ARCHIVOS GENERADOS

### Manifiestos principales:
1. **`namespace.yaml`** - Namespace dedicado con quotas y límites
2. **`deployment.yaml`** - Deployment con 3 réplicas, health checks y resource limits
3. **`service.yaml`** - ClusterIP Service + Headless Service
4. **`configmap.yaml`** - Configuración externalizada de la aplicación
5. **`ingress.yaml`** - Ingress para exposición HTTP
6. **`hpa.yaml`** - Horizontal Pod Autoscaler (auto-escalado)

---

## 🚀 DESPLIEGUE LOCAL CON MINIKUBE

### 1️⃣ Instalar Minikube (si no está instalado)

**Windows (PowerShell como Administrador):**
```powershell
# Descargar Minikube
New-Item -Path 'c:\minikube' -ItemType Directory -Force
Invoke-WebRequest -OutFile 'c:\minikube\minikube.exe' -Uri 'https://github.com/kubernetes/minikube/releases/latest/download/minikube-windows-amd64.exe' -UseBasicParsing

# Agregar al PATH
$oldPath = [Environment]::GetEnvironmentVariable('Path', [EnvironmentVariableTarget]::Machine)
if ($oldPath -notlike "*c:\minikube*") {
    [Environment]::SetEnvironmentVariable('Path', "$oldPath;c:\minikube", [EnvironmentVariableTarget]::Machine)
}

# Reiniciar terminal y verificar
minikube version
```

### 2️⃣ Iniciar Minikube

```powershell
# Iniciar con driver Docker
minikube start --driver=docker --cpus=4 --memory=4096

# Verificar estado
minikube status

# Habilitar addons necesarios
minikube addons enable ingress
minikube addons enable metrics-server
```

### 3️⃣ Cargar la imagen Docker en Minikube

```powershell
# Cargar imagen local a Minikube
minikube image load evaluacion2-devops-app:latest

# Verificar imagen
minikube ssh "docker images | grep evaluacion2"
```

### 4️⃣ Aplicar manifiestos (OPCIÓN 1: Namespace default)

```powershell
cd C:\Users\MAAXXDC\Downloads\Evaluacion2-DevOps

# Aplicar en orden
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml
kubectl apply -f k8s/hpa.yaml
```

### 4️⃣ ALTERNATIVA - Aplicar manifiestos (OPCIÓN 2: Namespace dedicado)

```powershell
# Crear namespace y aplicar manifiestos
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml -n bdget-app
kubectl apply -f k8s/deployment.yaml -n bdget-app
kubectl apply -f k8s/service.yaml -n bdget-app
kubectl apply -f k8s/ingress.yaml -n bdget-app
kubectl apply -f k8s/hpa.yaml -n bdget-app
```

---

## 🔍 VERIFICACIÓN DEL DEPLOYMENT

### Ver estado de los pods:
```powershell
# Namespace default
kubectl get pods -l app=bdget -w

# Namespace dedicado
kubectl get pods -n bdget-app -l app=bdget -w
```

**Resultado esperado:**
```
NAME                        READY   STATUS    RESTARTS   AGE
bdget-app-xxxxxxxxx-xxxxx   1/1     Running   0          30s
bdget-app-xxxxxxxxx-xxxxx   1/1     Running   0          30s
bdget-app-xxxxxxxxx-xxxxx   1/1     Running   0          30s
```

### Ver logs de un pod:
```powershell
# Obtener nombre del pod
kubectl get pods -l app=bdget

# Ver logs (reemplazar <pod-name>)
kubectl logs <pod-name> --follow
```

### Ver servicios:
```powershell
kubectl get svc -l app=bdget
```

**Resultado esperado:**
```
NAME              TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)    AGE
bdget-service     ClusterIP   10.96.xxx.xxx   <none>        80/TCP     1m
bdget-headless    ClusterIP   None            <none>        8080/TCP   1m
```

### Ver ingress:
```powershell
kubectl get ingress
```

### Ver HPA (Horizontal Pod Autoscaler):
```powershell
kubectl get hpa
```

**Resultado esperado:**
```
NAME        REFERENCE              TARGETS         MINPODS   MAXPODS   REPLICAS   AGE
bdget-hpa   Deployment/bdget-app   0%/70%, 0%/80%  2         10        3          1m
```

---

## 🌐 ACCEDER A LA APLICACIÓN

### Opción 1: Port Forward (más simple)
```powershell
# Forward del service al localhost
kubectl port-forward svc/bdget-service 8080:80

# Acceder en navegador o curl
curl http://localhost:8080/students
curl http://localhost:8080/actuator/health
```

### Opción 2: Minikube Tunnel (recomendado para Ingress)
```powershell
# Abrir túnel (en terminal separada, mantener abierta)
minikube tunnel

# Obtener IP del Ingress
kubectl get ingress

# Agregar a C:\Windows\System32\drivers\etc\hosts (como Administrador):
# <INGRESS-IP>  bdget.local

# Acceder en navegador
curl http://bdget.local/students
curl http://bdget.local/actuator/health
```

### Opción 3: Minikube Service (automático)
```powershell
# Abrir service en navegador automáticamente
minikube service bdget-service

# O obtener URL
minikube service bdget-service --url
```

---

## 📸 CAPTURAS DE PANTALLA PARA EVIDENCIA

### **CAPTURA 1: Pods corriendo**
```powershell
kubectl get pods -o wide
```
📸 Captura mostrando 3 pods en estado `Running`

### **CAPTURA 2: Deployment y ReplicaSet**
```powershell
kubectl get deployments,replicasets
```
📸 Captura mostrando deployment con 3/3 réplicas disponibles

### **CAPTURA 3: Services**
```powershell
kubectl get svc
kubectl describe svc bdget-service
```
📸 Captura mostrando services configurados

### **CAPTURA 4: Ingress**
```powershell
kubectl get ingress
kubectl describe ingress bdget-ingress
```
📸 Captura mostrando ingress con rules configuradas

### **CAPTURA 5: HPA (Autoscaler)**
```powershell
kubectl get hpa
kubectl describe hpa bdget-hpa
```
📸 Captura mostrando autoscaler configurado

### **CAPTURA 6: ConfigMap**
```powershell
kubectl get configmap bdget-config
kubectl describe configmap bdget-config
```
📸 Captura mostrando configuración externalizada

### **CAPTURA 7: Logs de pod**
```powershell
kubectl logs <pod-name> --tail=50
```
📸 Captura mostrando logs de Spring Boot iniciando

### **CAPTURA 8: Health Check desde pod**
```powershell
kubectl exec -it <pod-name> -- wget -qO- http://localhost:8080/actuator/health
```
📸 Captura mostrando health check respondiendo

### **CAPTURA 9: API GET /students**
```powershell
# Con port-forward activo
curl http://localhost:8080/students | ConvertFrom-Json | ConvertTo-Json -Depth 5
```
📸 Captura mostrando lista de estudiantes

### **CAPTURA 10: Métricas de Prometheus**
```powershell
curl http://localhost:8080/actuator/prometheus | Select-String "application_"
```
📸 Captura mostrando métricas exportadas

### **CAPTURA 11: Dashboard de Minikube**
```powershell
minikube dashboard
```
📸 Captura del dashboard web de Kubernetes

### **CAPTURA 12: Eventos del deployment**
```powershell
kubectl get events --sort-by='.lastTimestamp' | Select-Object -First 20
```
📸 Captura mostrando eventos de creación de recursos

---

## 🧪 PRUEBAS DE AUTO-ESCALADO

### Generar carga para probar HPA:
```powershell
# Generar tráfico intenso
for ($i=1; $i -le 1000; $i++) { 
    curl http://localhost:8080/students -UseBasicParsing | Out-Null
    Write-Host "Request $i" -NoNewline -ForegroundColor Green
    Write-Host "`r" -NoNewline
}

# En otra terminal, observar el escalado
kubectl get hpa -w
kubectl get pods -l app=bdget -w
```

**Resultado esperado:** HPA aumentará el número de réplicas cuando CPU > 70%

---

## 🧹 LIMPIEZA

### Eliminar recursos:
```powershell
# Namespace default
kubectl delete -f k8s/hpa.yaml
kubectl delete -f k8s/ingress.yaml
kubectl delete -f k8s/service.yaml
kubectl delete -f k8s/deployment.yaml
kubectl delete -f k8s/configmap.yaml

# O namespace dedicado
kubectl delete namespace bdget-app
```

### Detener Minikube:
```powershell
minikube stop
```

### Eliminar cluster:
```powershell
minikube delete
```

---

## ✅ CHECKLIST - IE2 (20%)

- [ ] ✅ Namespace creado con ResourceQuota y LimitRange
- [ ] ✅ Deployment con 3 réplicas configurado
- [ ] ✅ Health checks (liveness, readiness, startup) funcionando
- [ ] ✅ Resource limits y requests configurados
- [ ] ✅ Service ClusterIP creado
- [ ] ✅ ConfigMap con configuración externalizada
- [ ] ✅ Ingress para exposición HTTP
- [ ] ✅ HPA para auto-escalado configurado
- [ ] ✅ Pods en estado Running
- [ ] ✅ API accesible desde fuera del cluster
- [ ] ✅ 12 capturas de pantalla documentadas

---

## 🔄 SIGUIENTES PASOS

Una vez completada la Fase 3, continuar con:
- **FASE 4**: CI/CD con GitHub Actions
- **FASE 5**: Documentación completa y README

---

## 📚 RECURSOS ADICIONALES

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Minikube Documentation](https://minikube.sigs.k8s.io/docs/)
- [Spring Boot on Kubernetes](https://spring.io/guides/gs/spring-boot-kubernetes/)
- [Horizontal Pod Autoscaler](https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/)
