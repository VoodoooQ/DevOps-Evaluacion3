package com.example.bdget.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * CONFIGURACIÓN DE MÉTRICAS PERSONALIZADAS
 * 
 * Esta clase configura métricas personalizadas que serán exportadas a Prometheus
 * para su visualización en Grafana. Incluye contadores, gauges y timers para
 * medir el rendimiento y comportamiento de la aplicación.
 * 
 * Métricas configuradas:
 * - Contador de requests HTTP
 * - Contador de errores
 * - Gauge de conexiones activas
 * - Timer de latencia de operaciones
 * 
 * @author Evaluación DevOps
 * @version 1.0
 */
@Configuration
public class MetricsConfig {

    /**
     * Contador atómico para rastrear el número de conexiones activas.
     * Se utiliza con un Gauge para exponer esta métrica a Prometheus.
     */
    private final AtomicInteger activeConnections = new AtomicInteger(0);

    /**
     * Contador atómico para rastrear el uso de CPU simulado.
     * Útil para pruebas de escalado y alertas.
     */
    private final AtomicInteger cpuUsage = new AtomicInteger(50);

    /**
     * Contador atómico para rastrear el uso de memoria simulado.
     * Útil para pruebas de escalado y alertas.
     */
    private final AtomicInteger memoryUsage = new AtomicInteger(60);

    /**
     * Bean: Contador de requests HTTP totales
     * 
     * Este contador se incrementa cada vez que la aplicación recibe una petición HTTP.
     * Es útil para medir el tráfico y la carga del sistema.
     * 
     * Métrica expuesta: application_http_requests_total
     * 
     * @param registry El registro de métricas de Micrometer
     * @return Counter configurado
     */
    @Bean
    public Counter httpRequestCounter(MeterRegistry registry) {
        return Counter.builder("application.http.requests")
                .description("Total de peticiones HTTP recibidas")
                .tag("application", "microservicio-evaluacion")
                .register(registry);
    }

    /**
     * Bean: Contador de errores de la aplicación
     * 
     * Este contador se incrementa cuando ocurre un error en la aplicación.
     * Es crucial para detectar problemas y configurar alertas.
     * 
     * Métrica expuesta: application_errors_total
     * 
     * @param registry El registro de métricas de Micrometer
     * @return Counter configurado
     */
    @Bean
    public Counter errorCounter(MeterRegistry registry) {
        return Counter.builder("application.errors")
                .description("Total de errores en la aplicación")
                .tag("application", "microservicio-evaluacion")
                .register(registry);
    }

    /**
     * Bean: Gauge de conexiones activas
     * 
     * Un Gauge es una métrica que representa un valor que puede subir o bajar.
     * En este caso, representa el número de conexiones activas en tiempo real.
     * 
     * Métrica expuesta: application_active_connections
     * 
     * @param registry El registro de métricas de Micrometer
     * @return Gauge configurado
     */
    @Bean
    public Gauge activeConnectionsGauge(MeterRegistry registry) {
        return Gauge.builder("application.active.connections", activeConnections, AtomicInteger::get)
                .description("Número de conexiones activas")
                .tag("application", "microservicio-evaluacion")
                .register(registry);
    }

    /**
     * Bean: Gauge de uso de CPU simulado
     * 
     * Simula el uso de CPU del sistema (en %). 
     * En producción, esto se obtendría de métricas reales del sistema.
     * 
     * Métrica expuesta: application_cpu_usage_percent
     * 
     * @param registry El registro de métricas de Micrometer
     * @return Gauge configurado
     */
    @Bean
    public Gauge cpuUsageGauge(MeterRegistry registry) {
        return Gauge.builder("application.cpu.usage.percent", cpuUsage, AtomicInteger::get)
                .description("Uso de CPU en porcentaje (simulado)")
                .tag("application", "microservicio-evaluacion")
                .register(registry);
    }

    /**
     * Bean: Gauge de uso de memoria simulado
     * 
     * Simula el uso de memoria del sistema (en %).
     * En producción, esto se obtendría de métricas reales del sistema.
     * 
     * Métrica expuesta: application_memory_usage_percent
     * 
     * @param registry El registro de métricas de Micrometer
     * @return Gauge configurado
     */
    @Bean
    public Gauge memoryUsageGauge(MeterRegistry registry) {
        return Gauge.builder("application.memory.usage.percent", memoryUsage, AtomicInteger::get)
                .description("Uso de memoria en porcentaje (simulado)")
                .tag("application", "microservicio-evaluacion")
                .register(registry);
    }

    /**
     * Bean: Timer para medir latencia de operaciones
     * 
     * Un Timer mide la duración de operaciones y permite calcular percentiles
     * (p50, p95, p99) para análisis de performance.
     * 
     * Métrica expuesta: application_operation_duration_seconds
     * 
     * @param registry El registro de métricas de Micrometer
     * @return Timer configurado
     */
    @Bean
    public Timer operationTimer(MeterRegistry registry) {
        return Timer.builder("application.operation.duration")
                .description("Duración de operaciones en la aplicación")
                .tag("application", "microservicio-evaluacion")
                .publishPercentiles(0.5, 0.95, 0.99) // p50, p95, p99
                .register(registry);
    }

    /**
     * Métodos de utilidad para manipular las métricas desde otras clases
     */

    public void incrementActiveConnections() {
        activeConnections.incrementAndGet();
    }

    public void decrementActiveConnections() {
        activeConnections.decrementAndGet();
    }

    public void updateCpuUsage(int value) {
        cpuUsage.set(value);
    }

    public void updateMemoryUsage(int value) {
        memoryUsage.set(value);
    }

    public AtomicInteger getActiveConnections() {
        return activeConnections;
    }

    public AtomicInteger getCpuUsage() {
        return cpuUsage;
    }

    public AtomicInteger getMemoryUsage() {
        return memoryUsage;
    }
}
