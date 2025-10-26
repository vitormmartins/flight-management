package com.flightdata.flight_management.domain.service;

import com.flightdata.flight_management.application.dto.FlightRecord;
import com.flightdata.flight_management.application.dto.FlightTimeRange;
import com.flightdata.flight_management.domain.model.Flight;
import com.flightdata.flight_management.domain.repository.FlightRepository;
import com.flightdata.flight_management.presentation.exception.FlightNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Domain service for flight business logic.
 * Encapsulates domain operations and business rules that don't naturally fit
 * within a single entity.
 *
 * @author Vítor Matosinho Martins
 * @version 1.0
 * @since 2025-10-24
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FlightDomainService {

  private final FlightRepository flightRepository;

  /**
   * Creates and persists a new flight.
   *
   * @param request DTO containing flight data: airline, supplier, fare,
   *                departureAirport, destinationAirport, departureTime and arrivalTime
   * @return the persisted flight
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional
  public Flight createFlight(FlightRecord request) {

    log.debug("Creating new flight: {} from {} to {}",
            request.airline(),
            request.departureAirport(),
            request.destinationAirport());

    Flight flight = Flight.createFlight(
            request.airline(),
            request.supplier(),
            request.fare(),
            normalizeAirportCode(request.departureAirport()),
            normalizeAirportCode(request.destinationAirport()),
            request.departureTime(),
            request.arrivalTime());

    Flight savedFlight = flightRepository.save(flight);
    log.info("Flight created with ID: {}", savedFlight.getId());

    return savedFlight;
  }

  /**
   * Update an existing flight by applying values from the provided
   * `FlightRecord` to the persisted entity.
   * <p>
   * Only fields carried by `request` will be used to modify the flight.
   *
   * @param id      the flight ID to update
   * @param request DTO containing new values for airline, supplier, fare,
   *                departureAirport, destinationAirport, departureTime and arrivalTime
   * @return the updated `Flight`
   * @throws FlightNotFoundException if no flight exists with the given `id`
   */
  @Transactional
  public Flight updateFlight(Long id, FlightRecord request) {

    log.debug("Updating flight with ID: {}", id);

    Flight flight = flightRepository.findById(id)
            .orElseThrow(() -> new FlightNotFoundException(
                    "Flight not found with ID: " + id));

    flight.updateFlight(
            request.airline(),
            request.supplier(),
            request.fare(),
            normalizeAirportCode(request.departureAirport()),
            normalizeAirportCode(request.destinationAirport()),
            request.departureTime(),
            request.arrivalTime());

    Flight updatedFlight = flightRepository.save(flight);
    log.info("Flight updated with ID: {}", updatedFlight.getId());

    return updatedFlight;
  }

  /**
   * Deletes a flight by ID.
   *
   * @param id the flight ID
   * @throws FlightNotFoundException if flight doesn't exist
   */
  @Transactional
  public void deleteFlight(Long id) {
    log.debug("Deleting flight with ID: {}", id);

    if (!flightRepository.existsById(id)) {
      throw new FlightNotFoundException(
              "Flight not found with ID: " + id);
    }

    flightRepository.deleteById(id);
    log.info("Flight deleted with ID: {}", id);
  }

  /**
   * Retrieves a flight by ID.
   *
   * @param id the flight ID
   * @return the flight
   * @throws FlightNotFoundException if flight doesn't exist
   */
  public Flight getFlightById(Long id) {
    log.debug("Retrieving flight with ID: {}", id);

    return flightRepository.findById(id)
            .orElseThrow(() -> new FlightNotFoundException(
                    "Flight not found with ID: " + id));
  }

  /**
   * Searches for flights based on multiple criteria.
   *
   * @param departureAirport   optional departure airport filter
   * @param destinationAirport optional destination airport filter
   * @param airline            optional airline filter
   * @param flightTimes        optional container for time range filters; if non-null, the method will use
   *                           flightTimes.departureFrom(), flightTimes.departureTo(),
   *                           flightTimes.arrivalFrom(), and flightTimes.arrivalTo()
   * @param pageable           pagination information
   * @return page of matching flights
   */
  public Page<Flight> searchFlights(
          String departureAirport,
          String destinationAirport,
          String airline,
          FlightTimeRange flightTimes,
          Pageable pageable) {

    log.debug("Searching flights with criteria - origin: {}, destination: {}, airline: {}",
            departureAirport,
            destinationAirport,
            airline);

    return flightRepository.searchFlights(
            normalizeAirportCode(departureAirport),
            normalizeAirportCode(destinationAirport),
            airline,
            flightTimes.departureFrom(),
            flightTimes.departureTo(),
            flightTimes.arrivalFrom(),
            flightTimes.arrivalTo(),
            pageable);
  }

  /**
   * Checks if a duplicate flight exists.
   * A duplicate is defined as having the same airline, route, and departure time.
   *
   * @param airline            the airline name
   * @param departureAirport   the departure airport
   * @param destinationAirport the destination airport
   * @param departureTime      the departure time
   * @return true if a duplicate exists
   */
  public boolean isDuplicateFlight(
          String airline,
          String departureAirport,
          String destinationAirport,
          Instant departureTime) {

    return flightRepository.existsByAirlineAndDepartureAirportAndDestinationAirportAndDepartureTime(
            airline,
            normalizeAirportCode(departureAirport),
            normalizeAirportCode(destinationAirport),
            departureTime);
  }

  /**
   * Normalizes airport code to uppercase.
   * Returns null if input is null.
   *
   * @param airportCode the airport code to normalize
   * @return normalized airport code or null
   */
  private String normalizeAirportCode(String airportCode) {
    return airportCode != null ? airportCode.toUpperCase() : null;
  }
}