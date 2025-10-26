package com.flightdata.management.application.service;

import com.flightdata.management.application.dto.*;
import com.flightdata.management.application.mapper.FlightMapper;
import com.flightdata.management.domain.model.Flight;
import com.flightdata.management.domain.service.FlightDomainService;
import com.flightdata.management.infrastructure.external.client.CrazySupplierClient;
import com.flightdata.management.infrastructure.external.dto.CrazySupplierDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application service orchestrating flight operations.
 *
 * <p>This service acts as the orchestration layer in DDD architecture, coordinating
 * between the presentation layer (controllers), domain layer (business logic),
 * and infrastructure layer (external APIs, persistence).
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Coordinate between domain services and external clients</li>
 *   <li>Map between DTOs and domain entities</li>
 *   <li>Handle transaction boundaries</li>
 *   <li>Combine data from multiple sources (database + CrazySupplier)</li>
 *   <li>Apply application-level business rules</li>
 * </ul>
 *
 * <p>This service is the proper entry point for controllers, not the domain service.
 * Controllers should never directly call domain services or repositories.
 *
 * @author Vítor Matosinho Martins
 * @version 1.0
 * @since 2025-10-24
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FlightApplicationService {

  private final FlightDomainService flightDomainService;
  private final FlightMapper flightMapper;
  private final CrazySupplierClient crazySupplierClient;

  /**
   * Creates a new flight from request DTO.
   *
   * <p>This method:
   * <ol>
   *   <li>Validates the request (automatically via Bean Validation)</li>
   *   <li>Converts DTO to domain entity</li>
   *   <li>Delegates to domain service for creation</li>
   *   <li>Converts result back to DTO</li>
   * </ol>
   *
   * @param request the flight creation request containing all required fields
   * @return the created flight response with generated ID
   * @throws IllegalArgumentException if validation fails at the domain level
   */
  @Transactional
  public FlightResponse createFlight(FlightCreateRequest request) {
    log.info("Creating flight for airline: {}", request.getAirline());

    Flight flight = flightDomainService.createFlight(new FlightRecord(
            request.getAirline(),
            request.getSupplier(),
            request.getFare(),
            request.getDepartureAirport(),
            request.getDestinationAirport(),
            request.getDepartureTime().toInstant(),
            request.getArrivalTime().toInstant())
    );

    FlightResponse response = flightMapper.toResponse(flight);
    log.info("Flight created successfully with ID: {}", response.getId());

    return response;
  }

  /**
   * Updates an existing flight.
   *
   * <p>Only non-null fields in the request will be updated.
   * This allows partial updates where clients only need to send
   * the fields they want to change.
   *
   * @param id the flight ID to update
   * @param request the update request with fields to change
   * @return the updated flight response
   * @throws FlightDomainService.FlightNotFoundException if flight doesn't exist
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional
  public FlightResponse updateFlight(Long id, FlightUpdateRequest request) {
    log.info("Updating flight with ID: {}", id);

    Instant departureTime = request.getDepartureTime() != null
            ? request.getDepartureTime().toInstant() : null;
    Instant arrivalTime = request.getArrivalTime() != null
            ? request.getArrivalTime().toInstant() : null;

    Flight flight = flightDomainService.updateFlight(
            id,
            new FlightRecord(
                    request.getAirline(),
                    request.getSupplier(),
                    request.getFare(),
                    request.getDepartureAirport(),
                    request.getDestinationAirport(),
                    departureTime,
                    arrivalTime)
    );

    FlightResponse response = flightMapper.toResponse(flight);
    log.info("Flight updated successfully with ID: {}", response.getId());

    return response;
  }

  /**
   * Deletes a flight by ID.
   *
   * @param id the flight ID to delete
   * @throws FlightDomainService.FlightNotFoundException if flight doesn't exist
   */
  @Transactional
  public void deleteFlight(Long id) {
    log.info("Deleting flight with ID: {}", id);
    flightDomainService.deleteFlight(id);
    log.info("Flight deleted successfully with ID: {}", id);
  }

  /**
   * Retrieves a flight by ID.
   *
   * @param id the flight ID
   * @return the flight response
   * @throws FlightDomainService.FlightNotFoundException if flight doesn't exist
   */
  public FlightResponse getFlightById(Long id) {
    log.info("Retrieving flight with ID: {}", id);
    Flight flight = flightDomainService.getFlightById(id);
    return flightMapper.toResponse(flight);
  }

  /**
   * Searches for flights from a database and external suppliers.
   *
   * <p>This is the core search method that combines results from:
   * <ul>
   *   <li>Internal database (via FlightDomainService)</li>
   *   <li>CrazySupplier API (if origin and destination are provided)</li>
   * </ul>
   *
   * <p>Search Features:
   * <ul>
   *   <li>Filter by origin, destination, airline, and time ranges</li>
   *   <li>Pagination support</li>
   *   <li>Sorted by departure time</li>
   *   <li>Combines multiple data sources</li>
   *   <li>Graceful degradation if external API fails</li>
   * </ul>
   *
   * @param origin departure airport code (optional)
   * @param destination destination airport code (optional)
   * @param airline airline name filter (optional, partial match)
   * @param timeRange departure/arrival time range filter (optional)
   * @param page page number, 0-indexed (default: 0)
   * @param size page size (default: 20, max: 100)
   * @return combined flight search response with pagination info
   */
  public FlightSearchResponse searchFlights(
          String origin,
          String destination,
          String airline,
          FlightTimeRange timeRange,
          Integer page,
          Integer size) {

    log.info("Searching flights - origin: {}, destination: {}, airline: {}",
            origin, destination, airline);


    // Create a pageable with sorting by departure time
    Pageable pageable = PageRequest.of(
            page != null ? page : 0,
            size != null ? size : 20,
            Sort.by(Sort.Direction.ASC, "departureTime")
    );

    // Search in a database
    Page<Flight> databaseFlights = flightDomainService.searchFlights(
            origin,
            destination,
            airline,
            timeRange,
            pageable
    );

    // Convert database flights to response DTOs
    List<FlightResponse> allFlights = databaseFlights.getContent().stream()
            .map(flightMapper::toResponse)
            .collect(Collectors.toCollection(ArrayList::new));

    log.debug("Found {} flights in database", allFlights.size());

    // Fetch from CrazySupplier if applicable
    if (shouldQueryCrazySupplier(origin, destination)) {
      List<FlightResponse> crazySupplierFlights = fetchFromCrazySupplier(
              origin,
              destination,
              timeRange.departureFrom(),
              timeRange.arrivalTo(),
              airline);

      allFlights.addAll(crazySupplierFlights);
      log.info("Combined {} database flights with {} CrazySupplier flights",
              databaseFlights.getContent().size(), crazySupplierFlights.size());
    }

    // Build pagination info
    PaginationInfo paginationInfo = PaginationInfo.builder()
            .currentPage(databaseFlights.getNumber())
            .pageSize(databaseFlights.getSize())
            .totalElements((long) allFlights.size())
            .totalPages((int) Math.ceil((double) allFlights.size() / databaseFlights.getSize()))
            .build();

    FlightSearchResponse response = FlightSearchResponse.builder()
                                                        .flights(allFlights)
                                                        .pagination(paginationInfo)
                                                        .build();

    log.info("Search completed. Returning {} total flights", allFlights.size());

    return response;
  }

  /**
   * Determines if CrazySupplier should be queried based on search criteria.
   *
   * <p>CrazySupplier requires both origin and destination to perform a search.
   * We only query if:
   * <ul>
   *   <li>Integration is enabled</li>
   *   <li>Both origin and destination are provided</li>
   * </ul>
   *
   * @param origin departure airport code
   * @param destination destination airport code
   * @return true if CrazySupplier should be queried
   */
  private boolean shouldQueryCrazySupplier(String origin, String destination) {
    if (!crazySupplierClient.isEnabled()) {
      log.debug("CrazySupplier integration is disabled");
      return false;
    }

    if (origin == null || destination == null) {
      log.debug("Skipping CrazySupplier query - origin or destination is missing");
      return false;
    }

    return true;
  }

  /**
   * Fetches flights from CrazySupplier API.
   *
   * <p>This method:
   * <ol>
   *   <li>Converts UTC times to CET LocalDate for CrazySupplier API</li>
   *   <li>Calls CrazySupplier API</li>
   *   <li>Filters results by airline if specified</li>
   *   <li>Maps responses to our FlightResponse DTO</li>
   *   <li>Returns an empty list on any error (graceful degradation)</li>
   * </ol>
   *
   * <p><b>Timezone Conversion:</b><br>
   * CrazySupplier uses CET timezone, but we work in UTC internally.
   * We convert UTC Instant to CET LocalDate for the API call.
   *
   * @param origin departure airport
   * @param destination destination airport
   * @param departureFrom minimum departure time (UTC)
   * @param arrivalTo maximum arrival time (UTC)
   * @param airline airline filter (optional)
   * @return list of flights from CrazySupplier, empty if unavailable
   */
  private List<FlightResponse> fetchFromCrazySupplier(
          String origin,
          String destination,
          Instant departureFrom,
          Instant arrivalTo,
          String airline) {

    try {
      log.debug("Fetching flights from CrazySupplier for route {} -> {}", origin, destination);

      // Convert UTC times to CET LocalDate for CrazySupplier API
      // CrazySupplier expects dates in CET timezone
      ZoneId cetZone = ZoneId.of("CET");

      LocalDate outboundDate = departureFrom != null
              ? LocalDateTime.ofInstant(departureFrom, cetZone).toLocalDate()
              : LocalDate.now(cetZone);

      LocalDate inboundDate = arrivalTo != null
              ? LocalDateTime.ofInstant(arrivalTo, cetZone).toLocalDate()
              : outboundDate.plusDays(1); // Default to the next day if not specified

      // Call CrazySupplier API
      List<CrazySupplierDTO.Response> crazySupplierResponses =
              crazySupplierClient.searchFlights(origin, destination, outboundDate, inboundDate);

      // Map to our DTO
      List<FlightResponse> flights = crazySupplierResponses.stream()
              .map(flightMapper::crazySupplierToResponse)
              .toList();

      // Filter by airline if specified
      if (airline != null && !airline.trim().isEmpty()) {
        flights = flights.stream()
                         .filter(f -> f.getAirline() != null
                                 && f.getAirline().toLowerCase().contains(airline.toLowerCase()))
                         .toList();

        log.debug("Filtered CrazySupplier results by airline '{}': {} flights remain",
                airline, flights.size());
      }

      log.info("Retrieved {} flights from CrazySupplier", flights.size());
      return flights;

    } catch (Exception e) {
      log.error("Error fetching from CrazySupplier, continuing with database results only", e);
      // Graceful degradation: return an empty list, don't fail the entire search
      return new ArrayList<>();
    }
  }
}
