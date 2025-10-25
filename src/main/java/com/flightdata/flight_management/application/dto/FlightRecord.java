package com.flightdata.flight_management.application.dto;

import java.time.Instant;

/**
 * DTO for update flight request to avoid long parameter lists.
 */
public record FlightRecord(
        String airline,
        String supplier,
        Double fare,
        String departureAirport,
        String destinationAirport,
        Instant departureTime,
        Instant arrivalTime
) {

}

