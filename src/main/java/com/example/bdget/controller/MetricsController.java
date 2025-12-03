package com.example.bdget.controller;

import com.example.bdget.config.MetricsConfig;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * METRICS CONTROLLER
 * 
 * Este controlador expone endpoints para consultar y manipular métricas
 * personalizadas de la aplicación. Las métricas se exportan a Prometheus
 * y se visualizan en Grafana.
 * 
 * Endpoints:
 * - GET /metrics/custom: Obtener resumen de métricas personalizadas
 * - POST /metrics/simulate/cpu: Simular cambio en uso de CPU
 * - POST /metrics/simulate/memory: Simular cambio en uso de memoria
 * - POST /metrics/simulate/error: Generar un error y incrementar contador
 * - POST /metrics/simulate/traffic: Simular tráfico HTTP
 * 
 * Estos endpoints son útiles para:
 * - IE3: Dashboard con métricas clave
 * - Pruebas de escalado automático (HPA)
 * - Pruebas de alertas en Prometheus
 * - Demostración de observabilidad
 * 
 * @author Evaluación DevOps
 * @version 1.0
 */
@RestController
@RequestMapping("/metrics")
public class MetricsController {

    private static final Logger logger = LoggerFactory.getLogger(MetricsController.class);

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private MetricsConfig metricsConfig;

    @Autowired
    private Counter httpRequestCounter;

    @Autowired
    private Counter errorCounter;

    private final Random random = new Random();

    /**
     * Obtener resumen de métricas personalizadas
     * 
     * Endpoint: GET /metrics/custom
     * 
     * Retorna un resumen de las métricas actuales de la aplicación:
     * - Total de requests HTTP
     * - Total de errores
     * - Conexiones activas
     * - Uso de CPU (simulado)
     * - Uso de memoria (simulado)
     * 
     * @return ResponseEntity con las métricas
     */
    @GetMapping("/custom")
    public ResponseEntity<Map<String, Object>> getCustomMetrics() {
        logger.info("Consultando métricas personalizadas");

        Map<String, Object> metrics = new HashMap<>();

        // Obtener contador de requests HTTP
        double httpRequests = httpRequestCounter.count();
        
        // Obtener contador de errores
        double errors = errorCounter.count();
        
        // Obtener conexiones activas
        int activeConnections = metricsConfig.getActiveConnections().get();
        
        // Obtener uso de CPU y memoria (simulados)
        int cpuUsage = metricsConfig.getCpuUsage().get();
        int memoryUsage = metricsConfig.getMemoryUsage().get();

        metrics.put("httpRequestsTotal", httpRequests);
        metrics.put("errorsTotal", errors);
        metrics.put("activeConnections", activeConnections);
        metrics.put("cpuUsagePercent", cpuUsage);
        metrics.put("memoryUsagePercent", memoryUsage);
        metrics.put("timestamp", System.currentTimeMillis());

        logger.debug("Métricas: Requests={}, Errors={}, CPU={}%, Memory={}%", 
            httpRequests, errors, cpuUsage, memoryUsage);

        return ResponseEntity.ok(metrics);
    }

    /**
     * Simular cambio en uso de CPU
     * 
     * Endpoint: POST /metrics/simulate/cpu?value=75
     * 
     * Actualiza el gauge de uso de CPU con el valor especificado.
     * Útil para probar alertas de alta CPU y escalado automático.
     * 
     * @param value Valor de CPU (0-100)
     * @return ResponseEntity con el nuevo valor
     */
    @PostMapping("/simulate/cpu")
    public ResponseEntity<Map<String, Object>> simulateCpuUsage(@RequestParam(defaultValue = "50") int value) {
        // Validar rango
        if (value < 0 || value > 100) {
            logger.warn("Valor de CPU inválido: {}", value);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "El valor debe estar entre 0 y 100",
                "providedValue", value
            ));
        }

        metricsConfig.updateCpuUsage(value);
        logger.info("Uso de CPU simulado actualizado a {}%", value);

        return ResponseEntity.ok(Map.of(
            "message", "Uso de CPU actualizado",
            "cpuUsagePercent", value,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Simular cambio en uso de memoria
     * 
     * Endpoint: POST /metrics/simulate/memory?value=80
     * 
     * Actualiza el gauge de uso de memoria con el valor especificado.
     * Útil para probar alertas de alta memoria y escalado automático.
     * 
     * @param value Valor de memoria (0-100)
     * @return ResponseEntity con el nuevo valor
     */
    @PostMapping("/simulate/memory")
    public ResponseEntity<Map<String, Object>> simulateMemoryUsage(@RequestParam(defaultValue = "60") int value) {
        // Validar rango
        if (value < 0 || value > 100) {
            logger.warn("Valor de memoria inválido: {}", value);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "El valor debe estar entre 0 y 100",
                "providedValue", value
            ));
        }

        metricsConfig.updateMemoryUsage(value);
        logger.info("Uso de memoria simulado actualizado a {}%", value);

        return ResponseEntity.ok(Map.of(
            "message", "Uso de memoria actualizado",
            "memoryUsagePercent", value,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Simular un error en la aplicación
     * 
     * Endpoint: POST /metrics/simulate/error
     * 
     * Incrementa el contador de errores y registra un error en los logs.
     * Útil para probar alertas de tasa de errores alta.
     * 
     * @return ResponseEntity con información del error simulado
     */
    @PostMapping("/simulate/error")
    public ResponseEntity<Map<String, Object>> simulateError() {
        // Incrementar contador de errores
        errorCounter.increment();
        
        String errorMessage = "Error simulado para pruebas de observabilidad";
        logger.error(errorMessage);

        double totalErrors = errorCounter.count();

        return ResponseEntity.ok(Map.of(
            "message", "Error simulado generado",
            "errorMessage", errorMessage,
            "totalErrors", totalErrors,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Simular tráfico HTTP
     * 
     * Endpoint: POST /metrics/simulate/traffic?requests=100
     * 
     * Incrementa el contador de requests HTTP el número especificado de veces.
     * También simula cambios en conexiones activas.
     * 
     * Útil para:
     * - Probar métricas de tráfico
     * - Generar datos para dashboards
     * - Simular carga para HPA
     * 
     * @param requests Número de requests a simular
     * @return ResponseEntity con el resultado
     */
    @PostMapping("/simulate/traffic")
    public ResponseEntity<Map<String, Object>> simulateTraffic(@RequestParam(defaultValue = "10") int requests) {
        if (requests < 1 || requests > 1000) {
            logger.warn("Número de requests inválido: {}", requests);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "El número de requests debe estar entre 1 y 1000",
                "providedValue", requests
            ));
        }

        // Incrementar contador de requests
        for (int i = 0; i < requests; i++) {
            httpRequestCounter.increment();
        }

        // Simular cambio en conexiones activas
        int newConnections = random.nextInt(50) + 10;
        for (int i = 0; i < newConnections; i++) {
            metricsConfig.incrementActiveConnections();
        }

        double totalRequests = httpRequestCounter.count();
        int activeConnections = metricsConfig.getActiveConnections().get();

        logger.info("Tráfico simulado: {} requests generados, {} conexiones activas", 
            requests, activeConnections);

        return ResponseEntity.ok(Map.of(
            "message", "Tráfico HTTP simulado",
            "requestsGenerated", requests,
            "totalRequests", totalRequests,
            "activeConnections", activeConnections,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Resetear conexiones activas
     * 
     * Endpoint: POST /metrics/reset/connections
     * 
     * Resetea el contador de conexiones activas a 0.
     * 
     * @return ResponseEntity con confirmación
     */
    @PostMapping("/reset/connections")
    public ResponseEntity<Map<String, String>> resetConnections() {
        int currentConnections = metricsConfig.getActiveConnections().get();
        
        // Decrementar todas las conexiones
        for (int i = 0; i < currentConnections; i++) {
            metricsConfig.decrementActiveConnections();
        }

        logger.info("Conexiones activas reseteadas (antes: {})", currentConnections);

        return ResponseEntity.ok(Map.of(
            "message", "Conexiones activas reseteadas",
            "previousConnections", String.valueOf(currentConnections),
            "currentConnections", "0"
        ));
    }

    /**
     * Obtener información de todos los meters registrados
     * 
     * Endpoint: GET /metrics/registry
     * 
     * Retorna información sobre todos los meters registrados en Micrometer.
     * Útil para debugging y verificación de métricas.
     * 
     * @return ResponseEntity con información del registry
     */
    @GetMapping("/registry")
    public ResponseEntity<Map<String, Object>> getRegistryInfo() {
        Map<String, Object> info = new HashMap<>();
        
        int totalMeters = meterRegistry.getMeters().size();
        
        info.put("totalMeters", totalMeters);
        info.put("metricsEndpoint", "/actuator/prometheus");
        info.put("message", "Consulta /actuator/prometheus para ver todas las métricas en formato Prometheus");

        return ResponseEntity.ok(info);
    }
}
