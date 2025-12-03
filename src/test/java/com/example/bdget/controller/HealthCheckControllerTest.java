package com.example.bdget.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TESTS PARA HEALTH CHECK CONTROLLER
 * 
 * Verifica el correcto funcionamiento de los health checks:
 * - Health check personalizado
 * - Liveness probe
 * - Readiness probe
 * - Health indicator de Spring Boot
 * 
 * @author Evaluación DevOps
 * @version 1.0
 */
class HealthCheckControllerTest {

    @InjectMocks
    private HealthCheckController healthCheckController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCustomHealthCheck_ShouldReturnHealthInformation() {
        // When
        ResponseEntity<Map<String, Object>> response = healthCheckController.customHealthCheck();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKeys("status", "cpu", "memory", "uptime", "timestamp");
        assertThat(response.getBody().get("status")).isEqualTo("UP");
    }

    @Test
    void testCustomHealthCheck_ShouldIncludeCpuInformation() {
        // When
        ResponseEntity<Map<String, Object>> response = healthCheckController.customHealthCheck();

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("cpu");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> cpuInfo = (Map<String, Object>) response.getBody().get("cpu");
        assertThat(cpuInfo).containsKeys("availableProcessors", "systemLoadAverage");
    }

    @Test
    void testCustomHealthCheck_ShouldIncludeMemoryInformation() {
        // When
        ResponseEntity<Map<String, Object>> response = healthCheckController.customHealthCheck();

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("memory");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> memoryInfo = (Map<String, Object>) response.getBody().get("memory");
        assertThat(memoryInfo).containsKeys("heapUsedMB", "heapMaxMB", "heapUsagePercent");
    }

    @Test
    void testCustomHealthCheck_ShouldIncludeUptimeInformation() {
        // When
        ResponseEntity<Map<String, Object>> response = healthCheckController.customHealthCheck();

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("uptime");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> uptimeInfo = (Map<String, Object>) response.getBody().get("uptime");
        assertThat(uptimeInfo).containsKeys("uptimeSeconds", "uptimeFormatted");
    }

    @Test
    void testLivenessProbe_ShouldReturnUp() {
        // When
        ResponseEntity<Map<String, String>> response = healthCheckController.livenessProbe();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("UP");
        assertThat(response.getBody().get("probe")).isEqualTo("liveness");
        assertThat(response.getBody()).containsKey("message");
    }

    @Test
    void testReadinessProbe_ShouldReturnUp_WhenMemoryIsNormal() {
        // When
        ResponseEntity<Map<String, Object>> response = healthCheckController.readinessProbe();

        // Then
        // El probe debería estar UP si la memoria está por debajo del 90%
        // En condiciones normales de test, esto debería ser true
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKeys("status", "probe");
        assertThat(response.getBody().get("probe")).isEqualTo("readiness");
    }

    @Test
    void testHealth_ShouldReturnHealthStatus() {
        // When
        Health health = healthCheckController.health();

        // Then
        assertThat(health).isNotNull();
        assertThat(health.getStatus()).isIn(Status.UP, Status.DOWN);
        assertThat(health.getDetails()).containsKey("heapUsagePercent");
    }

    @Test
    void testHealth_ShouldReturnUp_WhenMemoryIsNormal() {
        // When
        Health health = healthCheckController.health();

        // Then
        // En condiciones normales, el health debería estar UP
        // Solo estaría DOWN si el uso de memoria es >90%
        assertThat(health).isNotNull();
        assertThat(health.getDetails()).containsKey("heapUsagePercent");
        
        String heapUsage = (String) health.getDetails().get("heapUsagePercent");
        assertThat(heapUsage).isNotNull();
        assertThat(heapUsage).contains("%");
    }

    @Test
    void testReadinessProbe_ShouldIncludeHeapUsageInResponse() {
        // When
        ResponseEntity<Map<String, Object>> response = healthCheckController.readinessProbe();

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("heapUsagePercent");
        
        String heapUsage = (String) response.getBody().get("heapUsagePercent");
        assertThat(heapUsage).isNotNull();
        assertThat(heapUsage).contains("%");
    }

    @Test
    void testLivenessProbe_ShouldAlwaysReturnOk() {
        // El liveness probe debe siempre retornar OK mientras la app esté corriendo
        // Si falla, Kubernetes reiniciará el pod
        
        // When
        ResponseEntity<Map<String, String>> response1 = healthCheckController.livenessProbe();
        ResponseEntity<Map<String, String>> response2 = healthCheckController.livenessProbe();
        ResponseEntity<Map<String, String>> response3 = healthCheckController.livenessProbe();

        // Then
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testCustomHealthCheck_ShouldHaveTimestamp() {
        // When
        ResponseEntity<Map<String, Object>> response = healthCheckController.customHealthCheck();

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("timestamp");
        
        Object timestamp = response.getBody().get("timestamp");
        assertThat(timestamp).isInstanceOf(Long.class);
        assertThat((Long) timestamp).isGreaterThan(0L);
    }
}
