package com.flightdata.flight_management.domain.repository;

import com.flightdata.flight_management.domain.model.Flight;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository interface for Flight entity operations.
 * Provides data access methods following Domain-Driven Design principles.
 *
 * @author Vítor Matosinho Martins
 * @version 1.0
 * @since 2025-10-25
 */
@Repository
public interface FlightRepository extends JpaRepository<Flight, Long>,
        JpaSpecificationExecutor<Flight> {

  /**
   * Finds flights by departure airport code.
   *
   * @param departureAirport the 3-letter departure airport code
   * @param pageable pagination information
   * @return page of flights
   */
  Page<Flight> findByDepartureAirport(String departureAirport, Pageable pageable);

  /**
   * Finds flights by destination airport code.
   *
   * @param destinationAirport the 3-letter destination airport code
   * @param pageable pagination information
   * @return page of flights
   */
  Page<Flight> findByDestinationAirport(String destinationAirport, Pageable pageable);

  /**
   * Finds flights by airline name.
   *
   * @param airline the airline name
   * @param pageable pagination information
   * @return page of flights
   */
  Page<Flight> findByAirlineContainingIgnoreCase(String airline, Pageable pageable);

  /**
   * Finds flights by supplier name.
   *
   * @param supplier the supplier name
   * @param pageable pagination information
   * @return page of flights
   */
  Page<Flight> findBySupplier(String supplier, Pageable pageable);

  /**
   * Finds flights within a departure time range.
   *
   * @param from the start of the time range
   * @param to the end of the time range
   * @param pageable pagination information
   * @return page of flights
   */
  Page<Flight> findByDepartureTimeBetween(Instant from, Instant to, Pageable pageable);

  /**
   * Finds flights by departure and destination airports.
   *
   * @param departureAirport the departure airport code
   * @param destinationAirport the destination airport code
   * @param pageable pagination information
   * @return page of flights
   */
  Page<Flight> findByDepartureAirportAndDestinationAirport(
          String departureAirport,
          String destinationAirport,
          Pageable pageable);

  /**
   * Complex search query for flights with multiple optional filters.
   * Uses JPQL to handle dynamic query construction efficiently.
   *
   * @param departureAirport optional departure airport filter
   * @param destinationAirport optional destination airport filter
   * @param airline optional airline filter
   * @param departureFrom optional minimum departure time
   * @param departureTo optional maximum departure time
   * @param arrivalFrom optional minimum arrival time
   * @param arrivalTo optional maximum arrival time
   * @param pageable pagination information
   * @return page of matching flights
   */
  @Query("SELECT f FROM Flight f WHERE " +
          "(:departureAirport IS NULL OR f.departureAirport = :departureAirport) AND " +
          "(:destinationAirport IS NULL OR f.destinationAirport = :destinationAirport) AND " +
          "(:airline IS NULL OR LOWER(f.airline) LIKE LOWER(CONCAT('%', :airline, '%'))) AND " +
          "(:departureFrom IS NULL OR f.departureTime >= :departureFrom) AND " +
          "(:departureTo IS NULL OR f.departureTime <= :departureTo) AND " +
          "(:arrivalFrom IS NULL OR f.arrivalTime >= :arrivalFrom) AND " +
          "(:arrivalTo IS NULL OR f.arrivalTime <= :arrivalTo)")
  Page<Flight> searchFlights(
          @Param("departureAirport") String departureAirport,
          @Param("destinationAirport") String destinationAirport,
          @Param("airline") String airline,
          @Param("departureFrom") Instant departureFrom,
          @Param("departureTo") Instant departureTo,
          @Param("arrivalFrom") Instant arrivalFrom,
          @Param("arrivalTo") Instant arrivalTo,
          Pageable pageable);

  /**
   * Counts flights matching search criteria.
   * Used for pagination calculations.
   *
   * @param departureAirport optional departure airport filter
   * @param destinationAirport optional destination airport filter
   * @param airline optional airline filter
   * @param departureFrom optional minimum departure time
   * @param departureTo optional maximum departure time
   * @param arrivalFrom optional minimum arrival time
   * @param arrivalTo optional maximum arrival time
   * @return count of matching flights
   */
  @Query("SELECT COUNT(f) FROM Flight f WHERE " +
          "(:departureAirport IS NULL OR f.departureAirport = :departureAirport) AND " +
          "(:destinationAirport IS NULL OR f.destinationAirport = :destinationAirport) AND " +
          "(:airline IS NULL OR LOWER(f.airline) LIKE LOWER(CONCAT('%', :airline, '%'))) AND " +
          "(:departureFrom IS NULL OR f.departureTime >= :departureFrom) AND " +
          "(:departureTo IS NULL OR f.departureTime <= :departureTo) AND " +
          "(:arrivalFrom IS NULL OR f.arrivalTime >= :arrivalFrom) AND " +
          "(:arrivalTo IS NULL OR f.arrivalTime <= :arrivalTo)")
  long countSearchFlights(
          @Param("departureAirport") String departureAirport,
          @Param("destinationAirport") String destinationAirport,
          @Param("airline") String airline,
          @Param("departureFrom") Instant departureFrom,
          @Param("departureTo") Instant departureTo,
          @Param("arrivalFrom") Instant arrivalFrom,
          @Param("arrivalTo") Instant arrivalTo);

  /**
   * Finds all flights departing after a specific time.
   * Useful for finding upcoming flights.
   *
   * @param departureTime the minimum departure time
   * @return list of flights
   */
  List<Flight> findByDepartureTimeAfter(Instant departureTime);

  /**
   * Checks if a flight exists with the exact same details.
   * Used to prevent duplicate entries.
   *
   * @param airline the airline name
   * @param departureAirport the departure airport
   * @param destinationAirport the destination airport
   * @param departureTime the departure time
   * @return true if a matching flight exists
   */
  boolean existsByAirlineAndDepartureAirportAndDestinationAirportAndDepartureTime(
          String airline,
          String departureAirport,
          String destinationAirport,
          Instant departureTime);
}