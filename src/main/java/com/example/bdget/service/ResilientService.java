package com.example.bdget.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.function.Supplier;

/**
 * RESILIENT SERVICE
 * 
 * Este servicio implementa patrones de resiliencia utilizando Resilience4j:
 * - Circuit Breaker: Protege contra fallos en cascada
 * - Retry: Reintenta operaciones fallidas automáticamente
 * - Fallback: Proporciona respuestas alternativas en caso de fallo
 * 
 * Estos patrones son esenciales para:
 * - IE6: Validación ante fallas del sistema
 * - Alta disponibilidad
 * - Recuperación automática
 * - Protección del entorno productivo
 * 
 * @author Evaluación DevOps
 * @version 1.0
 */
@Service
public class ResilientService {

    private static final Logger logger = LoggerFactory.getLogger(ResilientService.class);

    @Autowired
    private CircuitBreaker backendCircuitBreaker;

    @Autowired
    private Retry backendRetry;

    private final Random random = new Random();

    /**
     * Ejecutar operación resiliente con Circuit Breaker
     * 
     * Este método demuestra el uso del Circuit Breaker para proteger
     * una operación que puede fallar. El Circuit Breaker:
     * 
     * 1. En estado CLOSED: Permite todas las llamadas
     * 2. Si detecta muchos fallos (>50%): Abre el circuito (estado OPEN)
     * 3. En estado OPEN: Rechaza llamadas inmediatamente (fail-fast)
     * 4. Después de 60s: Intenta recuperación (estado HALF_OPEN)
     * 5. Si funciona: Vuelve a CLOSED
     * 
     * @param shouldFail Si es true, fuerza un fallo para probar el Circuit Breaker
     * @return String con el resultado de la operación
     * @throws Exception Si la operación falla y el Circuit Breaker está abierto
     */
    public String executeResilientOperation(boolean shouldFail) {
        logger.info("Ejecutando operación resiliente con shouldFail={}", shouldFail);

        // Decorar la operación con Circuit Breaker
        Supplier<String> decoratedSupplier = CircuitBreaker
                .decorateSupplier(backendCircuitBreaker, () -> simulateBackendCall(shouldFail));

        try {
            // Ejecutar operación protegida
            String result = decoratedSupplier.get();
            logger.info("Operación resiliente exitosa: {}", result);
            return result;

        } catch (Exception e) {
            logger.error("Operación resiliente falló: {}", e.getMessage());
            
            // Verificar estado del Circuit Breaker
            String cbState = backendCircuitBreaker.getState().toString();
            logger.warn("Estado del Circuit Breaker: {}", cbState);
            
            // Si el Circuit Breaker está abierto, usar fallback
            if ("OPEN".equals(cbState)) {
                return executeFallback();
            }
            
            throw e;
        }
    }

    /**
     * Ejecutar operación con Retry
     * 
     * Este método demuestra el patrón de Retry, que reintenta
     * automáticamente operaciones fallidas hasta un máximo de 3 veces,
     * con espera exponencial entre intentos (1s, 2s, 4s).
     * 
     * @param shouldFail Si es true, fuerza fallos para probar el Retry
     * @return String con el resultado de la operación
     */
    public String executeWithRetry(boolean shouldFail) {
        logger.info("Ejecutando operación con Retry, shouldFail={}", shouldFail);

        // Decorar la operación con Retry
        Supplier<String> decoratedSupplier = Retry
                .decorateSupplier(backendRetry, () -> simulateBackendCall(shouldFail));

        try {
            String result = decoratedSupplier.get();
            logger.info("Operación con Retry exitosa: {}", result);
            return result;

        } catch (Exception e) {
            logger.error("Operación con Retry falló después de todos los intentos: {}", e.getMessage());
            return executeFallback();
        }
    }

    /**
     * Ejecutar operación con Circuit Breaker y Retry combinados
     * 
     * Este método combina ambos patrones para máxima resiliencia:
     * 1. Primero intenta con Retry (hasta 3 veces)
     * 2. Si sigue fallando, el Circuit Breaker se abre
     * 3. Futuras llamadas fallan inmediatamente (fail-fast)
     * 4. Después del tiempo de espera, intenta recuperación
     * 
     * @param shouldFail Si es true, fuerza fallos para probar ambos patrones
     * @return String con el resultado de la operación
     */
    public String executeWithCombinedResilience(boolean shouldFail) {
        logger.info("Ejecutando operación con Circuit Breaker + Retry");

        // Decorar primero con Retry, luego con Circuit Breaker
        Supplier<String> decoratedSupplier = CircuitBreaker.decorateSupplier(
                backendCircuitBreaker,
                Retry.decorateSupplier(backendRetry, () -> simulateBackendCall(shouldFail))
        );

        try {
            String result = decoratedSupplier.get();
            logger.info("Operación combinada exitosa: {}", result);
            return result;

        } catch (Exception e) {
            logger.error("Operación combinada falló: {}", e.getMessage());
            return executeFallback();
        }
    }

    /**
     * Simular llamada a un servicio backend
     * 
     * Este método simula una llamada a un servicio externo que puede fallar.
     * En un escenario real, esto sería una llamada HTTP a otro microservicio,
     * una consulta a base de datos, etc.
     * 
     * @param shouldFail Si es true, fuerza un fallo
     * @return String con resultado exitoso
     * @throws RuntimeException Si la operación falla
     */
    private String simulateBackendCall(boolean shouldFail) {
        logger.debug("Simulando llamada a backend...");

        // Simular latencia (100-500ms)
        try {
            int latency = random.nextInt(400) + 100;
            Thread.sleep(latency);
            logger.debug("Latencia simulada: {}ms", latency);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operación interrumpida", e);
        }

        // Si shouldFail es true, lanzar excepción
        if (shouldFail) {
            logger.warn("Forzando fallo en backend");
            throw new RuntimeException("Backend service is unavailable");
        }

        // Simular fallo aleatorio (10% de probabilidad)
        if (random.nextInt(100) < 10) {
            logger.warn("Fallo aleatorio en backend (10% de probabilidad)");
            throw new RuntimeException("Random backend failure");
        }

        String result = "Backend call successful - Data retrieved";
        logger.debug("Llamada a backend exitosa");
        return result;
    }

    /**
     * Método de fallback
     * 
     * Este método se ejecuta cuando la operación principal falla
     * y todos los reintentos se agotan. Proporciona una respuesta
     * alternativa para mantener la disponibilidad del servicio.
     * 
     * En un escenario real, esto podría:
     * - Retornar datos de caché
     * - Retornar valores por defecto
     * - Llamar a un servicio de respaldo
     * - Retornar datos degradados
     * 
     * @return String con respuesta de fallback
     */
    private String executeFallback() {
        logger.warn("Ejecutando fallback - Operación principal falló");
        return "Fallback response: Service temporarily unavailable, using cached data";
    }

    /**
     * Obtener estado del Circuit Breaker
     * 
     * Este método permite consultar el estado actual del Circuit Breaker.
     * Útil para debugging y monitoreo.
     * 
     * @return String con el estado (CLOSED, OPEN, HALF_OPEN)
     */
    public String getCircuitBreakerState() {
        String state = backendCircuitBreaker.getState().toString();
        logger.info("Estado actual del Circuit Breaker: {}", state);
        return state;
    }

    /**
     * Obtener métricas del Circuit Breaker
     * 
     * Retorna información sobre el número de llamadas exitosas,
     * fallidas, y la tasa de fallos actual.
     * 
     * @return String con métricas del Circuit Breaker
     */
    public String getCircuitBreakerMetrics() {
        var metrics = backendCircuitBreaker.getMetrics();
        
        String info = String.format(
            "Circuit Breaker Metrics - Successful: %d, Failed: %d, Failure Rate: %.2f%%",
            metrics.getNumberOfSuccessfulCalls(),
            metrics.getNumberOfFailedCalls(),
            metrics.getFailureRate()
        );
        
        logger.info(info);
        return info;
    }

    /**
     * Resetear Circuit Breaker
     * 
     * Fuerza la transición del Circuit Breaker al estado CLOSED.
     * Útil para pruebas y recovery manual.
     */
    public void resetCircuitBreaker() {
        logger.warn("Reseteando Circuit Breaker manualmente");
        backendCircuitBreaker.transitionToClosedState();
    }
}
