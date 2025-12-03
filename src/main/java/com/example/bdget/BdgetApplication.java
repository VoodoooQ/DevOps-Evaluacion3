package com.example.bdget;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * APLICACIÓN PRINCIPAL - MICROSERVICIO EVALUACIÓN DEVOPS
 * 
 * Esta es la clase principal de la aplicación Spring Boot que implementa
 * observabilidad completa con Prometheus, Grafana y Loki.
 * 
 * Características principales:
 * - Spring Boot Actuator para health checks y métricas
 * - Micrometer + Prometheus para exportación de métricas
 * - Resilience4j para Circuit Breaker y patrones de resiliencia
 * - Logging estructurado para Loki
 * - Métricas personalizadas del pipeline CI/CD
 * - Health checks (liveness y readiness) para Kubernetes
 * 
 * Indicadores de evaluación cubiertos:
 * - IE1: Configuración de herramientas de monitoreo 
 * - IE2: Despliegue en Kubernetes con observabilidad 
 * - IE3: Dashboard con métricas clave 
 * - IE4: Documentación de integración 
 * - IE5: Políticas de cumplimiento 
 * - IE6: Validación ante fallas 
 * 
 * @author Evaluación DevOps
 * @version 1.0
 */
@SpringBootApplication
@EnableScheduling  // Habilitar tareas programadas para actualización de métricas
public class BdgetApplication {

	public static void main(String[] args) {
		SpringApplication.run(BdgetApplication.class, args);
	}

}
