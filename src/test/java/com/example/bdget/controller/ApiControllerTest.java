package com.example.bdget.controller;

import com.example.bdget.service.ResilientService;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * TESTS PARA API CONTROLLER
 * 
 * Verifica el correcto funcionamiento de los endpoints de la API:
 * - Endpoint de prueba
 * - Saludo personalizado
 * - Endpoint resiliente con Circuit Breaker
 * - Echo de datos
 * - Información de la aplicación
 * 
 * @author Evaluación DevOps
 * @version 1.0
 */
class ApiControllerTest {

    @Mock
    private Counter httpRequestCounter;

    @Mock
    private Counter errorCounter;

    @Mock
    private ResilientService resilientService;

    @InjectMocks
    private ApiController apiController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testTest_ShouldReturnSuccessResponse() {
        // When
        ResponseEntity<Map<String, Object>> response = apiController.test();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("success");
        assertThat(response.getBody().get("endpoint")).isEqualTo("/api/test");
        assertThat(response.getBody()).containsKey("timestamp");
        verify(httpRequestCounter).increment();
    }

    @Test
    void testHello_WithDefaultName_ShouldReturnDefaultGreeting() {
        // When
        ResponseEntity<Map<String, Object>> response = apiController.hello("Mundo");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isEqualTo("Hola, Mundo!");
        verify(httpRequestCounter).increment();
    }

    @Test
    void testHello_WithCustomName_ShouldReturnCustomGreeting() {
        // Given
        String name = "Juan";

        // When
        ResponseEntity<Map<String, Object>> response = apiController.hello(name);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isEqualTo("Hola, Juan!");
        verify(httpRequestCounter).increment();
    }

    @Test
    void testResilientEndpoint_WithSuccess_ShouldReturnSuccessResponse() {
        // Given
        when(resilientService.executeResilientOperation(false))
                .thenReturn("Operation successful");

        // When
        ResponseEntity<Map<String, Object>> response = apiController.resilientEndpoint(false);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("success");
        assertThat(response.getBody().get("circuitBreakerUsed")).isEqualTo(true);
        verify(httpRequestCounter).increment();
        verify(resilientService).executeResilientOperation(false);
    }

    @Test
    void testResilientEndpoint_WithFailure_ShouldReturnErrorResponse() {
        // Given
        when(resilientService.executeResilientOperation(true))
                .thenThrow(new RuntimeException("Service unavailable"));

        // When
        ResponseEntity<Map<String, Object>> response = apiController.resilientEndpoint(true);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("error");
        assertThat(response.getBody()).containsKey("error");
        verify(httpRequestCounter).increment();
        verify(errorCounter).increment();
        verify(resilientService).executeResilientOperation(true);
    }

    @Test
    void testEcho_ShouldReturnSamePayload() {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("message", "Test message");
        payload.put("number", 42);

        // When
        ResponseEntity<Map<String, Object>> response = apiController.echo(payload);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("success");
        assertThat(response.getBody()).containsKey("echo");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> echo = (Map<String, Object>) response.getBody().get("echo");
        assertThat(echo).isEqualTo(payload);
        verify(httpRequestCounter).increment();
    }

    @Test
    void testInfo_ShouldReturnApplicationInformation() {
        // When
        ResponseEntity<Map<String, Object>> response = apiController.info();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKeys(
            "name", "version", "description", "javaVersion", "endpoints", "timestamp"
        );
        assertThat(response.getBody().get("name")).isEqualTo("Microservicio Evaluación DevOps");
        assertThat(response.getBody().get("version")).isEqualTo("1.0.0");
        verify(httpRequestCounter).increment();
    }

    @Test
    void testInfo_ShouldIncludeEndpointsList() {
        // When
        ResponseEntity<Map<String, Object>> response = apiController.info();

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("endpoints");
        
        @SuppressWarnings("unchecked")
        Map<String, String> endpoints = (Map<String, String>) response.getBody().get("endpoints");
        assertThat(endpoints).isNotEmpty();
        assertThat(endpoints).containsKeys(
            "test", "hello", "resilient", "echo", "info", 
            "health", "liveness", "readiness", "metrics", "prometheus"
        );
    }

    @Test
    void testHandleException_ShouldReturnErrorResponse() {
        // Given
        Exception exception = new RuntimeException("Test exception");

        // When
        ResponseEntity<Map<String, Object>> response = apiController.handleException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("error");
        assertThat(response.getBody().get("error")).isEqualTo("Test exception");
        assertThat(response.getBody()).containsKey("timestamp");
        verify(errorCounter).increment();
    }

    @Test
    void testTest_ShouldHaveTimestamp() {
        // When
        ResponseEntity<Map<String, Object>> response = apiController.test();

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("timestamp");
        
        Object timestamp = response.getBody().get("timestamp");
        assertThat(timestamp).isInstanceOf(Long.class);
        assertThat((Long) timestamp).isGreaterThan(0L);
    }

    @Test
    void testEcho_WithEmptyPayload_ShouldReturnEmptyEcho() {
        // Given
        Map<String, Object> emptyPayload = new HashMap<>();

        // When
        ResponseEntity<Map<String, Object>> response = apiController.echo(emptyPayload);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("success");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> echo = (Map<String, Object>) response.getBody().get("echo");
        assertThat(echo).isEmpty();
    }
}
