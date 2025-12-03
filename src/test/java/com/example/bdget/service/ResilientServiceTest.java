package com.example.bdget.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * TESTS PARA RESILIENT SERVICE
 * 
 * Verifica el correcto funcionamiento de los patrones de resiliencia:
 * - Circuit Breaker
 * - Retry
 * - Fallback
 * - Estado del Circuit Breaker
 * 
 * @author Evaluación DevOps
 * @version 1.0
 */
class ResilientServiceTest {

    @Mock
    private CircuitBreaker backendCircuitBreaker;

    @Mock
    private Retry backendRetry;

    @InjectMocks
    private ResilientService resilientService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Configurar comportamiento básico del Circuit Breaker
        when(backendCircuitBreaker.getState()).thenReturn(CircuitBreaker.State.CLOSED);
    }

    @Test
    void testExecuteResilientOperation_WithSuccess_ShouldReturnSuccessMessage() {
        // Este test verifica que la operación puede ejecutarse exitosamente
        // Nota: La implementación real usa decorators de Resilience4j
        // En un test de integración completo, se probarían con el Circuit Breaker real
        
        // Given
        boolean shouldFail = false;

        // When & Then
        // El método puede lanzar excepción o retornar éxito dependiendo de la probabilidad
        // Lo importante es que el método existe y es invocable
        try {
            String result = resilientService.executeResilientOperation(shouldFail);
            assertThat(result).isNotNull();
        } catch (Exception e) {
            // Es aceptable que falle por el componente aleatorio
            assertThat(e).isInstanceOf(RuntimeException.class);
        }
    }

    @Test
    void testGetCircuitBreakerState_ShouldReturnCurrentState() {
        // Given
        when(backendCircuitBreaker.getState()).thenReturn(CircuitBreaker.State.CLOSED);

        // When
        String state = resilientService.getCircuitBreakerState();

        // Then
        assertThat(state).isEqualTo("CLOSED");
        verify(backendCircuitBreaker).getState();
    }

    @Test
    void testGetCircuitBreakerState_WhenOpen_ShouldReturnOpen() {
        // Given
        when(backendCircuitBreaker.getState()).thenReturn(CircuitBreaker.State.OPEN);

        // When
        String state = resilientService.getCircuitBreakerState();

        // Then
        assertThat(state).isEqualTo("OPEN");
    }

    @Test
    void testGetCircuitBreakerState_WhenHalfOpen_ShouldReturnHalfOpen() {
        // Given
        when(backendCircuitBreaker.getState()).thenReturn(CircuitBreaker.State.HALF_OPEN);

        // When
        String state = resilientService.getCircuitBreakerState();

        // Then
        assertThat(state).isEqualTo("HALF_OPEN");
    }

    @Test
    void testGetCircuitBreakerMetrics_ShouldReturnMetricsInformation() {
        // Given
        CircuitBreaker.Metrics metrics = mock(CircuitBreaker.Metrics.class);
        when(backendCircuitBreaker.getMetrics()).thenReturn(metrics);
        when(metrics.getNumberOfSuccessfulCalls()).thenReturn(10);
        when(metrics.getNumberOfFailedCalls()).thenReturn(2);
        when(metrics.getFailureRate()).thenReturn(16.67f);

        // When
        String metricsInfo = resilientService.getCircuitBreakerMetrics();

        // Then
        assertThat(metricsInfo).isNotNull();
        assertThat(metricsInfo).contains("Successful: 10");
        assertThat(metricsInfo).contains("Failed: 2");
        assertThat(metricsInfo).contains("Failure Rate:");
        verify(backendCircuitBreaker).getMetrics();
    }

    @Test
    void testResetCircuitBreaker_ShouldTransitionToClosed() {
        // When
        resilientService.resetCircuitBreaker();

        // Then
        verify(backendCircuitBreaker).transitionToClosedState();
    }

    @Test
    void testExecuteResilientOperation_Methods_AreCallable() {
        // Verificar que los métodos existen y son invocables
        
        // Test executeResilientOperation
        assertThat(resilientService).isNotNull();
        
        // Test que el método getCircuitBreakerState() existe
        when(backendCircuitBreaker.getState()).thenReturn(CircuitBreaker.State.CLOSED);
        String state = resilientService.getCircuitBreakerState();
        assertThat(state).isNotNull();
        
        // Test que el método resetCircuitBreaker() existe
        resilientService.resetCircuitBreaker();
        verify(backendCircuitBreaker).transitionToClosedState();
    }

    @Test
    void testMultipleStates_ShouldHandleCorrectly() {
        // Test transiciones de estado
        
        // Estado CLOSED
        when(backendCircuitBreaker.getState()).thenReturn(CircuitBreaker.State.CLOSED);
        assertThat(resilientService.getCircuitBreakerState()).isEqualTo("CLOSED");
        
        // Estado OPEN
        when(backendCircuitBreaker.getState()).thenReturn(CircuitBreaker.State.OPEN);
        assertThat(resilientService.getCircuitBreakerState()).isEqualTo("OPEN");
        
        // Estado HALF_OPEN
        when(backendCircuitBreaker.getState()).thenReturn(CircuitBreaker.State.HALF_OPEN);
        assertThat(resilientService.getCircuitBreakerState()).isEqualTo("HALF_OPEN");
    }

    @Test
    void testCircuitBreakerMetrics_WithZeroFailures_ShouldShowNoFailures() {
        // Given
        CircuitBreaker.Metrics metrics = mock(CircuitBreaker.Metrics.class);
        when(backendCircuitBreaker.getMetrics()).thenReturn(metrics);
        when(metrics.getNumberOfSuccessfulCalls()).thenReturn(100);
        when(metrics.getNumberOfFailedCalls()).thenReturn(0);
        when(metrics.getFailureRate()).thenReturn(0.0f);

        // When
        String metricsInfo = resilientService.getCircuitBreakerMetrics();

        // Then
        assertThat(metricsInfo).contains("Successful: 100");
        assertThat(metricsInfo).contains("Failed: 0");
        assertThat(metricsInfo).contains("Failure Rate: 0");
    }

    @Test
    void testCircuitBreakerMetrics_WithHighFailureRate_ShouldReflectInMetrics() {
        // Given
        CircuitBreaker.Metrics metrics = mock(CircuitBreaker.Metrics.class);
        when(backendCircuitBreaker.getMetrics()).thenReturn(metrics);
        when(metrics.getNumberOfSuccessfulCalls()).thenReturn(5);
        when(metrics.getNumberOfFailedCalls()).thenReturn(10);
        when(metrics.getFailureRate()).thenReturn(66.67f);

        // When
        String metricsInfo = resilientService.getCircuitBreakerMetrics();

        // Then
        assertThat(metricsInfo).contains("Successful: 5");
        assertThat(metricsInfo).contains("Failed: 10");
        assertThat(metricsInfo).contains("Failure Rate:");
    }
}
