package com.flightdata.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main application class for Flight Data Management Application.
 *
 * <p>This application provides RESTful APIs for managing flight data with support for:
 * <ul>
 *   <li>CRUD operations on flight records</li>
 *   <li>Advanced search and filtering capabilities</li>
 *   <li>Integration with external suppliers (CrazySupplier)</li>
 *   <li>Pagination and sorting</li>
 * </ul>
 *
 * <p>The application follows Domain-Driven Design (DDD) principles with clear separation
 * of concerns across presentation, application, domain, and infrastructure layers.
 *
 * <p>Key features:
 * <ul>
 *   <li>OpenAPI 3.0 specification with code generation</li>
 *   <li>MySQL database with JPA/Hibernate</li>
 *   <li>RestClient for external API calls</li>
 *   <li>MapStruct for efficient object mapping</li>
 *   <li>Comprehensive error handling and validation</li>
 *   <li>Docker support for containerized deployment</li>
 * </ul>
 *
 * @author Flight Data Management Team
 * @version 1.0
 * @since 2025-10-24
 * @see org.springframework.boot.SpringApplication
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.flightdata.management.domain.repository")
public class FlightDataManagementApplication {

    /**
     * Main entry point for the Spring Boot application.
     *
     * <p>This method bootstraps the Spring application context and starts the embedded
     * web server. The application will be available on the configured port (default: 8080).
     *
     * <p>Environment-specific configuration can be activated using Spring profiles:
     * <ul>
     *   <li>default: Local development with local MySQL</li>
     *   <li>docker: Docker container deployment</li>
     *   <li>test: Test environment with H2 an in-memory database</li>
     * </ul>
     *
     * <p>Example usage:
     * <pre>{@code
     * // Run with default profile
     * java -jar flight-management.jar
     *
     * // Run with docker profile
     * java -jar flight-management.jar --spring.profiles.active=docker
     *
     * // Run with custom port
     * java -jar flight-management.jar --server.port=9090
     * }</pre>
     *
     * @param args command line arguments passed to the application
     * @see SpringApplication#run(Class, String...)
     */
    public static void main(String[] args) {
        SpringApplication.run(FlightDataManagementApplication.class, args);
    }
}