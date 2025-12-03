package com.example.bdget.controller;

import com.example.bdget.config.MetricsConfig;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * TESTS PARA METRICS CONTROLLER
 * 
 * Verifica el correcto funcionamiento de los endpoints de métricas:
 * - Consulta de métricas personalizadas
 * - Simulación de uso de CPU/memoria
 * - Simulación de errores y tráfico
 * - Validaciones de parámetros
 * 
 * @author Evaluación DevOps
 * @version 1.0
 */
class MetricsControllerTest {

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private MetricsConfig metricsConfig;

    @Mock
    private Counter httpRequestCounter;

    @Mock
    private Counter errorCounter;

    @InjectMocks
    private MetricsController metricsController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Configurar comportamiento de los mocks
        when(httpRequestCounter.count()).thenReturn(100.0);
        when(errorCounter.count()).thenReturn(5.0);
        when(metricsConfig.getActiveConnections()).thenReturn(new java.util.concurrent.atomic.AtomicInteger(10));
        when(metricsConfig.getCpuUsage()).thenReturn(new java.util.concurrent.atomic.AtomicInteger(50));
        when(metricsConfig.getMemoryUsage()).thenReturn(new java.util.concurrent.atomic.AtomicInteger(60));
    }

    @Test
    void testGetCustomMetrics_ShouldReturnMetrics() {
        // When
        ResponseEntity<Map<String, Object>> response = metricsController.getCustomMetrics();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKeys(
            "httpRequestsTotal",
            "errorsTotal",
            "activeConnections",
            "cpuUsagePercent",
            "memoryUsagePercent",
            "timestamp"
        );
        assertThat(response.getBody().get("httpRequestsTotal")).isEqualTo(100.0);
        assertThat(response.getBody().get("errorsTotal")).isEqualTo(5.0);
    }

    @Test
    void testSimulateCpuUsage_WithValidValue_ShouldUpdateCpu() {
        // Given
        int cpuValue = 75;

        // When
        ResponseEntity<Map<String, Object>> response = metricsController.simulateCpuUsage(cpuValue);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("cpuUsagePercent")).isEqualTo(75);
        verify(metricsConfig).updateCpuUsage(75);
    }

    @Test
    void testSimulateCpuUsage_WithInvalidValue_ShouldReturnBadRequest() {
        // Given
        int invalidValue = 150;

        // When
        ResponseEntity<Map<String, Object>> response = metricsController.simulateCpuUsage(invalidValue);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("error");
        verify(metricsConfig, never()).updateCpuUsage(anyInt());
    }

    @Test
    void testSimulateMemoryUsage_WithValidValue_ShouldUpdateMemory() {
        // Given
        int memoryValue = 80;

        // When
        ResponseEntity<Map<String, Object>> response = metricsController.simulateMemoryUsage(memoryValue);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("memoryUsagePercent")).isEqualTo(80);
        verify(metricsConfig).updateMemoryUsage(80);
    }

    @Test
    void testSimulateMemoryUsage_WithInvalidValue_ShouldReturnBadRequest() {
        // Given
        int invalidValue = -10;

        // When
        ResponseEntity<Map<String, Object>> response = metricsController.simulateMemoryUsage(invalidValue);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("error");
        verify(metricsConfig, never()).updateMemoryUsage(anyInt());
    }

    @Test
    void testSimulateError_ShouldIncrementErrorCounter() {
        // Given
        when(errorCounter.count()).thenReturn(6.0);

        // When
        ResponseEntity<Map<String, Object>> response = metricsController.simulateError();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKeys("message", "errorMessage", "totalErrors");
        verify(errorCounter).increment();
    }

    @Test
    void testSimulateTraffic_WithValidRequests_ShouldIncrementCounters() {
        // Given
        int requests = 50;

        // When
        ResponseEntity<Map<String, Object>> response = metricsController.simulateTraffic(requests);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("requestsGenerated")).isEqualTo(50);
        verify(httpRequestCounter, times(50)).increment();
    }

    @Test
    void testSimulateTraffic_WithInvalidRequests_ShouldReturnBadRequest() {
        // Given
        int invalidRequests = 5000;

        // When
        ResponseEntity<Map<String, Object>> response = metricsController.simulateTraffic(invalidRequests);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsKey("error");
        verify(httpRequestCounter, never()).increment();
    }

    @Test
    void testResetConnections_ShouldResetActiveConnections() {
        // When
        ResponseEntity<Map<String, String>> response = metricsController.resetConnections();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("message");
    }

    @Test
    void testGetRegistryInfo_ShouldReturnRegistryInformation() {
        // Given
        when(meterRegistry.getMeters()).thenReturn(java.util.Collections.emptyList());

        // When
        ResponseEntity<Map<String, Object>> response = metricsController.getRegistryInfo();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKeys("totalMeters", "metricsEndpoint", "message");
    }
}
