package com.flightdata.flight_management.infrastructure.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Objects for CrazySupplier API communication.
 *
 * <p>CrazySupplier API uses CET (Central European Time) timezone for all date/time fields.
 * These DTOs handle the conversion between CET (external API) and UTC (internal storage).
 *
 * @author Flight Data Management Team
 * @version 1.0
 * @since 2025-10-24
 */
public class CrazySupplierDTO {

  /**
   * Request DTO for CrazySupplier API.
   *
   * <p>Used when querying the CrazySupplier API for available flights.
   * All date fields are in CET timezone as required by the external API.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Request {

    /**
     * 3-letter departure airport code (IATA format).
     * Example: "JFK", "LAX", "FRA"
     */
    @JsonProperty("from")
    private String from;

    /**
     * 3-letter destination airport code (IATA format).
     * Example: "JFK", "LAX", "FRA"
     */
    @JsonProperty("to")
    private String to;

    /**
     * Outbound date in ISO_LOCAL_DATE format (CET timezone).
     * Format: "YYYY-MM-DD"
     * Example: "2025-10-25"
     */
    @JsonProperty("outboundDate")
    private LocalDate outboundDate;

    /**
     * Inbound date in ISO_LOCAL_DATE format (CET timezone).
     * Format: "YYYY-MM-DD"
     * Example: "2025-10-26"
     */
    @JsonProperty("inboundDate")
    private LocalDate inboundDate;
  }

  /**
   * Response DTO from CrazySupplier API.
   *
   * <p>Represents a single flight option returned by CrazySupplier.
   * The total fare is calculated as basePrice + tax.
   * All date-time fields are in CET timezone and need conversion to UTC for storage.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Response {

    /**
     * Airline/carrier name operating the flight.
     * Example: "Lufthansa", "Air France"
     */
    @JsonProperty("carrier")
    private String carrier;

    /**
     * Base price without tax.
     * Currency is assumed to be in the configured default (e.g., USD, EUR).
     * Must be non-negative.
     */
    @JsonProperty("basePrice")
    private Double basePrice;

    /**
     * Tax amount to be added to base price.
     * Must be non-negative.
     */
    @JsonProperty("tax")
    private Double tax;

    /**
     * 3-letter departure airport code (IATA format).
     * Example: "JFK", "LAX", "FRA"
     */
    @JsonProperty("departureAirportName")
    private String departureAirportName;

    /**
     * 3-letter arrival airport code (IATA format).
     * Example: "JFK", "LAX", "FRA"
     */
    @JsonProperty("arrivalAirportName")
    private String arrivalAirportName;

    /**
     * Outbound departure date-time in ISO_LOCAL_DATE_TIME format (CET timezone).
     * Format: "YYYY-MM-DDTHH:mm:ss"
     * Example: "2025-10-25T10:30:00"
     *
     * <p>Note: This is in CET timezone and must be converted to UTC for storage.
     */
    @JsonProperty("outboundDateTime")
    private LocalDateTime outboundDateTime;

    /**
     * Inbound arrival date-time in ISO_LOCAL_DATE_TIME format (CET timezone).
     * Format: "YYYY-MM-DDTHH:mm:ss"
     * Example: "2025-10-25T22:45:00"
     *
     * <p>Note: This is in CET timezone and must be converted to UTC for storage.
     */
    @JsonProperty("inboundDateTime")
    private LocalDateTime inboundDateTime;

    /**
     * Calculates the total fare (base price + tax).
     *
     * <p>This method is used to compute the final price that should be displayed
     * to users or stored in the database. If either basePrice or tax is null,
     * returns 0.0 to prevent NullPointerException.
     *
     * @return total fare as the sum of base price and tax, or 0.0 if either is null
     */
    public Double getTotalFare() {
      if (basePrice == null || tax == null) {
        return 0.0;
      }
      return basePrice + tax;
    }

    /**
     * Validates that this response contains all required fields.
     *
     * @return true if all required fields are non-null and valid
     */
    public boolean isValid() {
      return carrier != null && !carrier.trim().isEmpty()
              && basePrice != null && basePrice >= 0
              && tax != null && tax >= 0
              && departureAirportName != null && departureAirportName.matches("^[A-Z]{3}$")
              && arrivalAirportName != null && arrivalAirportName.matches("^[A-Z]{3}$")
              && outboundDateTime != null
              && inboundDateTime != null
              && inboundDateTime.isAfter(outboundDateTime);
    }
  }
}