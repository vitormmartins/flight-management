package com.flightdata.flight_management.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * Domain entity representing a flight record.
 * This entity encapsulates the core business logic and invariants for flight data.
 *
 * @author Vítor Matosinho Martins
 * @version 1.0
 * @since 2025-10-25
 */
@Entity
@Table(name = "flights", indexes = {
        @Index(name = "idx_departure_airport", columnList = "departure_airport"),
        @Index(name = "idx_destination_airport", columnList = "destination_airport"),
        @Index(name = "idx_airline", columnList = "airline"),
        @Index(name = "idx_departure_time", columnList = "departure_time"),
        @Index(name = "idx_supplier", columnList = "supplier")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString
public class Flight {

  /**
   * Unique identifier for the flight record.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * The name of the airline operating this flight.
   * Cannot be null or empty.
   */
  @Column(name = "airline", nullable = false, length = 100)
  private String airline;

  /**
   * The supplier providing this flight data.
   * Cannot be null or empty.
   */
  @Column(name = "supplier", nullable = false, length = 100)
  private String supplier;

  /**
   * The total fare/price for this flight.
   * Must be a positive value.
   */
  @Column(name = "fare", nullable = false)
  private Double fare;

  /**
   * Three-letter IATA code for the departure airport.
   * Must be exactly 3 uppercase letters.
   */
  @Column(name = "departure_airport", nullable = false, length = 3)
  private String departureAirport;

  /**
   * Three-letter IATA code for the destination airport.
   * Must be exactly 3 uppercase letters.
   */
  @Column(name = "destination_airport", nullable = false, length = 3)
  private String destinationAirport;

  /**
   * Departure date and time in the UTC timezone.
   * Stored as Instant for timezone-independent representation.
   */
  @Column(name = "departure_time", nullable = false)
  private Instant departureTime;

  /**
   * Arrival date and time in UTC timezone.
   * Stored as Instant for timezone-independent representation.
   */
  @Column(name = "arrival_time", nullable = false)
  private Instant arrivalTime;

  /**
   * Timestamp when this record was created.
   */
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  /**
   * Timestamp when this record was last updated.
   */
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  /**
   * JPA callback to set the creation timestamp before persisting.
   */
  @PrePersist
  protected void onCreate() {
    createdAt = Instant.now();
    updatedAt = Instant.now();
    validateBusinessRules();
  }

  /**
   * JPA callback to update the modification timestamp before updating.
   */
  @PreUpdate
  protected void onUpdate() {
    updatedAt = Instant.now();
    validateBusinessRules();
  }

  /**
   * Validates business rules for the flight entity.
   * Ensures data integrity at the domain level.
   *
   * @throws IllegalStateException if any business rule is violated
   */
  private void validateBusinessRules() {
    if (fare != null && fare < 0) {
      throw new IllegalStateException("Fare cannot be negative");
    }

    if (departureTime != null && arrivalTime != null
        && !arrivalTime.isAfter(departureTime)) {
      throw new IllegalStateException(
              "Arrival time must be after departure time");
    }

    if (departureAirport != null && departureAirport.equals(destinationAirport)) {
      throw new IllegalStateException(
              "Departure and destination airports cannot be the same");
    }
  }

  /**
   * Factory method to create a new Flight instance with validation.
   *
   * @param airline the airline name
   * @param supplier the supplier name
   * @param fare the flight fare
   * @param departureAirport the departure airport code
   * @param destinationAirport the destination airport code
   * @param departureTime the departure time
   * @param arrivalTime the arrival time
   * @return a new Flight instance
   * @throws IllegalArgumentException if any required field is invalid
   */
  public static Flight createFlight(
          String airline,
          String supplier,
          Double fare,
          String departureAirport,
          String destinationAirport,
          Instant departureTime,
          Instant arrivalTime) {

    validateRequiredFields(airline,
                           supplier,
                           fare,
                           departureAirport,
                           destinationAirport,
                           departureTime,
                           arrivalTime);

    return Flight.builder()
                 .airline(airline)
                 .supplier(supplier)
                 .fare(fare)
                 .departureAirport(departureAirport.toUpperCase())
                 .destinationAirport(destinationAirport.toUpperCase())
                 .departureTime(departureTime)
                 .arrivalTime(arrivalTime)
                 .build();
  }

  /**
   * Updates this flight with new values.
   * Only updates non-null values.
   *
   * @param airline the new airline name (optional)
   * @param supplier the new supplier name (optional)
   * @param fare the new fare (optional)
   * @param departureAirport the new departure airport (optional)
   * @param destinationAirport the new destination airport (optional)
   * @param departureTime the new departure time (optional)
   * @param arrivalTime the new arrival time (optional)
   */
  public void updateFlight(
          String airline,
          String supplier,
          Double fare,
          String departureAirport,
          String destinationAirport,
          Instant departureTime,
          Instant arrivalTime) {

    if (airline != null) {
      this.airline = airline;
    }
    if (supplier != null) {
      this.supplier = supplier;
    }
    if (fare != null) {
      this.fare = fare;
    }
    if (departureAirport != null) {
      this.departureAirport = departureAirport.toUpperCase();
    }
    if (destinationAirport != null) {
      this.destinationAirport = destinationAirport.toUpperCase();
    }
    if (departureTime != null) {
      this.departureTime = departureTime;
    }
    if (arrivalTime != null) {
      this.arrivalTime = arrivalTime;
    }

    validateBusinessRules();
  }

  /**
   * Validates that all required fields are provided and valid.
   */
  private static void validateRequiredFields(
          String airline,
          String supplier,
          Double fare,
          String departureAirport,
          String destinationAirport,
          Instant departureTime,
          Instant arrivalTime) {

    if (airline == null || airline.trim().isEmpty()) {
      throw new IllegalArgumentException("Airline is required");
    }
    if (supplier == null || supplier.trim().isEmpty()) {
      throw new IllegalArgumentException("Supplier is required");
    }
    if (fare == null) {
      throw new IllegalArgumentException("Fare is required");
    }
    if (departureAirport == null || !departureAirport.matches("^[A-Z]{3}$")) {
      throw new IllegalArgumentException(
              "Departure airport must be a 3-letter code");
    }
    if (destinationAirport == null || !destinationAirport.matches("^[A-Z]{3}$")) {
      throw new IllegalArgumentException(
              "Destination airport must be a 3-letter code");
    }
    if (departureTime == null) {
      throw new IllegalArgumentException("Departure time is required");
    }
    if (arrivalTime == null) {
      throw new IllegalArgumentException("Arrival time is required");
    }
  }
}