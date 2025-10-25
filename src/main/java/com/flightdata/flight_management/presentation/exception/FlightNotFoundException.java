package com.flightdata.flight_management.presentation.exception;

/**
 * Custom exception for flighted not found scenarios.
 */
public class FlightNotFoundException extends RuntimeException {
  public FlightNotFoundException(String message) {
    super(message);
  }
}
