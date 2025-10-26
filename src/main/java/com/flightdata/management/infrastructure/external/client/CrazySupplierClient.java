package com.flightdata.management.infrastructure.external.client;

import com.flightdata.management.infrastructure.external.dto.CrazySupplierDTO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Client for communicating with CrazySupplier API.
 *
 * <p>This client uses Spring's RestClient for synchronous HTTP communication.
 *
 * <p>The client implements graceful degradation: if CrazySupplier is unavailable,
 * it returns an empty list rather than failing the entire search operation.
 *
 * @author Vítor Matosinho Martins
 * @version 1.0
 * @since 2025-10-24
 */
@Component
@Slf4j
public class CrazySupplierClient {

  private final RestClient restClient;
  /**
   * -- GETTER --
   *  Checks if the CrazySupplier integration is enabled.
   *
   */
  @Getter
  private final boolean enabled;

  /**
   * Constructor with configuration injection.
   *
   * <p>Creates a RestClient instance configured with:
   * <ul>
   *   <li>Base URL for CrazySupplier API</li>
   *   <li>Request/response timeout</li>
   *   <li>Default headers (Content-Type, Accept)</li>
   * </ul>
   *
   * @param restClientBuilder Spring RestClient builder (auto-configured)
   * @param baseUrl CrazySupplier API base URL from configuration
   * @param timeoutMs request timeout in milliseconds
   * @param enabled whether the CrazySupplier integration is enabled
   */
  public CrazySupplierClient(
          RestClient.Builder restClientBuilder,
          @Value("${application.crazy-supplier.base-url}") String baseUrl,
          @Value("${application.crazy-supplier.timeout:5000}") int timeoutMs,
          @Value("${application.crazy-supplier.enabled:true}") boolean enabled) {


    this.enabled = enabled;

    // Force HTTP/1.1 for improved compatibility with WireMock and test stubs
    java.net.http.HttpClient javaHttpClient = java.net.http.HttpClient.newBuilder()
            .version(java.net.http.HttpClient.Version.HTTP_1_1)
            .build();

    org.springframework.http.client.JdkClientHttpRequestFactory requestFactory =
            new org.springframework.http.client.JdkClientHttpRequestFactory(javaHttpClient);

    this.restClient = restClientBuilder
            .baseUrl(baseUrl)
            .requestFactory(requestFactory)
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
            .build();

    log.info("CrazySupplierClient initialized - enabled: {}, baseUrl: {}, timeout: {}ms",
            enabled, baseUrl, timeoutMs);
  }

  /**
   * Fetches flights from CrazySupplier API based on search criteria.
   *
   * <p>This method uses Spring Retry for automatic retry on transient failures:
   * <ul>
   *   <li>Retries on: HttpServerErrorException (5xx), RestClientException</li>
   *   <li>Does NOT retry on: HttpClientErrorException (4xx - client errors)</li>
   *   <li>Max attempts: 3</li>
   *   <li>Backoff: Exponential (1s, 2s, 4s)</li>
   * </ul>
   *
   * <p>If CrazySupplier is unavailable or returns an error after retries,
   * this method returns an empty list to enable graceful degradation.
   *
   * @param from departure airport code (3-letter IATA code)
   * @param to destination airport code (3-letter IATA code)
   * @param outboundDate departure date in CET timezone
   * @param inboundDate arrival date in CET timezone
   * @return list of flight responses from CrazySupplier, empty list if unavailable
   */
  @Retryable(
          retryFor = {HttpServerErrorException.class, RestClientException.class},
          noRetryFor = {HttpClientErrorException.class},
          maxAttempts = 3,
          backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 5000)
  )
  public List<CrazySupplierDTO.Response> searchFlights(
          String from,
          String to,
          LocalDate outboundDate,
          LocalDate inboundDate) {

    if (!enabled) {
      log.debug("CrazySupplier integration is disabled");
      return Collections.emptyList();
    }

    log.debug("Searching CrazySupplier flights: {} -> {}, dates: {} to {}",
            from, to, outboundDate, inboundDate);

    try {
      // Build request payload
      CrazySupplierDTO.Request request = CrazySupplierDTO.Request.builder()
              .from(from)
              .to(to)
              .outboundDate(outboundDate)
              .inboundDate(inboundDate)
              .build();

      // Execute HTTP POST request
      List<CrazySupplierDTO.Response> responses = restClient.post()
              .uri("/flights")
              .contentType(MediaType.APPLICATION_JSON)
              .body(request)
              .retrieve()
              .body(new ParameterizedTypeReference<>() {
              });

      // Handle null response
      if (responses == null) {
        log.warn("CrazySupplier returned null response");
        return Collections.emptyList();
      }

      // Filter out invalid responses
      List<CrazySupplierDTO.Response> validResponses = responses.stream()
              .filter(CrazySupplierDTO.Response::isValid)
              .toList();

      if (validResponses.size() < responses.size()) {
        log.warn("Filtered out {} invalid responses from CrazySupplier",
                responses.size() - validResponses.size());
      }

      log.info("Successfully retrieved {} valid flights from CrazySupplier",
              validResponses.size());

      return validResponses;

    } catch (HttpClientErrorException e) {
      // 4xx errors - client mistakes, don't retry
      log.error("Client error from CrazySupplier: {} - {}",
              e.getStatusCode(), e.getStatusText());
      log.debug("Error body: {}", e.getResponseBodyAsString());
      return Collections.emptyList();

    } catch (HttpServerErrorException e) {
      // 5xx errors - @Retryable will retry server issues
      log.error("Server error from CrazySupplier: {} - {}",
              e.getStatusCode(), e.getStatusText());
      throw new CrazySupplierException("Server error: " + e.getStatusCode(), e);

    } catch (RestClientException e) {
      // Network issues, timeouts - will be retried by @Retryable
      log.error("Network error communicating with CrazySupplier: {}",
              e.getMessage());
      throw new CrazySupplierException("Network error", e);

    } catch (Exception e) {
      // Unexpected errors - log and return empty list (graceful degradation)
      log.error("Unexpected error fetching flights from CrazySupplier", e);
      return Collections.emptyList();
    }
  }

  /**
   * Custom exception for CrazySupplier client errors.
   * Used to trigger retry logic via Spring Retry.
   */
  public static class CrazySupplierException extends RuntimeException {
    public CrazySupplierException(String message) {
      super(message);
    }

    public CrazySupplierException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}