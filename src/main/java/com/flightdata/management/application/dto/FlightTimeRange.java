package com.flightdata.management.application.dto;

import java.time.Instant;

public record FlightTimeRange(
    Instant departureFrom,
    Instant departureTo,
    Instant arrivalFrom,
    Instant arrivalTo
) {}