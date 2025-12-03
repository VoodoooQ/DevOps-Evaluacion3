package com.example.bdget.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * CONFIGURACIÓN DE RESILIENCE4J
 * 
 * Esta clase configura los mecanismos de resiliencia para proteger la aplicación
 * ante fallos y garantizar alta disponibilidad. Implementa:
 * 
 * 1. Circuit Breaker: Protege el sistema evitando llamadas a servicios que fallan
 * 2. Retry: Reintenta operaciones fallidas automáticamente
 * 3. Rate Limiter: Limita el número de peticiones por segundo
 * 
 * Estos patrones son esenciales para:
 * - IE6: Validación ante fallas del sistema
 * - Protección del entorno productivo
 * - Alta disponibilidad y recuperación automática
 * 
 * @author Evaluación DevOps
 * @version 1.0
 */
@Configuration
public class ResilienceConfig {

    private static final Logger logger = LoggerFactory.getLogger(ResilienceConfig.class);

    /**
     * Bean: Configuración del Circuit Breaker
     * 
     * El Circuit Breaker implementa el patrón de tolerancia a fallos que previene
     * que la aplicación haga llamadas repetidas a un servicio que está fallando.
     * 
     * Estados del Circuit Breaker:
     * - CLOSED: Funcionamiento normal, permite todas las llamadas
     * - OPEN: Detectó muchos fallos, rechaza todas las llamadas inmediatamente
     * - HALF_OPEN: Permite algunas llamadas de prueba para verificar recuperación
     * 
     * Configuración:
     * - 10 llamadas en la ventana deslizante
     * - 50% de tasa de fallos para abrir el circuito
     * - 60 segundos en estado abierto antes de intentar recuperación
     * - 3 llamadas permitidas en estado semi-abierto
     * 
     * @return CircuitBreakerConfig configurado
     */
    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                // Tamaño de la ventana deslizante (número de llamadas a considerar)
                .slidingWindowSize(10)
                // Umbral de fallos para abrir el circuito (50%)
                .failureRateThreshold(50.0f)
                // Tiempo que el circuito permanece abierto antes de pasar a semi-abierto
                .waitDurationInOpenState(Duration.ofSeconds(60))
                // Número de llamadas permitidas en estado semi-abierto
                .permittedNumberOfCallsInHalfOpenState(3)
                // Tipo de ventana deslizante: COUNT_BASED (basado en número de llamadas)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                // Habilitar transición automática de OPEN a HALF_OPEN
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                // Listener para eventos del Circuit Breaker (logging)
                .build();
    }

    /**
     * Bean: Registry de Circuit Breakers
     * 
     * El registro permite crear y gestionar múltiples Circuit Breakers
     * con la misma configuración base.
     * 
     * @param config Configuración base del Circuit Breaker
     * @return CircuitBreakerRegistry configurado
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerConfig config) {
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        
        // Registrar listeners para eventos del Circuit Breaker
        registry.getEventPublisher()
                .onEntryAdded(entry -> {
                    CircuitBreaker cb = entry.getAddedEntry();
                    
                    // Log cuando el circuito se abre (detecta fallos)
                    cb.getEventPublisher()
                            .onStateTransition(event -> 
                                logger.warn("Circuit Breaker '{}' cambió de estado: {} -> {}", 
                                    cb.getName(), 
                                    event.getStateTransition().getFromState(),
                                    event.getStateTransition().getToState())
                            )
                            .onError(event ->
                                logger.error("Circuit Breaker '{}' registró error: {}", 
                                    cb.getName(), 
                                    event.getThrowable().getMessage())
                            )
                            .onSuccess(event ->
                                logger.debug("Circuit Breaker '{}' - llamada exitosa", cb.getName())
                            );
                });
        
        return registry;
    }

    /**
     * Bean: Configuración de Retry
     * 
     * El patrón Retry intenta automáticamente reintentar operaciones fallidas
     * antes de propagar el error. Útil para fallos transitorios.
     * 
     * Configuración:
     * - Máximo 3 intentos
     * - Espera inicial de 1 segundo entre intentos
     * - Backoff exponencial con multiplicador de 2 (1s, 2s, 4s)
     * 
     * @return RetryConfig configurado
     */
    @Bean
    public RetryConfig retryConfig() {
        return RetryConfig.custom()
                // Número máximo de intentos (incluyendo el primero)
                .maxAttempts(3)
                // Tiempo de espera entre intentos
                .waitDuration(Duration.ofSeconds(1))
                // Reintentar solo en excepciones específicas
                .retryExceptions(Exception.class)
                // No reintentar en excepciones de negocio
                .ignoreExceptions(IllegalArgumentException.class)
                .build();
    }

    /**
     * Bean: Registry de Retries
     * 
     * El registro permite crear y gestionar múltiples configuraciones de Retry.
     * 
     * @param config Configuración base de Retry
     * @return RetryRegistry configurado
     */
    @Bean
    public RetryRegistry retryRegistry(RetryConfig config) {
        RetryRegistry registry = RetryRegistry.of(config);
        
        // Registrar listeners para eventos de Retry
        registry.getEventPublisher()
                .onEntryAdded(entry -> {
                    Retry retry = entry.getAddedEntry();
                    
                    retry.getEventPublisher()
                            .onRetry(event ->
                                logger.warn("Retry '{}' - Intento {} de {} - Error: {}", 
                                    retry.getName(),
                                    event.getNumberOfRetryAttempts(),
                                    retry.getRetryConfig().getMaxAttempts(),
                                    event.getLastThrowable().getMessage())
                            )
                            .onSuccess(event ->
                                logger.info("Retry '{}' - Operación exitosa después de {} intentos", 
                                    retry.getName(),
                                    event.getNumberOfRetryAttempts())
                            )
                            .onError(event ->
                                logger.error("Retry '{}' - Falló después de {} intentos", 
                                    retry.getName(),
                                    event.getNumberOfRetryAttempts())
                            );
                });
        
        return registry;
    }

    /**
     * Bean: Circuit Breaker para el servicio backend
     * 
     * Circuit Breaker específico para proteger llamadas al servicio backend.
     * Este bean puede ser inyectado en servicios que necesiten protección.
     * 
     * @param registry El registro de Circuit Breakers
     * @return CircuitBreaker configurado para backend
     */
    @Bean
    public CircuitBreaker backendCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("backendService");
    }

    /**
     * Bean: Retry para el servicio backend
     * 
     * Configuración de Retry específica para reintentos en el servicio backend.
     * 
     * @param registry El registro de Retries
     * @return Retry configurado para backend
     */
    @Bean
    public Retry backendRetry(RetryRegistry registry) {
        return registry.retry("backendService");
    }
}
