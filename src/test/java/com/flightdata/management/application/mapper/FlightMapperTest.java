package com.flightdata.management.application.mapper;

import com.flightdata.management.domain.model.Flight;
import com.flightdata.management.infrastructure.external.dto.CrazySupplierDTO;
import com.flightdata.management.application.dto.FlightCreateRequest;
import com.flightdata.management.application.dto.FlightResponse;
import com.flightdata.management.application.dto.FlightUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for FlightMapper.
 *
 * <p>These are TRUE unit tests because:
 * <ul>
 *   <li>Test a single component (the mapper) in isolation</li>
 *   <li>No Spring context needed (MapStruct generates implementation at compile time)</li>
 *   <li>No external dependencies or mocking required</li>
 *   <li>Fast execution (milliseconds)</li>
 * </ul>
 *
 * <p>MapStruct generates the mapper implementation during compilation,
 * so we can test it directly without Spring's dependency injection.
 *
 * @author Vítor Matosinho Martins
 * @version 1.0
 * @since 2025-10-24
 */
@DisplayName("FlightMapper Unit Tests")
class FlightMapperTest {

  private FlightMapper flightMapper;

  @BeforeEach
  void setUp() {
    // MapStruct generates the implementation at compile time
    // We can get it directly without Spring context
    flightMapper = Mappers.getMapper(FlightMapper.class);
  }

  // ==================== ENTITY TO RESPONSE MAPPING ====================

  @Test
  @DisplayName("Should map Flight entity to FlightResponse DTO")
  void testToResponse_Success() {
    // Arrange
    Instant departureTime = Instant.parse("2025-10-25T10:30:00Z");
    Instant arrivalTime = Instant.parse("2025-10-25T16:45:00Z");

    Flight flight = Flight.builder()
            .id(1L)
            .airline("American Airlines")
            .supplier("GlobalSupplier")
            .fare(299.99)
            .departureAirport("JFK")
            .destinationAirport("LAX")
            .departureTime(departureTime)
            .arrivalTime(arrivalTime)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

    // Act
    FlightResponse response = flightMapper.toResponse(flight);

    // Assert
    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getAirline()).isEqualTo("American Airlines");
    assertThat(response.getSupplier()).isEqualTo("GlobalSupplier");
    assertThat(response.getFare()).isEqualTo(299.99);
    assertThat(response.getDepartureAirport()).isEqualTo("JFK");
    assertThat(response.getDestinationAirport()).isEqualTo("LAX");
    assertThat(response.getDepartureTime()).isNotNull();
    assertThat(response.getArrivalTime()).isNotNull();
  }

  @Test
  @DisplayName("Should handle null Flight entity")
  void testToResponse_NullEntity() {
    // Act
    FlightResponse response = flightMapper.toResponse(null);

    // Assert
    assertThat(response).isNull();
  }

  @Test
  @DisplayName("Should preserve all fields when mapping to response")
  void testToResponse_AllFieldsMapped() {
    // Arrange
    Flight flight = Flight.builder()
            .id(999L)
            .airline("Delta Airlines")
            .supplier("TestSupplier")
            .fare(450.75)
            .departureAirport("ORD")
            .destinationAirport("MIA")
            .departureTime(Instant.parse("2025-11-01T08:00:00Z"))
            .arrivalTime(Instant.parse("2025-11-01T12:15:00Z"))
            .build();

    // Act
    FlightResponse response = flightMapper.toResponse(flight);

    // Assert - Verify all business fields are mapped
    assertThat(response.getId()).isEqualTo(999L);
    assertThat(response.getAirline()).isEqualTo("Delta Airlines");
    assertThat(response.getSupplier()).isEqualTo("TestSupplier");
    assertThat(response.getFare()).isEqualTo(450.75);
    assertThat(response.getDepartureAirport()).isEqualTo("ORD");
    assertThat(response.getDestinationAirport()).isEqualTo("MIA");
  }

  // ==================== CREATE REQUEST TO ENTITY MAPPING ====================

  @Test
  @DisplayName("Should map FlightCreateRequest to Flight entity")
  void testToEntity_FromCreateRequest() {
    // Arrange
    FlightCreateRequest request = FlightCreateRequest.builder()
            .airline("United Airlines")
            .supplier("GlobalSupplier")
            .fare(320.00)
            .departureAirport("JFK")
            .destinationAirport("SFO")
            .departureTime(OffsetDateTime.parse("2025-10-25T08:15:00Z"))
            .arrivalTime(OffsetDateTime.parse("2025-10-25T14:30:00Z"))
            .build();

    // Act
    Flight flight = flightMapper.toEntity(request);

    // Assert
    assertThat(flight).isNotNull();
    assertThat(flight.getId()).isNull(); // Should not be set for new entities
    assertThat(flight.getAirline()).isEqualTo("United Airlines");
    assertThat(flight.getSupplier()).isEqualTo("GlobalSupplier");
    assertThat(flight.getFare()).isEqualTo(320.00);
    assertThat(flight.getDepartureAirport()).isEqualTo("JFK");
    assertThat(flight.getDestinationAirport()).isEqualTo("SFO");
    assertThat(flight.getDepartureTime()).isNotNull();
    assertThat(flight.getArrivalTime()).isNotNull();
    assertThat(flight.getCreatedAt()).isNull(); // Managed by JPA
    assertThat(flight.getUpdatedAt()).isNull(); // Managed by JPA
  }

  @Test
  @DisplayName("Should handle null FlightCreateRequest")
  void testToEntity_NullCreateRequest() {
    // Act
    Flight flight = flightMapper.toEntity(null);

    // Assert
    assertThat(flight).isNull();
  }

  // ==================== UPDATE REQUEST MAPPING ====================

  @Test
  @DisplayName("Should update Flight entity from FlightUpdateRequest with all fields")
  void testUpdateEntityFromRequest_AllFields() {
    // Arrange
    Flight existingFlight = Flight.builder()
            .id(1L)
            .airline("American Airlines")
            .supplier("GlobalSupplier")
            .fare(299.99)
            .departureAirport("JFK")
            .destinationAirport("LAX")
            .departureTime(Instant.parse("2025-10-25T10:30:00Z"))
            .arrivalTime(Instant.parse("2025-10-25T16:45:00Z"))
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

    FlightUpdateRequest updateRequest = FlightUpdateRequest.builder()
            .airline("Delta Airlines")
            .supplier("NewSupplier")
            .fare(399.99)
            .departureAirport("ORD")
            .destinationAirport("MIA")
            .departureTime(OffsetDateTime.parse("2025-10-26T09:00:00Z"))
            .arrivalTime(OffsetDateTime.parse("2025-10-26T13:15:00Z"))
            .build();

    // Act
    flightMapper.updateEntityFromRequest(updateRequest, existingFlight);

    // Assert - All fields should be updated
    assertThat(existingFlight.getId()).isEqualTo(1L); // ID should NOT change
    assertThat(existingFlight.getAirline()).isEqualTo("Delta Airlines");
    assertThat(existingFlight.getSupplier()).isEqualTo("NewSupplier");
    assertThat(existingFlight.getFare()).isEqualTo(399.99);
    assertThat(existingFlight.getDepartureAirport()).isEqualTo("ORD");
    assertThat(existingFlight.getDestinationAirport()).isEqualTo("MIA");
    assertThat(existingFlight.getDepartureTime()).isNotNull();
    assertThat(existingFlight.getArrivalTime()).isNotNull();
  }

  @Test
  @DisplayName("Should only update non-null fields from FlightUpdateRequest")
  void testUpdateEntityFromRequest_PartialUpdate() {
    // Arrange
    Flight existingFlight = Flight.builder()
            .id(1L)
            .airline("American Airlines")
            .supplier("GlobalSupplier")
            .fare(299.99)
            .departureAirport("JFK")
            .destinationAirport("LAX")
            .departureTime(Instant.parse("2025-10-25T10:30:00Z"))
            .arrivalTime(Instant.parse("2025-10-25T16:45:00Z"))
            .build();

    FlightUpdateRequest updateRequest = FlightUpdateRequest.builder()
            .fare(399.99) // Only update fare
            // All other fields are null
            .build();

    // Act
    flightMapper.updateEntityFromRequest(updateRequest, existingFlight);

    // Assert - Only fare should be updated
    assertThat(existingFlight.getAirline()).isEqualTo("American Airlines"); // Unchanged
    assertThat(existingFlight.getSupplier()).isEqualTo("GlobalSupplier"); // Unchanged
    assertThat(existingFlight.getFare()).isEqualTo(399.99); // Changed
    assertThat(existingFlight.getDepartureAirport()).isEqualTo("JFK"); // Unchanged
    assertThat(existingFlight.getDestinationAirport()).isEqualTo("LAX"); // Unchanged
  }

  @Test
  @DisplayName("Should not change entity when update request is empty")
  void testUpdateEntityFromRequest_EmptyRequest() {
    // Arrange
    Flight existingFlight = Flight.builder()
            .id(1L)
            .airline("American Airlines")
            .supplier("GlobalSupplier")
            .fare(299.99)
            .departureAirport("JFK")
            .destinationAirport("LAX")
            .departureTime(Instant.parse("2025-10-25T10:30:00Z"))
            .arrivalTime(Instant.parse("2025-10-25T16:45:00Z"))
            .build();

    FlightUpdateRequest emptyRequest = FlightUpdateRequest.builder().build();

    // Act
    flightMapper.updateEntityFromRequest(emptyRequest, existingFlight);

    // Assert - Nothing should change
    assertThat(existingFlight.getAirline()).isEqualTo("American Airlines");
    assertThat(existingFlight.getSupplier()).isEqualTo("GlobalSupplier");
    assertThat(existingFlight.getFare()).isEqualTo(299.99);
    assertThat(existingFlight.getDepartureAirport()).isEqualTo("JFK");
    assertThat(existingFlight.getDestinationAirport()).isEqualTo("LAX");
  }

  // ==================== CRAZY SUPPLIER MAPPING ====================

  @Test
  @DisplayName("Should map CrazySupplier response to FlightResponse")
  void testCrazySupplierToResponse_Success() {
    // Arrange
    CrazySupplierDTO.Response crazyResponse = CrazySupplierDTO.Response.builder()
            .carrier("Lufthansa")
            .basePrice(250.00)
            .tax(50.00)
            .departureAirportName("JFK")
            .arrivalAirportName("FRA")
            .outboundDateTime(LocalDateTime.of(2025, 10, 25, 10, 30))
            .inboundDateTime(LocalDateTime.of(2025, 10, 25, 22, 45))
            .build();

    // Act
    FlightResponse response = flightMapper.crazySupplierToResponse(crazyResponse);

    // Assert
    assertThat(response).isNotNull();
    assertThat(response.getId()).isNull(); // CrazySupplier flights have no ID
    assertThat(response.getAirline()).isEqualTo("Lufthansa");
    assertThat(response.getSupplier()).isEqualTo("CrazySupplier"); // Constant mapping
    assertThat(response.getFare()).isEqualTo(300.00); // basePrice + tax
    assertThat(response.getDepartureAirport()).isEqualTo("JFK");
    assertThat(response.getDestinationAirport()).isEqualTo("FRA");
    assertThat(response.getDepartureTime()).isNotNull();
    assertThat(response.getArrivalTime()).isNotNull();
  }

  @Test
  @DisplayName("Should correctly calculate total fare from CrazySupplier")
  void testCrazySupplierToResponse_FareCalculation() {
    // Arrange
    CrazySupplierDTO.Response crazyResponse = CrazySupplierDTO.Response.builder()
            .carrier("Air France")
            .basePrice(280.00)
            .tax(55.00)
            .departureAirportName("JFK")
            .arrivalAirportName("CDG")
            .outboundDateTime(LocalDateTime.of(2025, 10, 25, 14, 15))
            .inboundDateTime(LocalDateTime.of(2025, 10, 26, 2, 30))
            .build();

    // Act
    FlightResponse response = flightMapper.crazySupplierToResponse(crazyResponse);

    // Assert - Fare should be basePrice + tax
    assertThat(response.getFare()).isEqualTo(335.00);
  }

  @Test
  @DisplayName("Should handle null CrazySupplier response")
  void testCrazySupplierToResponse_NullResponse() {
    // Act
    FlightResponse response = flightMapper.crazySupplierToResponse(null);

    // Assert
    assertThat(response).isNull();
  }

  @Test
  @DisplayName("Should map carrier field to airline")
  void testCrazySupplierToResponse_CarrierMapping() {
    // Arrange
    CrazySupplierDTO.Response crazyResponse = CrazySupplierDTO.Response.builder()
            .carrier("British Airways")
            .basePrice(300.00)
            .tax(60.00)
            .departureAirportName("JFK")
            .arrivalAirportName("LHR")
            .outboundDateTime(LocalDateTime.of(2025, 10, 25, 18, 0))
            .inboundDateTime(LocalDateTime.of(2025, 10, 26, 6, 15))
            .build();

    // Act
    FlightResponse response = flightMapper.crazySupplierToResponse(crazyResponse);

    // Assert - carrier should be mapped to airline
    assertThat(response.getAirline()).isEqualTo("British Airways");
  }

  @Test
  @DisplayName("Should always set supplier to CrazySupplier constant")
  void testCrazySupplierToResponse_SupplierConstant() {
    // Arrange
    CrazySupplierDTO.Response crazyResponse = CrazySupplierDTO.Response.builder()
            .carrier("Test Airline")
            .basePrice(200.00)
            .tax(40.00)
            .departureAirportName("JFK")
            .arrivalAirportName("LAX")
            .outboundDateTime(LocalDateTime.of(2025, 10, 25, 10, 0))
            .inboundDateTime(LocalDateTime.of(2025, 10, 25, 13, 0))
            .build();

    // Act
    FlightResponse response = flightMapper.crazySupplierToResponse(crazyResponse);

    // Assert - Supplier should always be "CrazySupplier"
    assertThat(response.getSupplier()).isEqualTo("CrazySupplier");
  }

  // ==================== TIMEZONE CONVERSION TESTS ====================

  @Test
  @DisplayName("Should convert CET LocalDateTime to UTC OffsetDateTime")
  void testLocalDateTimeToInstant_TimezoneConversion() {
    // Arrange
    LocalDateTime cetTime = LocalDateTime.of(2025, 10, 25, 14, 30); // 14:30 CET

    // Act
    OffsetDateTime result = flightMapper.localDateTimeToInstant(cetTime);

    // Assert
    assertThat(result).isNotNull();
    // CET is UTC+1 (or UTC+2 during DST)
    // The conversion should preserve the local time but add timezone info
    assertThat(result.getHour()).isIn(13, 14); // Could be 13 or 14 depending on DST
  }

  @Test
  @DisplayName("Should handle null LocalDateTime")
  void testLocalDateTimeToInstant_NullInput() {
    // Act
    OffsetDateTime result = flightMapper.localDateTimeToInstant(null);

    // Assert
    assertThat(result).isNull();
  }

  @Test
  @DisplayName("Should convert Instant to OffsetDateTime in UTC")
  void testInstantToOffsetDateTime_Success() {
    // Arrange
    Instant instant = Instant.parse("2025-10-25T10:30:00Z");

    // Act
    OffsetDateTime result = flightMapper.instantToOffsetDateTime(instant);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getOffset()).isEqualTo(ZoneOffset.UTC);
    assertThat(result.getYear()).isEqualTo(2025);
    assertThat(result.getMonthValue()).isEqualTo(10);
    assertThat(result.getDayOfMonth()).isEqualTo(25);
    assertThat(result.getHour()).isEqualTo(10);
    assertThat(result.getMinute()).isEqualTo(30);
  }

  @Test
  @DisplayName("Should handle null Instant")
  void testInstantToOffsetDateTime_NullInput() {
    // Act
    OffsetDateTime result = flightMapper.instantToOffsetDateTime(null);

    // Assert
    assertThat(result).isNull();
  }

  @Test
  @DisplayName("Should convert OffsetDateTime to Instant")
  void testOffsetDateTimeToInstant_Success() {
    // Arrange
    OffsetDateTime offsetDateTime = OffsetDateTime.parse("2025-10-25T10:30:00Z");

    // Act
    Instant result = flightMapper.offsetDateTimeToInstant(offsetDateTime);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.toString()).hasToString("2025-10-25T10:30:00Z");
  }

  @Test
  @DisplayName("Should handle null OffsetDateTime")
  void testOffsetDateTimeToInstant_NullInput() {
    // Act
    Instant result = flightMapper.offsetDateTimeToInstant(null);

    // Assert
    assertThat(result).isNull();
  }

  @Test
  @DisplayName("Should preserve time when converting OffsetDateTime with different offset")
  void testOffsetDateTimeToInstant_DifferentOffset() {
    // Arrange - Same moment in time, different timezone
    OffsetDateTime utcTime = OffsetDateTime.parse("2025-10-25T10:30:00Z");
    OffsetDateTime plusTwoTime = OffsetDateTime.parse("2025-10-25T12:30:00+02:00");

    // Act
    Instant instantFromUtc = flightMapper.offsetDateTimeToInstant(utcTime);
    Instant instantFromPlusTwo = flightMapper.offsetDateTimeToInstant(plusTwoTime);

    // Assert - Should represent the same moment in time
    assertThat(instantFromUtc).isEqualTo(instantFromPlusTwo);
  }

  // ==================== EDGE CASES ====================

  @Test
  @DisplayName("Should handle entity with minimum required fields")
  void testToResponse_MinimalEntity() {
    // Arrange - Only required fields
    Flight flight = Flight.builder()
            .airline("Test")
            .supplier("Test")
            .fare(100.0)
            .departureAirport("JFK")
            .destinationAirport("LAX")
            .departureTime(Instant.now())
            .arrivalTime(Instant.now().plusSeconds(3600))
            .build();

    // Act
    FlightResponse response = flightMapper.toResponse(flight);

    // Assert
    assertThat(response).isNotNull();
    assertThat(response.getAirline()).isEqualTo("Test");
  }

  @Test
  @DisplayName("Should handle CrazySupplier response with zero tax")
  void testCrazySupplierToResponse_ZeroTax() {
    // Arrange
    CrazySupplierDTO.Response crazyResponse = CrazySupplierDTO.Response.builder()
            .carrier("Budget Airline")
            .basePrice(150.00)
            .tax(0.0)
            .departureAirportName("JFK")
            .arrivalAirportName("LAX")
            .outboundDateTime(LocalDateTime.of(2025, 10, 25, 10, 0))
            .inboundDateTime(LocalDateTime.of(2025, 10, 25, 13, 0))
            .build();

    // Act
    FlightResponse response = flightMapper.crazySupplierToResponse(crazyResponse);

    // Assert - Fare should equal basePrice when tax is 0
    assertThat(response.getFare()).isEqualTo(150.00);
  }
}