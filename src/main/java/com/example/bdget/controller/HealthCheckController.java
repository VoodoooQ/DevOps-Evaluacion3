package com.example.bdget.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * HEALTH CHECK CONTROLLER
 * 
 * Este controlador expone endpoints personalizados para health checks,
 * complementando los endpoints de Spring Boot Actuator.
 * 
 * Endpoints:
 * - GET /health/custom: Health check personalizado con información detallada
 * - GET /health/liveness: Liveness probe para Kubernetes
 * - GET /health/readiness: Readiness probe para Kubernetes
 * 
 * Los health checks son esenciales para:
 * - IE6: Validación ante fallas (Kubernetes auto-healing)
 * - Detección de pods no saludables
 * - Recuperación automática de la aplicación
 * 
 * @author Evaluación DevOps
 * @version 1.0
 */
@RestController
@RequestMapping("/health")
public class HealthCheckController implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckController.class);

    /**
     * Health Check Personalizado
     * 
     * Endpoint: GET /health/custom
     * 
     * Retorna información detallada sobre el estado de la aplicación:
     * - Estado general (UP/DOWN)
     * - Uso de CPU
     * - Uso de memoria (heap)
     * - Uptime de la aplicación
     * - Número de procesadores disponibles
     * 
     * @return ResponseEntity con información de salud
     */
    @GetMapping("/custom")
    public ResponseEntity<Map<String, Object>> customHealthCheck() {
        logger.info("Ejecutando health check personalizado");
        
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Obtener información del sistema
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            
            // Estado general
            health.put("status", "UP");
            
            // Información de CPU
            int availableProcessors = osBean.getAvailableProcessors();
            double systemLoadAverage = osBean.getSystemLoadAverage();
            health.put("cpu", Map.of(
                "availableProcessors", availableProcessors,
                "systemLoadAverage", systemLoadAverage > 0 ? systemLoadAverage : "N/A"
            ));
            
            // Información de memoria
            long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
            long heapMax = memoryBean.getHeapMemoryUsage().getMax();
            double heapUsagePercent = (heapUsed * 100.0) / heapMax;
            
            health.put("memory", Map.of(
                "heapUsedMB", heapUsed / (1024 * 1024),
                "heapMaxMB", heapMax / (1024 * 1024),
                "heapUsagePercent", String.format("%.2f%%", heapUsagePercent)
            ));
            
            // Uptime de la aplicación
            long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
            long uptimeSeconds = uptimeMs / 1000;
            health.put("uptime", Map.of(
                "uptimeSeconds", uptimeSeconds,
                "uptimeFormatted", formatUptime(uptimeSeconds)
            ));
            
            // Timestamp del check
            health.put("timestamp", System.currentTimeMillis());
            
            logger.info("Health check exitoso - Heap: {}%, Load: {}", 
                String.format("%.2f", heapUsagePercent), systemLoadAverage);
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            logger.error("Error en health check: {}", e.getMessage(), e);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            return ResponseEntity.status(503).body(health);
        }
    }

    /**
     * Liveness Probe para Kubernetes
     * 
     * Endpoint: GET /health/liveness
     * 
     * Indica si la aplicación está "viva" y funcionando.
     * Si este endpoint falla repetidamente, Kubernetes reiniciará el pod.
     * 
     * Este probe es simple y solo verifica que la aplicación responda.
     * 
     * @return ResponseEntity con estado OK
     */
    @GetMapping("/liveness")
    public ResponseEntity<Map<String, String>> livenessProbe() {
        logger.debug("Liveness probe ejecutado");
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("probe", "liveness");
        response.put("message", "La aplicación está viva");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Readiness Probe para Kubernetes
     * 
     * Endpoint: GET /health/readiness
     * 
     * Indica si la aplicación está lista para recibir tráfico.
     * Si este endpoint falla, Kubernetes dejará de enviar tráfico al pod,
     * pero NO lo reiniciará.
     * 
     * Verifica:
     * - Uso de memoria no crítico (<90%)
     * - Aplicación inicializada correctamente
     * 
     * @return ResponseEntity con estado de readiness
     */
    @GetMapping("/readiness")
    public ResponseEntity<Map<String, Object>> readinessProbe() {
        logger.debug("Readiness probe ejecutado");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar uso de memoria
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
            long heapMax = memoryBean.getHeapMemoryUsage().getMax();
            double heapUsagePercent = (heapUsed * 100.0) / heapMax;
            
            // Si el uso de memoria es muy alto (>90%), no está listo
            if (heapUsagePercent > 90) {
                logger.warn("Readiness probe FAIL - Uso de memoria muy alto: {}%", 
                    String.format("%.2f", heapUsagePercent));
                response.put("status", "DOWN");
                response.put("probe", "readiness");
                response.put("reason", "Uso de memoria crítico");
                response.put("heapUsagePercent", String.format("%.2f%%", heapUsagePercent));
                return ResponseEntity.status(503).body(response);
            }
            
            // Aplicación lista
            response.put("status", "UP");
            response.put("probe", "readiness");
            response.put("message", "La aplicación está lista para recibir tráfico");
            response.put("heapUsagePercent", String.format("%.2f%%", heapUsagePercent));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error en readiness probe: {}", e.getMessage(), e);
            response.put("status", "DOWN");
            response.put("probe", "readiness");
            response.put("error", e.getMessage());
            return ResponseEntity.status(503).body(response);
        }
    }

    /**
     * Implementación de HealthIndicator de Spring Boot
     * 
     * Este método se ejecuta automáticamente cuando se consulta /actuator/health
     * 
     * @return Health con estado de la aplicación
     */
    @Override
    public Health health() {
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
            long heapMax = memoryBean.getHeapMemoryUsage().getMax();
            double heapUsagePercent = (heapUsed * 100.0) / heapMax;
            
            if (heapUsagePercent > 90) {
                return Health.down()
                    .withDetail("reason", "Uso de memoria crítico")
                    .withDetail("heapUsagePercent", String.format("%.2f%%", heapUsagePercent))
                    .build();
            }
            
            return Health.up()
                .withDetail("heapUsagePercent", String.format("%.2f%%", heapUsagePercent))
                .build();
                
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }

    /**
     * Método auxiliar para formatear el uptime
     * 
     * @param seconds Segundos de uptime
     * @return String formateado (Xd Xh Xm Xs)
     */
    private String formatUptime(long seconds) {
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        return String.format("%dd %dh %dm %ds", days, hours, minutes, secs);
    }
}
