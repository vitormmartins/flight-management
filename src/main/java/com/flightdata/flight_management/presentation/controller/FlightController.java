package com.flightdata.flight_management.presentation.controller;


import com.flightdata.flight_management.application.dto.FlightTimeRange;
import com.flightdata.flight_management.application.service.FlightApplicationService;
import com.flightdata.management.application.dto.*;
import com.flightdata.management.presentation.api.FlightsApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

/**
 * REST Controller for Flight operations.
 * Implements the OpenAPI-generated interface.
 *
 * @author Vítor Matosinho Martins
 * @version 1.0
 * @since 2025-10-24
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class FlightController implements FlightsApi {

  private final FlightApplicationService flightApplicationService;

  /**
   * {@inheritDoc}
   */
  @Override
  public ResponseEntity<FlightSearchResponse> _searchFlights(
          String origin,
          String destination,
          String airline,
          OffsetDateTime departureFrom,
          OffsetDateTime departureTo,
          OffsetDateTime arrivalFrom,
          OffsetDateTime arrivalTo,
          Integer page,
          Integer size) {

    log.info("GET /api/v1/flights - Search request received");

    FlightTimeRange flightTimeRange = new FlightTimeRange(
            departureFrom != null ? departureFrom.toInstant() : null,
            departureTo != null ? departureTo.toInstant() : null,
            arrivalFrom != null ? arrivalFrom.toInstant() : null,
            arrivalTo != null ? arrivalTo.toInstant() : null
    );

    FlightSearchResponse response = flightApplicationService.searchFlights(
            origin,
            destination,
            airline,
            flightTimeRange,
            page,
            size
    );

    return ResponseEntity.ok(response);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ResponseEntity<FlightResponse> _createFlight(FlightCreateRequest flightCreateRequest) {
    log.info("POST /api/v1/flights - Create flight request received");

    FlightResponse response = flightApplicationService.createFlight(flightCreateRequest);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ResponseEntity<FlightResponse> _getFlightById(Long id) {
    log.info("GET /api/v1/flights/{} - Get flight by ID", id);

    FlightResponse response = flightApplicationService.getFlightById(id);

    return ResponseEntity.ok(response);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ResponseEntity<FlightResponse> _updateFlight(Long id, FlightUpdateRequest flightUpdateRequest) {
    log.info("PUT /api/v1/flights/{} - Update flight request received", id);

    FlightResponse response = flightApplicationService.updateFlight(id, flightUpdateRequest);

    return ResponseEntity.ok(response);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ResponseEntity<Void> _deleteFlight(Long id) {
    log.info("DELETE /api/v1/flights/{} - Delete flight request received", id);

    flightApplicationService.deleteFlight(id);

    return ResponseEntity.noContent().build();
  }
}
