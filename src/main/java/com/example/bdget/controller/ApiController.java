package com.example.bdget.controller;

import com.example.bdget.service.ResilientService;
import io.micrometer.core.instrument.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * API CONTROLLER
 * 
 * Controlador principal de la API REST del microservicio.
 * Expone endpoints para demostrar funcionalidades y patrones de resiliencia.
 * 
 * Endpoints:
 * - GET /api/test: Endpoint de prueba simple
 * - GET /api/hello: Saludo personalizado
 * - GET /api/resilient: Endpoint protegido con Circuit Breaker
 * - POST /api/echo: Echo de datos enviados
 * - GET /api/info: Información de la aplicación
 * 
 * Características:
 * - Logging estructurado para Loki
 * - Métricas automáticas de requests (Micrometer)
 * - Manejo de errores
 * - Integración con Circuit Breaker
 * 
 * @author Evaluación DevOps
 * @version 1.0
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    private Counter httpRequestCounter;

    @Autowired
    private Counter errorCounter;

    @Autowired
    private ResilientService resilientService;

    /**
     * Endpoint de prueba simple
     * 
     * Endpoint: GET /api/test
     * 
     * Endpoint básico para verificar que la API está funcionando.
     * Incrementa el contador de requests HTTP.
     * 
     * @return ResponseEntity con mensaje de prueba
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        logger.info("Endpoint /api/test invocado");
        
        // Incrementar contador de requests
        httpRequestCounter.increment();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "API funcionando correctamente");
        response.put("timestamp", System.currentTimeMillis());
        response.put("endpoint", "/api/test");

        return ResponseEntity.ok(response);
    }

    /**
     * Saludo personalizado
     * 
     * Endpoint: GET /api/hello?name=Juan
     * 
     * Retorna un saludo personalizado con el nombre proporcionado.
     * 
     * @param name Nombre de la persona (opcional)
     * @return ResponseEntity con saludo
     */
    @GetMapping("/hello")
    public ResponseEntity<Map<String, Object>> hello(@RequestParam(defaultValue = "Mundo") String name) {
        logger.info("Endpoint /api/hello invocado con name={}", name);
        
        // Incrementar contador de requests
        httpRequestCounter.increment();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", String.format("Hola, %s!", name));
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint protegido con Circuit Breaker
     * 
     * Endpoint: GET /api/resilient?shouldFail=false
     * 
     * Este endpoint demuestra el patrón Circuit Breaker implementado
     * con Resilience4j. Permite simular fallos para probar la resiliencia.
     * 
     * @param shouldFail Si es true, fuerza un fallo para probar Circuit Breaker
     * @return ResponseEntity con resultado de la operación
     */
    @GetMapping("/resilient")
    public ResponseEntity<Map<String, Object>> resilientEndpoint(
            @RequestParam(defaultValue = "false") boolean shouldFail) {
        
        logger.info("Endpoint /api/resilient invocado con shouldFail={}", shouldFail);
        
        // Incrementar contador de requests
        httpRequestCounter.increment();

        try {
            // Llamar al servicio resiliente
            String result = resilientService.executeResilientOperation(shouldFail);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", result);
            response.put("circuitBreakerUsed", true);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error en endpoint resilient: {}", e.getMessage());
            
            // Incrementar contador de errores
            errorCounter.increment();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Error en operación resiliente");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("circuitBreakerActivated", true);
            errorResponse.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        }
    }

    /**
     * Endpoint de echo
     * 
     * Endpoint: POST /api/echo
     * Body: { "message": "Hola mundo" }
     * 
     * Retorna el mismo mensaje que se envía en el body.
     * Útil para pruebas de integración.
     * 
     * @param payload Mapa con el mensaje
     * @return ResponseEntity con el echo del mensaje
     */
    @PostMapping("/echo")
    public ResponseEntity<Map<String, Object>> echo(@RequestBody Map<String, Object> payload) {
        logger.info("Endpoint /api/echo invocado con payload: {}", payload);
        
        // Incrementar contador de requests
        httpRequestCounter.increment();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("echo", payload);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    /**
     * Información de la aplicación
     * 
     * Endpoint: GET /api/info
     * 
     * Retorna información general sobre la aplicación:
     * - Nombre
     * - Versión
     * - Descripción
     * - Endpoints disponibles
     * 
     * @return ResponseEntity con información de la app
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        logger.info("Endpoint /api/info invocado");
        
        // Incrementar contador de requests
        httpRequestCounter.increment();

        Map<String, Object> response = new HashMap<>();
        response.put("name", "Microservicio Evaluación DevOps");
        response.put("version", "1.0.0");
        response.put("description", "Microservicio con observabilidad completa (Prometheus + Grafana + Loki)");
        response.put("javaVersion", System.getProperty("java.version"));
        
        // Lista de endpoints disponibles
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("test", "GET /api/test");
        endpoints.put("hello", "GET /api/hello?name=Juan");
        endpoints.put("resilient", "GET /api/resilient?shouldFail=false");
        endpoints.put("echo", "POST /api/echo");
        endpoints.put("info", "GET /api/info");
        endpoints.put("health", "GET /health/custom");
        endpoints.put("liveness", "GET /health/liveness");
        endpoints.put("readiness", "GET /health/readiness");
        endpoints.put("metrics", "GET /metrics/custom");
        endpoints.put("prometheus", "GET /actuator/prometheus");
        endpoints.put("actuator", "GET /actuator");
        
        response.put("endpoints", endpoints);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    /**
     * Manejador de excepciones global para este controlador
     * 
     * Captura cualquier excepción no manejada y retorna una respuesta
     * estructurada con el error.
     * 
     * @param e Excepción capturada
     * @return ResponseEntity con información del error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        logger.error("Excepción capturada en ApiController: {}", e.getMessage(), e);
        
        // Incrementar contador de errores
        errorCounter.increment();

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", "Error interno del servidor");
        errorResponse.put("error", e.getMessage());
        errorResponse.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
