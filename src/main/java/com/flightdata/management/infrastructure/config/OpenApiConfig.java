package com.flightdata.management.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration for OpenAPI/Swagger documentation.
 *
 * @author Vítor Matosinho Martins
 * @version 1.0
 * @since 2025-10-24
 */
@Configuration
public class OpenApiConfig {

  @Value("${server.port:8080}")
  private String serverPort;

  /**
   * Configures OpenAPI documentation.
   *
   * @return OpenAPI configuration
   */
  @Bean
  public OpenAPI flightManagementOpenAPI() {
    Server localServer = new Server()
            .url("http://localhost:" + serverPort)
            .description("Local development server");

    Server productionServer = new Server()
            .url("https://api.flightdata.com")
            .description("Production server");

    Contact contact = new Contact()
            .name("Vítor Matosinho Martins")
            .email("vitor.matosinho.martins@gmail.com")
            .url("https://github.com/vitormmartins/flight-management");

    License license = new License()
            .name("MIT License")
            .url("https://opensource.org/licenses/MIT");

    Info info = new Info()
            .title("Flight Data Management API")
            .version("1.0.0")
            .description("RESTful API for managing flight data with support for CRUD operations " +
                    "and integration with external supplier CrazySupplier.")
            .contact(contact)
            .license(license);

    return new OpenAPI()
            .info(info)
            .servers(List.of(localServer, productionServer));
  }
}