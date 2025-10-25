package com.flightdata.flight_management.domain.service;

import com.flightdata.flight_management.application.dto.FlightRecord;
import com.flightdata.flight_management.application.dto.FlightTimeRange;
import com.flightdata.flight_management.domain.model.Flight;
import com.flightdata.flight_management.domain.repository.FlightRepository;
import com.flightdata.flight_management.presentation.exception.FlightNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FlightDomainService.
 *
 * @author Flight Data Management Team
 * @version 1.0
 * @since 2025-10-24
 */
@ExtendWith(MockitoExtension.class)
class FlightDomainServiceTest {

  @Mock
  private FlightRepository flightRepository;

  @InjectMocks
  private FlightDomainService flightDomainService;

  private Flight testFlight;
  private Instant departureTime;
  private Instant arrivalTime;

  @BeforeEach
  void setUp() {
    departureTime = Instant.now().plus(2, ChronoUnit.DAYS);
    arrivalTime = departureTime.plus(3, ChronoUnit.HOURS);

    testFlight = Flight.builder()
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
  }

  /**
   * Test successful flight creation.
   */
  @Test
  void testCreateFlight_Success() {
    // Arrange
    when(flightRepository.save(any(Flight.class))).thenReturn(testFlight);

    // Act
    Flight result = flightDomainService.createFlight(
            new FlightRecord("American Airlines",
                            "GlobalSupplier",
                            299.99,
                            "JFK",
                            "LAX",
                            departureTime,
                            arrivalTime)
    );

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getAirline()).isEqualTo("American Airlines");
    assertThat(result.getDepartureAirport()).isEqualTo("JFK");
    assertThat(result.getDestinationAirport()).isEqualTo("LAX");

    verify(flightRepository, times(1)).save(any(Flight.class));
  }

  /**
   * Test flight creation with null airline throws exception.
   */
  @Test
  void testCreateFlight_NullAirline_ThrowsException() {
    // Arrange
    FlightRecord flightRecord = new FlightRecord(
            null,
            "GlobalSupplier",
            299.99,
            "JFK",
            "LAX",
            departureTime,
            arrivalTime
    );

    // Act & Assert
    assertThatThrownBy(() -> flightDomainService.createFlight(flightRecord))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Airline is required");

    verify(flightRepository, never()).save(any(Flight.class));
  }

  /**
   * Test flight creation with invalid airport code throws an exception.
   */
  @Test
  void testCreateFlight_InvalidAirportCode_ThrowsException() {
    // Arrange
    FlightRecord invalidRecord = new FlightRecord(
            "American Airlines",
            "GlobalSupplier",
            299.99,
            "INVALID",
            "LAX",
            departureTime,
            arrivalTime
    );

    // Act & Assert
    assertThatThrownBy(() -> flightDomainService.createFlight(invalidRecord))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("3-letter code");

    verify(flightRepository, never()).save(any(Flight.class));
  }

  /**
   * Test successful flight update.
   */
  @Test
  void testUpdateFlight_Success() {
    // Arrange
    when(flightRepository.findById(1L)).thenReturn(Optional.of(testFlight));
    when(flightRepository.save(any(Flight.class))).thenReturn(testFlight);

    // Act
    Flight result = flightDomainService.updateFlight(
            1L,
            new FlightRecord(
            "Delta Airlines",
            null,
            399.99,
            null,
            null,
            null,
            null)
    );

    // Assert
    assertThat(result).isNotNull();
    verify(flightRepository, times(1)).findById(1L);
    verify(flightRepository, times(1)).save(any(Flight.class));
  }

  /**
   * Test update of non-existent flight throws exception.
   */
  @Test
  void testUpdateFlight_NotFound_ThrowsException() {
    // Arrange
    when(flightRepository.findById(999L)).thenReturn(Optional.empty());
    FlightRecord flightRecord = new FlightRecord(
            "Delta",
            null,
            null,
            null,
            null,
            null,
            null
    );
    // Act & Assert
    assertThatThrownBy(() -> flightDomainService.updateFlight(
            999L, flightRecord))
            .isInstanceOf(FlightNotFoundException.class)
            .hasMessageContaining("Flight not found with ID: 999");

    verify(flightRepository, times(1)).findById(999L);
    verify(flightRepository, never()).save(any(Flight.class));
  }

  /**
   * Test successful flight deletion.
   */
  @Test
  void testDeleteFlight_Success() {
    // Arrange
    when(flightRepository.existsById(1L)).thenReturn(true);
    doNothing().when(flightRepository).deleteById(1L);

    // Act
    flightDomainService.deleteFlight(1L);

    // Assert
    verify(flightRepository, times(1)).existsById(1L);
    verify(flightRepository, times(1)).deleteById(1L);
  }

  /**
   * Test deletion of non-existent flight throws exception.
   */
  @Test
  void testDeleteFlight_NotFound_ThrowsException() {
    // Arrange
    when(flightRepository.existsById(999L)).thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> flightDomainService.deleteFlight(999L))
            .isInstanceOf(FlightNotFoundException.class)
            .hasMessageContaining("Flight not found with ID: 999");

    verify(flightRepository, times(1)).existsById(999L);
    verify(flightRepository, never()).deleteById(anyLong());
  }

  /**
   * Test get flight by ID success.
   */
  @Test
  void testGetFlightById_Success() {
    // Arrange
    when(flightRepository.findById(1L)).thenReturn(Optional.of(testFlight));

    // Act
    Flight result = flightDomainService.getFlightById(1L);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getAirline()).isEqualTo("American Airlines");

    verify(flightRepository, times(1)).findById(1L);
  }

  /**
   * Test get flight by ID not found.
   */
  @Test
  void testGetFlightById_NotFound_ThrowsException() {
    // Arrange
    when(flightRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> flightDomainService.getFlightById(999L))
            .isInstanceOf(FlightNotFoundException.class)
            .hasMessageContaining("Flight not found with ID: 999");

    verify(flightRepository, times(1)).findById(999L);
  }

  /**
   * Test search flights with filters.
   */
  @Test
  void testSearchFlights_WithFilters() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 20);
    Page<Flight> flightPage = new PageImpl<>(Collections.singletonList(testFlight));

    when(flightRepository.searchFlights(
            eq("JFK"),
            eq("LAX"),
            eq("American"),
            any(Instant.class),
            any(Instant.class),
            isNull(),
            isNull(),
            eq(pageable)
    )).thenReturn(flightPage);

    // Act
    Page<Flight> result = flightDomainService.searchFlights(
            "jfk",  // Should be normalized to uppercase
            "lax",
            "American",
            new FlightTimeRange(
            departureTime,
            arrivalTime,
            null,
            null),
            pageable
    );

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().getFirst().getAirline()).isEqualTo("American Airlines");

    verify(flightRepository, times(1)).searchFlights(
            eq("JFK"),
            eq("LAX"),
            eq("American"),
            any(Instant.class),
            any(Instant.class),
            isNull(),
            isNull(),
            eq(pageable)
    );
  }

  /**
   * Test check for duplicate flight.
   */
  @Test
  void testIsDuplicateFlight_ReturnsTrue() {
    // Arrange
    when(flightRepository.existsByAirlineAndDepartureAirportAndDestinationAirportAndDepartureTime(
            "American Airlines",
            "JFK",
            "LAX",
            departureTime
    )).thenReturn(true);

    // Act
    boolean result = flightDomainService.isDuplicateFlight(
            "American Airlines",
            "jfk",
            "lax",
            departureTime
    );

    // Assert
    assertThat(result).isTrue();

    verify(flightRepository, times(1))
            .existsByAirlineAndDepartureAirportAndDestinationAirportAndDepartureTime(
                    "American Airlines",
                    "JFK",
                    "LAX",
                    departureTime
            );
  }

  /**
   * Test airport code normalization.
   */
  @Test
  void testSearchFlights_NormalizesAirportCodes() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 20);
    Page<Flight> flightPage = new PageImpl<>(Collections.singletonList(testFlight));

    when(flightRepository.searchFlights(
            eq("JFK"),
            eq("LAX"),
            isNull(),
            isNull(),
            isNull(),
            isNull(),
            isNull(),
            eq(pageable)
    )).thenReturn(flightPage);

    // Act
    flightDomainService.searchFlights(
            "jfk",  // lowercase
            "Lax",  // mixed case
            null,
            new FlightTimeRange(null,
            null,
            null,
            null),
            pageable
    );

    // Assert - verify uppercase codes were passed to repository
    verify(flightRepository, times(1)).searchFlights(
            eq("JFK"),
            eq("LAX"),
            isNull(),
            isNull(),
            isNull(),
            isNull(),
            isNull(),
            eq(pageable)
    );
  }
}