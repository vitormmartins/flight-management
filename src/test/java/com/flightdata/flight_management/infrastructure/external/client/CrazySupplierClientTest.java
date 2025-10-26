package com.flightdata.flight_management.infrastructure.external.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flightdata.flight_management.infrastructure.external.dto.CrazySupplierDTO;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for CrazySupplierClient using WireMock.
 *
 * <p>These are integration tests because they test the HTTP communication layer
 * with a mocked external service. WireMock acts as a test double for the real
 * CrazySupplier API.
 *
 * <p>Test Categories:
 * <ul>
 *   <li>Success scenarios with various response sizes</li>
 *   <li>Error handling (4xx, 5xx, network errors)</li>
 *   <li>Retry mechanism verification</li>
 *   <li>Request validation</li>
 *   <li>Response filtering and validation</li>
 *   <li>Configuration and feature toggles</li>
 * </ul>
 *
 * @author Vítor Matosinho Martins
 * @version 1.0
 * @since 2025-10-24
 */
@DisplayName("CrazySupplierClient Integration Tests")
class CrazySupplierClientTest {

  private WireMockServer wireMockServer;
  private CrazySupplierClient crazySupplierClient;
  private ObjectMapper objectMapper;
  private String baseUrl;

  @BeforeEach
  void setUp() {
    // Start a WireMock server on a random available port
    wireMockServer = new WireMockServer(
            WireMockConfiguration.options()
                    .dynamicPort()
    );
    wireMockServer.start();

    // Configure WireMock client
    WireMock.configureFor("localhost", wireMockServer.port());

    baseUrl = "http://localhost:" + wireMockServer.port();

    // Create ObjectMapper with Java 8 time support
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());

    // Create a client under test
    crazySupplierClient = new CrazySupplierClient(
            RestClient.builder(),
            baseUrl,
            5000,  // 5-second timeout
            true   // enabled
    );
  }

  @AfterEach
  void tearDown() {
    if (wireMockServer != null && wireMockServer.isRunning()) {
      wireMockServer.stop();
    }
  }

  // ==================== SUCCESS SCENARIOS ====================

  @Test
  @DisplayName("Should successfully fetch flights from CrazySupplier")
  void testSearchFlights_Success() throws Exception {
    // Arrange
    List<CrazySupplierDTO.Response> mockResponses = Arrays.asList(
            CrazySupplierDTO.Response.builder()
                    .carrier("Lufthansa")
                    .basePrice(250.00)
                    .tax(50.00)
                    .departureAirportName("JFK")
                    .arrivalAirportName("FRA")
                    .outboundDateTime(LocalDateTime.of(2025, 10, 25, 10, 30))
                    .inboundDateTime(LocalDateTime.of(2025, 10, 25, 22, 45))
                    .build(),
            CrazySupplierDTO.Response.builder()
                    .carrier("Air France")
                    .basePrice(280.00)
                    .tax(55.00)
                    .departureAirportName("JFK")
                    .arrivalAirportName("FRA")
                    .outboundDateTime(LocalDateTime.of(2025, 10, 25, 14, 15))
                    .inboundDateTime(LocalDateTime.of(2025, 10, 26, 2, 30))
                    .build()
    );

    String jsonResponse = objectMapper.writeValueAsString(mockResponses);

    stubFor(post(urlEqualTo("/flights"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(jsonResponse)));

    // Act
    List<CrazySupplierDTO.Response> results = crazySupplierClient.searchFlights(
            "JFK",
            "FRA",
            LocalDate.of(2025, 10, 25),
            LocalDate.of(2025, 10, 26)
    );

    // Assert
    assertThat(results).isNotNull();
    assertThat(results).hasSize(2);

    assertThat(results.getFirst().getCarrier()).isEqualTo("Lufthansa");
    assertThat(results.getFirst().getTotalFare()).isEqualTo(300.00);
    assertThat(results.getFirst().getDepartureAirportName()).isEqualTo("JFK");
    assertThat(results.getFirst().getArrivalAirportName()).isEqualTo("FRA");

    assertThat(results.get(1).getCarrier()).isEqualTo("Air France");
    assertThat(results.get(1).getTotalFare()).isEqualTo(335.00);

    // Verify the request was made correctly
    verify(postRequestedFor(urlEqualTo("/flights"))
            .withHeader("Content-Type", containing("application/json"))
            .withHeader("Accept", containing("application/json")));
  }

  @Test
  @DisplayName("Should return empty list when CrazySupplier returns no flights")
  void testSearchFlights_EmptyResponse() throws Exception {
    // Arrange
    String jsonResponse = objectMapper.writeValueAsString(List.of());

    stubFor(post(urlEqualTo("/flights"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(jsonResponse)));

    // Act
    List<CrazySupplierDTO.Response> results = crazySupplierClient.searchFlights(
            "JFK",
            "LAX",
            LocalDate.of(2025, 10, 25),
            LocalDate.of(2025, 10, 26)
    );

    // Assert
    assertThat(results).isNotNull();
    assertThat(results).isEmpty();

    verify(1, postRequestedFor(urlEqualTo("/flights")));
  }

  @Test
  @DisplayName("Should verify correct request body is sent")
  @Disabled
  void testSearchFlights_VerifyRequestBody() throws Exception {
    // Arrange
    String jsonResponse = objectMapper.writeValueAsString(List.of());

    stubFor(post(urlEqualTo("/flights"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(jsonResponse)));

    // Act
    crazySupplierClient.searchFlights(
            "JFK",
            "LAX",
            LocalDate.of(2025, 10, 25),
            LocalDate.of(2025, 10, 26)
    );

    // Assert - Verify request body contains correct fields
    verify(postRequestedFor(urlEqualTo("/flights"))
            .withRequestBody(containing("\"from\":\"JFK\""))
            .withRequestBody(containing("\"to\":\"LAX\""))
            .withRequestBody(containing("\"outboundDate\":\"2025-10-25\""))
            .withRequestBody(containing("\"inboundDate\":\"2025-10-26\"")));
  }

  // ==================== ERROR HANDLING SCENARIOS ====================

  @Test
  @DisplayName("Should return empty list on 4xx client error (no retry)")
  void testSearchFlights_ClientError_ReturnsEmptyList() {
    // Arrange
    stubFor(post(urlEqualTo("/flights"))
            .willReturn(aResponse()
                    .withStatus(400)
                    .withBody("Bad Request: Invalid airport code")));

    // Act
    List<CrazySupplierDTO.Response> results = crazySupplierClient.searchFlights(
            "INVALID",
            "LAX",
            LocalDate.of(2025, 10, 25),
            LocalDate.of(2025, 10, 26)
    );

    // Assert
    assertThat(results).isNotNull();
    assertThat(results).isEmpty();

    // Verify only ONE request was made (no retry on 4xx)
    verify(1, postRequestedFor(urlEqualTo("/flights")));
  }

  @Test
  @DisplayName("Should return empty list on 404 not found")
  void testSearchFlights_NotFound_ReturnsEmptyList() {
    // Arrange
    stubFor(post(urlEqualTo("/flights"))
            .willReturn(aResponse()
                    .withStatus(404)
                    .withBody("Not Found")));

    // Act
    List<CrazySupplierDTO.Response> results = crazySupplierClient.searchFlights(
            "JFK",
            "XXX",
            LocalDate.of(2025, 10, 25),
            LocalDate.of(2025, 10, 26)
    );

    // Assert
    assertThat(results).isNotNull();
    assertThat(results).isEmpty();

    verify(1, postRequestedFor(urlEqualTo("/flights")));
  }

  @Test
  @DisplayName("Should retry on 5xx server error and eventually return empty list")
  @Disabled
  void testSearchFlights_ServerError_RetriesAndReturnsEmpty() {
    // Arrange - All attempts return 500
    stubFor(post(urlEqualTo("/flights"))
            .willReturn(aResponse()
                    .withStatus(500)
                    .withBody("Internal Server Error")));

    // Act
    List<CrazySupplierDTO.Response> results = crazySupplierClient.searchFlights(
            "JFK",
            "LAX",
            LocalDate.of(2025, 10, 25),
            LocalDate.of(2025, 10, 26)
    );

    // Assert
    assertThat(results).isNotNull();
    assertThat(results).isEmpty();

    // Verify retries occurred (3 attempts: initial + 2 retries)
    verify(3, postRequestedFor(urlEqualTo("/flights")));
  }

  @Test
  @DisplayName("Should succeed after retry on transient server error")
  @Disabled
  void testSearchFlights_SuccessAfterRetry() throws Exception {
    // Arrange
    List<CrazySupplierDTO.Response> mockResponses = Collections.singletonList(
            CrazySupplierDTO.Response.builder()
                    .carrier("United Airlines")
                    .basePrice(300.00)
                    .tax(60.00)
                    .departureAirportName("JFK")
                    .arrivalAirportName("LAX")
                    .outboundDateTime(LocalDateTime.of(2025, 10, 25, 10, 30))
                    .inboundDateTime(LocalDateTime.of(2025, 10, 25, 13, 45))
                    .build()
    );

    String jsonResponse = objectMapper.writeValueAsString(mockResponses);

    // First two attempts fail, third succeeds
    stubFor(post(urlEqualTo("/flights"))
            .inScenario("Retry Scenario")
            .whenScenarioStateIs("Started")
            .willReturn(aResponse()
                    .withStatus(500)
                    .withBody("Internal Server Error"))
            .willSetStateTo("First Retry"));

    stubFor(post(urlEqualTo("/flights"))
            .inScenario("Retry Scenario")
            .whenScenarioStateIs("First Retry")
            .willReturn(aResponse()
                    .withStatus(503)
                    .withBody("Service Unavailable"))
            .willSetStateTo("Second Retry"));

    stubFor(post(urlEqualTo("/flights"))
            .inScenario("Retry Scenario")
            .whenScenarioStateIs("Second Retry")
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(jsonResponse)));

    // Act
    List<CrazySupplierDTO.Response> results = crazySupplierClient.searchFlights(
            "JFK",
            "LAX",
            LocalDate.of(2025, 10, 25),
            LocalDate.of(2025, 10, 26)
    );

    // Assert
    assertThat(results).isNotNull();
    assertThat(results).hasSize(1);
    assertThat(results.getFirst().getCarrier()).isEqualTo("United Airlines");

    // Verify 3 attempts were made
    verify(3, postRequestedFor(urlEqualTo("/flights")));
  }

  // ==================== RESPONSE VALIDATION ====================

  @Test
  @DisplayName("Should filter out invalid responses")
  void testSearchFlights_FiltersInvalidResponses() throws Exception {
    // Arrange - Mix of valid and invalid responses
    List<CrazySupplierDTO.Response> mockResponses = Arrays.asList(
            // Valid response
            CrazySupplierDTO.Response.builder()
                    .carrier("Lufthansa")
                    .basePrice(250.00)
                    .tax(50.00)
                    .departureAirportName("JFK")
                    .arrivalAirportName("FRA")
                    .outboundDateTime(LocalDateTime.of(2025, 10, 25, 10, 30))
                    .inboundDateTime(LocalDateTime.of(2025, 10, 25, 22, 45))
                    .build(),
            // Invalid - missing carrier
            CrazySupplierDTO.Response.builder()
                    .basePrice(250.00)
                    .tax(50.00)
                    .departureAirportName("JFK")
                    .arrivalAirportName("FRA")
                    .outboundDateTime(LocalDateTime.of(2025, 10, 25, 10, 30))
                    .inboundDateTime(LocalDateTime.of(2025, 10, 25, 22, 45))
                    .build(),
            // Invalid - negative price
            CrazySupplierDTO.Response.builder()
                    .carrier("Air France")
                    .basePrice(-100.00)
                    .tax(50.00)
                    .departureAirportName("JFK")
                    .arrivalAirportName("FRA")
                    .outboundDateTime(LocalDateTime.of(2025, 10, 25, 10, 30))
                    .inboundDateTime(LocalDateTime.of(2025, 10, 25, 22, 45))
                    .build(),
            // Invalid - invalid airport code
            CrazySupplierDTO.Response.builder()
                    .carrier("Delta")
                    .basePrice(250.00)
                    .tax(50.00)
                    .departureAirportName("INVALID")
                    .arrivalAirportName("FRA")
                    .outboundDateTime(LocalDateTime.of(2025, 10, 25, 10, 30))
                    .inboundDateTime(LocalDateTime.of(2025, 10, 25, 22, 45))
                    .build(),
            // Valid response
            CrazySupplierDTO.Response.builder()
                    .carrier("British Airways")
                    .basePrice(300.00)
                    .tax(60.00)
                    .departureAirportName("JFK")
                    .arrivalAirportName("LHR")
                    .outboundDateTime(LocalDateTime.of(2025, 10, 25, 12, 0))
                    .inboundDateTime(LocalDateTime.of(2025, 10, 26, 0, 15))
                    .build()
    );

    String jsonResponse = objectMapper.writeValueAsString(mockResponses);

    stubFor(post(urlEqualTo("/flights"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(jsonResponse)));

    // Act
    List<CrazySupplierDTO.Response> results = crazySupplierClient.searchFlights(
            "JFK",
            "FRA",
            LocalDate.of(2025, 10, 25),
            LocalDate.of(2025, 10, 26)
    );

    // Assert - Only valid responses should be returned
    assertThat(results).isNotNull();
    assertThat(results).hasSize(2); // Only 2 valid out of 5
    assertThat(results.get(0).getCarrier()).isEqualTo("Lufthansa");
    assertThat(results.get(1).getCarrier()).isEqualTo("British Airways");
  }

  @Test
  @DisplayName("Should calculate total fare correctly")
  void testResponseDTO_TotalFareCalculation() {
    // Arrange
    CrazySupplierDTO.Response response = CrazySupplierDTO.Response.builder()
            .carrier("Test Airline")
            .basePrice(200.00)
            .tax(40.00)
            .build();

    // Act
    Double totalFare = response.getTotalFare();

    // Assert
    assertThat(totalFare).isEqualTo(240.00);
  }

  @Test
  @DisplayName("Should handle null prices in total fare calculation")
  void testResponseDTO_TotalFareCalculation_NullValues() {
    // Arrange
    CrazySupplierDTO.Response response = CrazySupplierDTO.Response.builder()
            .carrier("Test Airline")
            .build();

    // Act
    Double totalFare = response.getTotalFare();

    // Assert
    assertThat(totalFare).isEqualTo(0.0);
  }

  // ==================== CONFIGURATION TESTS ====================

  @Test
  @DisplayName("Should return empty list when integration is disabled")
  void testSearchFlights_Disabled_ReturnsEmptyList() {
    // Arrange
    CrazySupplierClient disabledClient = new CrazySupplierClient(
            RestClient.builder(),
            baseUrl,
            5000,
            false  // Disabled
    );

    // Act
    List<CrazySupplierDTO.Response> results = disabledClient.searchFlights(
            "JFK",
            "LAX",
            LocalDate.of(2025, 10, 25),
            LocalDate.of(2025, 10, 26)
    );

    // Assert
    assertThat(results).isNotNull();
    assertThat(results).isEmpty();

    // Verify no requests were made to the API
    verify(0, postRequestedFor(urlEqualTo("/flights")));
  }

  @Test
  @DisplayName("Should check if integration is enabled")
  void testIsEnabled() {
    // Assert
    assertThat(crazySupplierClient.isEnabled()).isTrue();

    // Test the disabled client
    CrazySupplierClient disabledClient = new CrazySupplierClient(
            RestClient.builder(),
            baseUrl,
            5000,
            false
    );
    assertThat(disabledClient.isEnabled()).isFalse();
  }

  // ==================== NETWORK ERROR SCENARIOS ====================

  @Test
  @DisplayName("Should handle connection refused gracefully")
  @Disabled
  void testSearchFlights_ConnectionRefused_ReturnsEmpty() {
    // Arrange - Stop the server to simulate the connection refused
    wireMockServer.stop();

    // Act
    List<CrazySupplierDTO.Response> results = crazySupplierClient.searchFlights(
            "JFK",
            "LAX",
            LocalDate.of(2025, 10, 25),
            LocalDate.of(2025, 10, 26)
    );

    // Assert - Should return an empty list on network error
    assertThat(results).isNotNull();
    assertThat(results).isEmpty();
  }

  @Test
  @DisplayName("Should handle timeout gracefully")
  void testSearchFlights_Timeout_ReturnsEmpty() {
    // Arrange - Create client with very short timeout
    CrazySupplierClient clientWithShortTimeout = new CrazySupplierClient(
            RestClient.builder(),
            baseUrl,
            100,  // 100ms timeout
            true
    );

    // Mock slow response (longer than timeout)
    stubFor(post(urlEqualTo("/flights"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("[]")
                    .withFixedDelay(500))); // 500ms delay

    // Act
    List<CrazySupplierDTO.Response> results = clientWithShortTimeout.searchFlights(
            "JFK",
            "LAX",
            LocalDate.of(2025, 10, 25),
            LocalDate.of(2025, 10, 26)
    );

    // Assert - Should return an empty list on timeout
    assertThat(results).isNotNull();
    assertThat(results).isEmpty();
  }
}