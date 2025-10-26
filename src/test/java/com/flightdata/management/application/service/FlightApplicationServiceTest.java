package com.flightdata.management.application.service;

import com.flightdata.management.application.dto.*;
import com.flightdata.management.application.mapper.FlightMapper;
import com.flightdata.management.domain.model.Flight;
import com.flightdata.management.domain.service.FlightDomainService;
import com.flightdata.management.infrastructure.external.client.CrazySupplierClient;
import com.flightdata.management.infrastructure.external.dto.CrazySupplierDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FlightApplicationService.
 *
 * <p>These are TRUE unit tests because:
 * <ul>
 *   <li>All dependencies are mocked (DomainService, Mapper, Client)</li>
 *   <li>No Spring context needed</li>
 *   <li>Tests the orchestration logic in isolation</li>
 *   <li>Fast execution (milliseconds)</li>
 * </ul>
 *
 * <p>What we test:
 * <ul>
 *   <li>CRUD operations orchestration</li>
 *   <li>DTO ↔ Entity mapping coordination</li>
 *   <li>Multiple data source combination (DB + CrazySupplier)</li>
 *   <li>Error handling and exception propagation</li>
 *   <li>Timezone conversions for external API</li>
 *   <li>FlightRecord and FlightTimeRange usage</li>
 * </ul>
 *
 * @author Vítor Matosinho Martins
 * @version 1.0
 * @since 2025-10-26
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FlightApplicationService Unit Tests")
class FlightApplicationServiceTest {

  @Mock
  private FlightDomainService flightDomainService;

  @Mock
  private FlightMapper flightMapper;

  @Mock
  private CrazySupplierClient crazySupplierClient;

  @InjectMocks
  private FlightApplicationService flightApplicationService;

  private Flight testFlight;
  private FlightResponse testFlightResponse;
  private Instant departureTime;
  private Instant arrivalTime;

  @BeforeEach
  void setUp() {
    departureTime = Instant.parse("2025-10-25T10:30:00Z");
    arrivalTime = Instant.parse("2025-10-25T16:45:00Z");

    testFlight = Flight.builder()
            .id(1L)
            .airline("American Airlines")
            .supplier("GlobalSupplier")
            .fare(299.99)
            .departureAirport("JFK")
            .destinationAirport("LAX")
            .departureTime(departureTime)
            .arrivalTime(arrivalTime)
            .build();

    testFlightResponse = FlightResponse.builder()
            .id(1L)
            .airline("American Airlines")
            .supplier("GlobalSupplier")
            .fare(299.99)
            .departureAirport("JFK")
            .destinationAirport("LAX")
            .departureTime(OffsetDateTime.ofInstant(departureTime, ZoneOffset.UTC))
            .arrivalTime(OffsetDateTime.ofInstant(arrivalTime, ZoneOffset.UTC))
            .build();
  }

  // ==================== CREATE FLIGHT TESTS ====================

  @Test
  @DisplayName("Should successfully create flight using FlightRecord")
  void testCreateFlight_Success() {
    // Arrange
    FlightCreateRequest request = FlightCreateRequest.builder()
            .airline("American Airlines")
            .supplier("GlobalSupplier")
            .fare(299.99)
            .departureAirport("JFK")
            .destinationAirport("LAX")
            .departureTime(OffsetDateTime.ofInstant(departureTime, ZoneOffset.UTC))
            .arrivalTime(OffsetDateTime.ofInstant(arrivalTime, ZoneOffset.UTC))
            .build();

    ArgumentCaptor<FlightRecord> recordCaptor = ArgumentCaptor.forClass(FlightRecord.class);

    when(flightDomainService.createFlight(recordCaptor.capture()))
            .thenReturn(testFlight);
    when(flightMapper.toResponse(testFlight)).thenReturn(testFlightResponse);

    // Act
    FlightResponse result = flightApplicationService.createFlight(request);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getAirline()).isEqualTo("American Airlines");
    assertThat(result.getFare()).isEqualTo(299.99);

    // Verify FlightRecord was created correctly
    FlightRecord capturedRecord = recordCaptor.getValue();
    assertThat(capturedRecord.airline()).isEqualTo("American Airlines");
    assertThat(capturedRecord.supplier()).isEqualTo("GlobalSupplier");
    assertThat(capturedRecord.fare()).isEqualTo(299.99);
    assertThat(capturedRecord.departureAirport()).isEqualTo("JFK");
    assertThat(capturedRecord.destinationAirport()).isEqualTo("LAX");

    verify(flightDomainService, times(1)).createFlight(any(FlightRecord.class));
    verify(flightMapper, times(1)).toResponse(testFlight);
  }

  @Test
  @DisplayName("Should propagate domain exception on create failure")
  void testCreateFlight_DomainException() {
    // Arrange
    FlightCreateRequest request = FlightCreateRequest.builder()
            .airline("American Airlines")
            .supplier("GlobalSupplier")
            .fare(299.99)
            .departureAirport("JFK")
            .destinationAirport("LAX")
            .departureTime(OffsetDateTime.ofInstant(departureTime, ZoneOffset.UTC))
            .arrivalTime(OffsetDateTime.ofInstant(arrivalTime, ZoneOffset.UTC))
            .build();

    when(flightDomainService.createFlight(any(FlightRecord.class)))
            .thenThrow(new IllegalArgumentException("Invalid airport code"));

    // Act & Assert
    assertThatThrownBy(() -> flightApplicationService.createFlight(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid airport code");

    verify(flightMapper, never()).toResponse(any());
  }

  // ==================== UPDATE FLIGHT TESTS ====================

  @Test
  @DisplayName("Should successfully update flight with all fields using FlightRecord")
  void testUpdateFlight_AllFields() {
    // Arrange
    FlightUpdateRequest request = FlightUpdateRequest.builder()
            .airline("Delta Airlines")
            .supplier("NewSupplier")
            .fare(399.99)
            .departureAirport("ORD")
            .destinationAirport("MIA")
            .departureTime(OffsetDateTime.ofInstant(departureTime, ZoneOffset.UTC))
            .arrivalTime(OffsetDateTime.ofInstant(arrivalTime, ZoneOffset.UTC))
            .build();

    Flight updatedFlight = Flight.builder()
            .id(1L)
            .airline("Delta Airlines")
            .supplier("NewSupplier")
            .fare(399.99)
            .departureAirport("ORD")
            .destinationAirport("MIA")
            .departureTime(departureTime)
            .arrivalTime(arrivalTime)
            .build();

    FlightResponse updatedResponse = FlightResponse.builder()
            .id(1L)
            .airline("Delta Airlines")
            .fare(399.99)
            .build();

    ArgumentCaptor<FlightRecord> recordCaptor = ArgumentCaptor.forClass(FlightRecord.class);

    when(flightDomainService.updateFlight(eq(1L), recordCaptor.capture()))
            .thenReturn(updatedFlight);
    when(flightMapper.toResponse(updatedFlight)).thenReturn(updatedResponse);

    // Act
    FlightResponse result = flightApplicationService.updateFlight(1L, request);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getAirline()).isEqualTo("Delta Airlines");

    // Verify FlightRecord was created correctly
    FlightRecord capturedRecord = recordCaptor.getValue();
    assertThat(capturedRecord.airline()).isEqualTo("Delta Airlines");
    assertThat(capturedRecord.supplier()).isEqualTo("NewSupplier");
    assertThat(capturedRecord.fare()).isEqualTo(399.99);

    verify(flightDomainService, times(1)).updateFlight(eq(1L), any(FlightRecord.class));
    verify(flightMapper, times(1)).toResponse(updatedFlight);
  }

  @Test
  @DisplayName("Should update flight with partial fields (nulls preserved in FlightRecord)")
  void testUpdateFlight_PartialUpdate() {
    // Arrange - Only fare is updated
    FlightUpdateRequest request = FlightUpdateRequest.builder()
            .fare(399.99)
            .build();

    ArgumentCaptor<FlightRecord> recordCaptor = ArgumentCaptor.forClass(FlightRecord.class);

    when(flightDomainService.updateFlight(eq(1L), recordCaptor.capture()))
            .thenReturn(testFlight);
    when(flightMapper.toResponse(testFlight)).thenReturn(testFlightResponse);

    // Act
    FlightResponse result = flightApplicationService.updateFlight(1L, request);

    // Assert
    assertThat(result).isNotNull();

    // Verify FlightRecord has null fields except fare
    FlightRecord capturedRecord = recordCaptor.getValue();
    assertThat(capturedRecord.airline()).isNull();
    assertThat(capturedRecord.supplier()).isNull();
    assertThat(capturedRecord.fare()).isEqualTo(399.99);
    assertThat(capturedRecord.departureAirport()).isNull();
    assertThat(capturedRecord.destinationAirport()).isNull();
    assertThat(capturedRecord.departureTime()).isNull();
    assertThat(capturedRecord.arrivalTime()).isNull();

    verify(flightDomainService, times(1)).updateFlight(eq(1L), any(FlightRecord.class));
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent flight")
  void testUpdateFlight_NotFound() {
    // Arrange
    FlightUpdateRequest request = FlightUpdateRequest.builder()
            .fare(399.99)
            .build();

    when(flightDomainService.updateFlight(eq(999L), any(FlightRecord.class)))
            .thenThrow(new FlightDomainService.FlightNotFoundException(
                    "Flight not found with ID: 999"));

    // Act & Assert
    assertThatThrownBy(() -> flightApplicationService.updateFlight(999L, request))
            .isInstanceOf(FlightDomainService.FlightNotFoundException.class)
            .hasMessageContaining("Flight not found with ID: 999");

    verify(flightMapper, never()).toResponse(any());
  }

  // ==================== DELETE FLIGHT TESTS ====================

  @Test
  @DisplayName("Should successfully delete flight")
  void testDeleteFlight_Success() {
    // Arrange
    doNothing().when(flightDomainService).deleteFlight(1L);

    // Act
    flightApplicationService.deleteFlight(1L);

    // Assert
    verify(flightDomainService, times(1)).deleteFlight(1L);
  }

  @Test
  @DisplayName("Should throw exception when deleting non-existent flight")
  void testDeleteFlight_NotFound() {
    // Arrange
    doThrow(new FlightDomainService.FlightNotFoundException(
            "Flight not found with ID: 999"))
            .when(flightDomainService).deleteFlight(999L);

    // Act & Assert
    assertThatThrownBy(() -> flightApplicationService.deleteFlight(999L))
            .isInstanceOf(FlightDomainService.FlightNotFoundException.class)
            .hasMessageContaining("Flight not found with ID: 999");

    verify(flightDomainService, times(1)).deleteFlight(999L);
  }

  // ==================== GET FLIGHT BY ID TESTS ====================

  @Test
  @DisplayName("Should successfully get flight by ID")
  void testGetFlightById_Success() {
    // Arrange
    when(flightDomainService.getFlightById(1L)).thenReturn(testFlight);
    when(flightMapper.toResponse(testFlight)).thenReturn(testFlightResponse);

    // Act
    FlightResponse result = flightApplicationService.getFlightById(1L);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getAirline()).isEqualTo("American Airlines");

    verify(flightDomainService, times(1)).getFlightById(1L);
    verify(flightMapper, times(1)).toResponse(testFlight);
  }

  @Test
  @DisplayName("Should throw exception when flight not found by ID")
  void testGetFlightById_NotFound() {
    // Arrange
    when(flightDomainService.getFlightById(999L))
            .thenThrow(new FlightDomainService.FlightNotFoundException(
                    "Flight not found with ID: 999"));

    // Act & Assert
    assertThatThrownBy(() -> flightApplicationService.getFlightById(999L))
            .isInstanceOf(FlightDomainService.FlightNotFoundException.class)
            .hasMessageContaining("Flight not found with ID: 999");

    verify(flightMapper, never()).toResponse(any());
  }

  // ==================== SEARCH FLIGHTS - DATABASE ONLY ====================

  @Test
  @DisplayName("Should search flights from database only (no CrazySupplier)")
  void testSearchFlights_DatabaseOnly() {
    // Arrange
    Page<Flight> flightPage = new PageImpl<>(Collections.singletonList(testFlight));
    FlightTimeRange timeRange = new FlightTimeRange(
            departureTime,
            arrivalTime,
            null,
            null
    );

    ArgumentCaptor<FlightTimeRange> timeRangeCaptor = ArgumentCaptor.forClass(FlightTimeRange.class);

    when(flightDomainService.searchFlights(
            eq("JFK"), eq("LAX"), isNull(),
            timeRangeCaptor.capture(), any(Pageable.class)
    )).thenReturn(flightPage);

    when(flightMapper.toResponse(testFlight)).thenReturn(testFlightResponse);
    when(crazySupplierClient.isEnabled()).thenReturn(false);

    // Act
    FlightSearchResponse result = flightApplicationService.searchFlights(
            "JFK", "LAX", null, timeRange, 0, 20
    );

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getFlights()).hasSize(1);
    assertThat(result.getFlights().getFirst().getId()).isEqualTo(1L);
    assertThat(result.getPagination()).isNotNull();
    assertThat(result.getPagination().getTotalElements()).isEqualTo(1);

    // Verify FlightTimeRange was passed correctly
    FlightTimeRange capturedTimeRange = timeRangeCaptor.getValue();
    assertThat(capturedTimeRange.departureFrom()).isEqualTo(departureTime);
    assertThat(capturedTimeRange.departureTo()).isEqualTo(arrivalTime);

    verify(flightDomainService, times(1)).searchFlights(
            any(), any(), any(), any(FlightTimeRange.class), any(Pageable.class)
    );
    verify(crazySupplierClient, times(1)).isEnabled();
    verify(crazySupplierClient, never()).searchFlights(any(), any(), any(), any());
  }

  @Test
  @DisplayName("Should search flights without origin/destination (no CrazySupplier)")
  void testSearchFlights_NoOriginDestination() {
    // Arrange
    Page<Flight> flightPage = new PageImpl<>(Collections.singletonList(testFlight));
    FlightTimeRange timeRange = new FlightTimeRange(null, null, null, null);

    when(flightDomainService.searchFlights(
            isNull(), isNull(), eq("American"),
            any(FlightTimeRange.class), any(Pageable.class)
    )).thenReturn(flightPage);

    when(flightMapper.toResponse(testFlight)).thenReturn(testFlightResponse);
    when(crazySupplierClient.isEnabled()).thenReturn(true);

    // Act
    FlightSearchResponse result = flightApplicationService.searchFlights(
            null, null, "American", timeRange, 0, 20
    );

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getFlights()).hasSize(1);

    verify(crazySupplierClient, times(1)).isEnabled();
    // CrazySupplier should NOT be called (missing origin/destination)
    verify(crazySupplierClient, never()).searchFlights(any(), any(), any(), any());
  }

  // ==================== SEARCH FLIGHTS - WITH CRAZY SUPPLIER ====================

  @Test
  @DisplayName("Should combine database and CrazySupplier results")
  void testSearchFlights_CombinedSources() {
    // Arrange
    Page<Flight> flightPage = new PageImpl<>(Collections.singletonList(testFlight));
    FlightTimeRange timeRange = new FlightTimeRange(
            departureTime,
            null,
            null,
            arrivalTime
    );

    CrazySupplierDTO.Response crazyResponse = CrazySupplierDTO.Response.builder()
            .carrier("Lufthansa")
            .basePrice(250.00)
            .tax(50.00)
            .departureAirportName("JFK")
            .arrivalAirportName("LAX")
            .outboundDateTime(LocalDateTime.of(2025, 10, 25, 14, 0))
            .inboundDateTime(LocalDateTime.of(2025, 10, 25, 20, 0))
            .build();

    FlightResponse crazyFlightResponse = FlightResponse.builder()
            .id(null)
            .airline("Lufthansa")
            .supplier("CrazySupplier")
            .fare(300.00)
            .departureAirport("JFK")
            .destinationAirport("LAX")
            .build();

    when(flightDomainService.searchFlights(
            eq("JFK"), eq("LAX"), isNull(),
            any(FlightTimeRange.class), any(Pageable.class)
    )).thenReturn(flightPage);

    when(flightMapper.toResponse(testFlight)).thenReturn(testFlightResponse);
    when(crazySupplierClient.isEnabled()).thenReturn(true);
    when(crazySupplierClient.searchFlights(
            eq("JFK"), eq("LAX"), any(LocalDate.class), any(LocalDate.class)
    )).thenReturn(Collections.singletonList(crazyResponse));
    when(flightMapper.crazySupplierToResponse(crazyResponse))
            .thenReturn(crazyFlightResponse);

    // Act
    FlightSearchResponse result = flightApplicationService.searchFlights(
            "JFK", "LAX", null, timeRange, 0, 20
    );

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getFlights()).hasSize(2); // 1 from DB + 1 from CrazySupplier
    assertThat(result.getFlights().get(0).getSupplier()).isEqualTo("GlobalSupplier");
    assertThat(result.getFlights().get(1).getSupplier()).isEqualTo("CrazySupplier");

    verify(crazySupplierClient, times(1)).searchFlights(
            eq("JFK"), eq("LAX"), any(LocalDate.class), any(LocalDate.class)
    );
    verify(flightMapper, times(1)).crazySupplierToResponse(crazyResponse);
  }

  @Test
  @DisplayName("Should filter CrazySupplier results by airline")
  void testSearchFlights_FilterCrazySupplierByAirline() {
    // Arrange
    Page<Flight> flightPage = new PageImpl<>(Collections.emptyList());
    FlightTimeRange timeRange = new FlightTimeRange(null, null, null, null);

    CrazySupplierDTO.Response crazyResponse1 = CrazySupplierDTO.Response.builder()
            .carrier("Lufthansa")
            .basePrice(250.00)
            .tax(50.00)
            .departureAirportName("JFK")
            .arrivalAirportName("LAX")
            .outboundDateTime(LocalDateTime.of(2025, 10, 25, 14, 0))
            .inboundDateTime(LocalDateTime.of(2025, 10, 25, 20, 0))
            .build();

    CrazySupplierDTO.Response crazyResponse2 = CrazySupplierDTO.Response.builder()
            .carrier("Air France")
            .basePrice(280.00)
            .tax(55.00)
            .departureAirportName("JFK")
            .arrivalAirportName("LAX")
            .outboundDateTime(LocalDateTime.of(2025, 10, 25, 16, 0))
            .inboundDateTime(LocalDateTime.of(2025, 10, 25, 22, 0))
            .build();

    FlightResponse lufthansaResponse = FlightResponse.builder()
            .airline("Lufthansa")
            .supplier("CrazySupplier")
            .build();

    FlightResponse airFranceResponse = FlightResponse.builder()
            .airline("Air France")
            .supplier("CrazySupplier")
            .build();

    when(flightDomainService.searchFlights(
            any(), any(), any(), any(FlightTimeRange.class), any(Pageable.class)
    )).thenReturn(flightPage);

    when(crazySupplierClient.isEnabled()).thenReturn(true);
    when(crazySupplierClient.searchFlights(any(), any(), any(), any()))
            .thenReturn(Arrays.asList(crazyResponse1, crazyResponse2));
    when(flightMapper.crazySupplierToResponse(crazyResponse1))
            .thenReturn(lufthansaResponse);
    when(flightMapper.crazySupplierToResponse(crazyResponse2))
            .thenReturn(airFranceResponse);

    // Act - Filter by "Lufthansa"
    FlightSearchResponse result = flightApplicationService.searchFlights(
            "JFK", "LAX", "Lufthansa", timeRange, 0, 20
    );

    // Assert - Only Lufthansa should be returned
    assertThat(result).isNotNull();
    assertThat(result.getFlights()).hasSize(1);
    assertThat(result.getFlights().getFirst().getAirline()).isEqualTo("Lufthansa");
  }

  @Test
  @DisplayName("Should handle CrazySupplier failure gracefully")
  void testSearchFlights_CrazySupplierFailure() {
    // Arrange
    Page<Flight> flightPage = new PageImpl<>(Collections.singletonList(testFlight));
    FlightTimeRange timeRange = new FlightTimeRange(
            departureTime,
            null,
            null,
            arrivalTime
    );

    when(flightDomainService.searchFlights(
            any(), any(), any(), any(FlightTimeRange.class), any(Pageable.class)
    )).thenReturn(flightPage);

    when(flightMapper.toResponse(testFlight)).thenReturn(testFlightResponse);
    when(crazySupplierClient.isEnabled()).thenReturn(true);
    when(crazySupplierClient.searchFlights(any(), any(), any(), any()))
            .thenThrow(new RuntimeException("CrazySupplier API down"));

    // Act - Should not throw exception, just return DB results
    FlightSearchResponse result = flightApplicationService.searchFlights(
            "JFK", "LAX", null, timeRange, 0, 20
    );

    // Assert - Should have DB results only
    assertThat(result).isNotNull();
    assertThat(result.getFlights()).hasSize(1);
    assertThat(result.getFlights().getFirst().getSupplier()).isEqualTo("GlobalSupplier");

    verify(crazySupplierClient, times(1)).searchFlights(any(), any(), any(), any());
  }

  // ==================== PAGINATION TESTS ====================

  @Test
  @DisplayName("Should create correct pagination info")
  void testSearchFlights_PaginationInfo() {
    // Arrange
    Page<Flight> flightPage = new PageImpl<>(
            Collections.singletonList(testFlight),
            org.springframework.data.domain.PageRequest.of(2, 10),
            100 // total elements
    );
    FlightTimeRange timeRange = new FlightTimeRange(null, null, null, null);

    when(flightDomainService.searchFlights(
            any(), any(), any(), any(FlightTimeRange.class), any(Pageable.class)
    )).thenReturn(flightPage);

    when(flightMapper.toResponse(testFlight)).thenReturn(testFlightResponse);
    when(crazySupplierClient.isEnabled()).thenReturn(false);

    // Act
    FlightSearchResponse result = flightApplicationService.searchFlights(
            null, null, null, timeRange, 2, 10
    );

    // Assert
    assertThat(result.getPagination()).isNotNull();
    assertThat(result.getPagination().getCurrentPage()).isEqualTo(2);
    assertThat(result.getPagination().getPageSize()).isEqualTo(10);
    assertThat(result.getPagination().getTotalElements()).isEqualTo(1); // 1 in result
  }

  @Test
  @DisplayName("Should use default pagination values when not provided")
  void testSearchFlights_DefaultPagination() {
    // Arrange
    Page<Flight> flightPage = new PageImpl<>(Collections.singletonList(testFlight));
    FlightTimeRange timeRange = new FlightTimeRange(null, null, null, null);

    when(flightDomainService.searchFlights(
            any(), any(), any(), any(FlightTimeRange.class), any(Pageable.class)
    )).thenReturn(flightPage);

    when(flightMapper.toResponse(testFlight)).thenReturn(testFlightResponse);
    when(crazySupplierClient.isEnabled()).thenReturn(false);

    // Act - No page/size provided
    FlightSearchResponse result = flightApplicationService.searchFlights(
            null, null, null, timeRange, null, null
    );

    // Assert
    assertThat(result.getPagination()).isNotNull();
    assertThat(result.getPagination().getCurrentPage()).isZero();
    assertThat(result.getPagination().getPageSize()).isEqualTo(1);
  }

  // ==================== FLIGHTRECORD AND FLIGHTTIMERANGE TESTS ====================

  @Test
  @DisplayName("Should handle null FlightTimeRange values correctly")
  void testSearchFlights_NullTimeRangeValues() {
    // Arrange
    Page<Flight> flightPage = new PageImpl<>(Collections.singletonList(testFlight));
    FlightTimeRange timeRange = new FlightTimeRange(null, null, null, null);

    when(flightDomainService.searchFlights(
            any(), any(), any(), any(FlightTimeRange.class), any(Pageable.class)
    )).thenReturn(flightPage);

    when(flightMapper.toResponse(testFlight)).thenReturn(testFlightResponse);
    when(crazySupplierClient.isEnabled()).thenReturn(false);

    // Act
    FlightSearchResponse result = flightApplicationService.searchFlights(
            "JFK", "LAX", null, timeRange, 0, 20
    );

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getFlights()).hasSize(1);

    verify(flightDomainService, times(1)).searchFlights(
            eq("JFK"), eq("LAX"), isNull(),
            any(FlightTimeRange.class), any(Pageable.class)
    );
  }
}