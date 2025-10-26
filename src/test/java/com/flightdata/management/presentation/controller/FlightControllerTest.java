package com.flightdata.management.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightdata.management.application.dto.*;
import com.flightdata.management.application.service.FlightApplicationService;
import com.flightdata.management.domain.service.FlightDomainService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for FlightController.
 * Tests HTTP layer with a mocked service layer.
 *
 * @author Vítor Matosinho Martins
 * @version 1.0
 * @since 2025-10-26
 */
@WebMvcTest(FlightController.class)
class FlightControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private FlightApplicationService flightApplicationService;

  @TestConfiguration
  static class TestConfig {
    @Bean
    FlightApplicationService flightApplicationService() {
      return mock(FlightApplicationService.class);
    }
  }
  /**
   * Test create a flight endpoint - success scenario.
   */
  @Test
  void testCreateFlight_Success() throws Exception {
    // Arrange
    FlightCreateRequest request = FlightCreateRequest.builder()
            .airline("American Airlines")
            .supplier("GlobalSupplier")
            .fare(299.99)
            .departureAirport("JFK")
            .destinationAirport("LAX")
            .departureTime(OffsetDateTime.of(2025, 10, 25, 10, 30, 0, 0, ZoneOffset.UTC))
            .arrivalTime(OffsetDateTime.of(2025, 10, 25, 16, 45, 0, 0, ZoneOffset.UTC))
            .build();

    FlightResponse response = FlightResponse.builder()
            .id(1L)
            .airline("American Airlines")
            .supplier("GlobalSupplier")
            .fare(299.99)
            .departureAirport("JFK")
            .destinationAirport("LAX")
            .departureTime(OffsetDateTime.of(2025, 10, 25, 10, 30, 0, 0, ZoneOffset.UTC))
            .arrivalTime(OffsetDateTime.of(2025, 10, 25, 16, 45, 0, 0, ZoneOffset.UTC))
            .build();

    when(flightApplicationService.createFlight(org.mockito.ArgumentMatchers.any(FlightCreateRequest.class)))
            .thenReturn(response);

    // Act & Assert
    mockMvc.perform(post("/api/v1/flights")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.airline", is("American Airlines")))
            .andExpect(jsonPath("$.departureAirport", is("JFK")))
            .andExpect(jsonPath("$.destinationAirport", is("LAX")))
            .andExpect(jsonPath("$.fare", is(299.99)));

    verify(flightApplicationService, times(1)).createFlight(org.mockito.ArgumentMatchers.any(FlightCreateRequest.class));
  }

  /**
   * Test get flight by ID endpoint - success scenario.
   */
  @Test
  void testGetFlightById_Success() throws Exception {
    // Arrange
    FlightResponse response = FlightResponse.builder()
            .id(1L)
            .airline("Delta Airlines")
            .supplier("GlobalSupplier")
            .fare(350.00)
            .departureAirport("ORD")
            .destinationAirport("MIA")
            .departureTime(OffsetDateTime.of(2025, 10, 26, 8, 0, 0, 0, ZoneOffset.UTC))
            .arrivalTime(OffsetDateTime.of(2025, 10, 26, 12, 15, 0, 0, ZoneOffset.UTC))
            .build();

    when(flightApplicationService.getFlightById(1L)).thenReturn(response);

    // Act & Assert
    mockMvc.perform(get("/api/v1/flights/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.airline", is("Delta Airlines")))
            .andExpect(jsonPath("$.fare", is(350.00)));

    verify(flightApplicationService, times(1)).getFlightById(1L);
  }

  /**
   * Test get flight by ID - not found scenario.
   */
  @Test
  void testGetFlightById_NotFound() throws Exception {
    // Arrange
    when(flightApplicationService.getFlightById(999L))
            .thenThrow(new FlightDomainService.FlightNotFoundException("Flight not found with ID: 999"));

    // Act & Assert
    mockMvc.perform(get("/api/v1/flights/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status", is(404)))
            .andExpect(jsonPath("$.message", containsString("Flight not found")));

    verify(flightApplicationService, times(1)).getFlightById(999L);
  }

  /**
   * Test update flight endpoint - success scenario.
   */
  @Test
  void testUpdateFlight_Success() throws Exception {
    // Arrange
    FlightUpdateRequest request = FlightUpdateRequest.builder()
            .fare(399.99)
            .build();

    FlightResponse response = FlightResponse.builder()
            .id(1L)
            .airline("American Airlines")
            .supplier("GlobalSupplier")
            .fare(399.99)
            .departureAirport("JFK")
            .destinationAirport("LAX")
            .departureTime(OffsetDateTime.of(2025, 10, 25, 10, 30, 0, 0, ZoneOffset.UTC))
            .arrivalTime(OffsetDateTime.of(2025, 10, 25, 16, 45, 0, 0, ZoneOffset.UTC))
            .build();

    when(flightApplicationService.updateFlight(eq(1L), org.mockito.ArgumentMatchers.any(FlightUpdateRequest.class)))
            .thenReturn(response);

    // Act & Assert
    mockMvc.perform(put("/api/v1/flights/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.fare", is(399.99)));

    verify(flightApplicationService, times(1)).updateFlight(eq(1L), org.mockito.ArgumentMatchers.any(FlightUpdateRequest.class));
  }

  /**
   * Test delete flight endpoint - success scenario.
   */
  @Test
  void testDeleteFlight_Success() throws Exception {
    // Arrange
    doNothing().when(flightApplicationService).deleteFlight(1L);

    // Act & Assert
    mockMvc.perform(delete("/api/v1/flights/1"))
            .andExpect(status().isNoContent());

    verify(flightApplicationService, times(1)).deleteFlight(1L);
  }

  /**
   * Test delete flight - not found scenario.
   */
  @Test
  void testDeleteFlight_NotFound() throws Exception {
    // Arrange
    doThrow(new FlightDomainService.FlightNotFoundException("Flight not found with ID: 999"))
            .when(flightApplicationService).deleteFlight(999L);

    // Act & Assert
    mockMvc.perform(delete("/api/v1/flights/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status", is(404)))
            .andExpect(jsonPath("$.message", containsString("Flight not found")));

    verify(flightApplicationService, times(1)).deleteFlight(999L);
  }

  /**
   * Test search flights endpoint - with filters.
   */
  @Test
  void testSearchFlights_WithFilters() throws Exception {
    // Arrange
    FlightResponse flight1 = FlightResponse.builder()
            .id(1L)
            .airline("American Airlines")
            .supplier("GlobalSupplier")
            .fare(299.99)
            .departureAirport("JFK")
            .destinationAirport("LAX")
            .departureTime(OffsetDateTime.of(2025, 10, 25, 10, 30, 0, 0, ZoneOffset.UTC))
            .arrivalTime(OffsetDateTime.of(2025, 10, 25, 16, 45, 0, 0, ZoneOffset.UTC))
            .build();

    FlightResponse flight2 = FlightResponse.builder()
            .id(2L)
            .airline("United Airlines")
            .supplier("GlobalSupplier")
            .fare(320.00)
            .departureAirport("JFK")
            .destinationAirport("LAX")
            .departureTime(OffsetDateTime.of(2025, 10, 25, 14, 0, 0, 0, ZoneOffset.UTC))
            .arrivalTime(OffsetDateTime.of(2025, 10, 25, 20, 15, 0, 0, ZoneOffset.UTC))
            .build();

    PaginationInfo pagination = PaginationInfo.builder()
            .currentPage(0)
            .pageSize(20)
            .totalElements(2L)
            .totalPages(1)
            .build();

    FlightSearchResponse searchResponse = FlightSearchResponse.builder()
            .flights(Arrays.asList(flight1, flight2))
            .pagination(pagination)
            .build();

    when(flightApplicationService.searchFlights(
            eq("JFK"),
            eq("LAX"),
            isNull(),
            any(),
            eq(0),
            eq(20)
    )).thenReturn(searchResponse);

    // Act & Assert
    mockMvc.perform(get("/api/v1/flights")
                    .param("origin", "JFK")
                    .param("destination", "LAX")
                    .param("departureFrom", "2025-10-25T10:00:00Z")
                    .param("departureTo", "2025-10-25T18:00:00Z")
                    .param("page", "0")
                    .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.flights", hasSize(2)))
            .andExpect(jsonPath("$.flights[0].airline", is("American Airlines")))
            .andExpect(jsonPath("$.flights[1].airline", is("United Airlines")))
            .andExpect(jsonPath("$.pagination.totalElements", is(2)))
            .andExpect(jsonPath("$.pagination.currentPage", is(0)));

    verify(flightApplicationService, times(1)).searchFlights(
            eq("JFK"),
            eq("LAX"),
            isNull(),
            any(),
            eq(0),
            eq(20)
    );
  }

  /**
   * Test search flights endpoint - no filters (all flights).
   */
  @Test
  void testSearchFlights_NoFilters() throws Exception {
    // Arrange
    PaginationInfo pagination = PaginationInfo.builder()
            .currentPage(0)
            .pageSize(20)
            .totalElements(0L)
            .totalPages(0)
            .build();

    FlightSearchResponse searchResponse = FlightSearchResponse.builder()
            .flights(List.of())
            .pagination(pagination)
            .build();

    when(flightApplicationService.searchFlights(
            isNull(),
            isNull(),
            isNull(),
            any(),
            eq(0),
            eq(20)
    )).thenReturn(searchResponse);

    // Act & Assert
    mockMvc.perform(get("/api/v1/flights"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.flights", hasSize(0)))
            .andExpect(jsonPath("$.pagination.totalElements", is(0)));

    verify(flightApplicationService, times(1)).searchFlights(
            isNull(),
            isNull(),
            isNull(),
            any(),
            eq(0),
            eq(20)
    );
  }

  /**
   * Test create flight with a validation error.
   */
  @Test
  void testCreateFlight_ValidationError() throws Exception {
    // Arrange - Invalid request (missing required fields)
    FlightCreateRequest invalidRequest = FlightCreateRequest.builder()
            .airline("American Airlines")
            // Missing other required fields
            .build();

    // Act & Assert
    mockMvc.perform(post("/api/v1/flights")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());

    verify(flightApplicationService, never()).createFlight(org.mockito.ArgumentMatchers.any());
  }

  /**
   * Test search with airline filter.
   */
  @Test
  void testSearchFlights_WithAirlineFilter() throws Exception {
    // Arrange
    FlightResponse flight = FlightResponse.builder()
            .id(1L)
            .airline("American Airlines")
            .supplier("GlobalSupplier")
            .fare(299.99)
            .departureAirport("JFK")
            .destinationAirport("LAX")
            .departureTime(OffsetDateTime.of(2025, 10, 25, 10, 30, 0, 0, ZoneOffset.UTC))
            .arrivalTime(OffsetDateTime.of(2025, 10, 25, 16, 45, 0, 0, ZoneOffset.UTC))
            .build();

    PaginationInfo pagination = PaginationInfo.builder()
            .currentPage(0)
            .pageSize(20)
            .totalElements(1L)
            .totalPages(1)
            .build();

    FlightSearchResponse searchResponse = FlightSearchResponse.builder()
            .flights(Collections.singletonList(flight))
            .pagination(pagination)
            .build();

    when(flightApplicationService.searchFlights(
            isNull(),
            isNull(),
            eq("American"),
            any(),
            eq(0),
            eq(20)
    )).thenReturn(searchResponse);

    // Act & Assert
    mockMvc.perform(get("/api/v1/flights")
                    .param("airline", "American")
                    .param("page", "0")
                    .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.flights", hasSize(1)))
            .andExpect(jsonPath("$.flights[0].airline", is("American Airlines")));

    verify(flightApplicationService, times(1)).searchFlights(
            isNull(),
            isNull(),
            eq("American"),
           any(),
            eq(0),
            eq(20)
    );
  }
}