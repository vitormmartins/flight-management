package com.flightdata.management.application.mapper;

import com.flightdata.management.domain.model.Flight;
import com.flightdata.management.infrastructure.external.dto.CrazySupplierDTO;
import com.flightdata.management.application.dto.FlightCreateRequest;
import com.flightdata.management.application.dto.FlightResponse;
import com.flightdata.management.application.dto.FlightUpdateRequest;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.OffsetDateTime;

/**
 * MapStruct mapper for Flight entity and DTOs.
 * Handles bidirectional mapping between domain entities and API DTOs.
 *
 * @author Vítor Matosinho Martins
 * @version 1.0
 * @since 2025-10-24
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
@Component
public interface FlightMapper {

  /**
   * Maps Flight entity to FlightResponse DTO.
   *
   * @param flight the flight entity
   * @return the response DTO
   */
  @Mapping(target = "departureTime", source = "departureTime")
  @Mapping(target = "arrivalTime", source = "arrivalTime")
  FlightResponse toResponse(Flight flight);

  /**
   * Maps FlightCreateRequest to Flight entity.
   *
   * @param request the creation request
   * @return the flight entity
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Flight toEntity(FlightCreateRequest request);

  /**
   * Updates Flight entity from FlightUpdateRequest.
   * Only updates non-null values.
   *
   * @param request the update request
   * @param flight the existing flight entity to update
   */
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntityFromRequest(FlightUpdateRequest request, @MappingTarget Flight flight);

  /**
   * Maps CrazySupplier response to FlightResponse DTO.
   * Handles timezone conversion from CET to UTC.
   *
   * @param response the CrazySupplier response
   * @return the flight response DTO
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "airline", source = "carrier")
  @Mapping(target = "supplier", constant = "CrazySupplier")
  @Mapping(target = "fare", expression = "java(response.getTotalFare())")
  @Mapping(target = "departureAirport", source = "departureAirportName")
  @Mapping(target = "destinationAirport", source = "arrivalAirportName")
  @Mapping(target = "departureTime", source = "outboundDateTime", qualifiedByName = "localDateTimeToInstant")
  @Mapping(target = "arrivalTime", source = "inboundDateTime", qualifiedByName = "localDateTimeToInstant")
  FlightResponse crazySupplierToResponse(CrazySupplierDTO.Response response);

  /**
   * Converts LocalDateTime (CET timezone) to Instant (UTC).
   * CrazySupplier uses CET timezone, we store/return UTC.
   *
   * @param localDateTime the local date-time in CET
   * @return instant in UTC
   */
  @Named("localDateTimeToInstant")
  default OffsetDateTime localDateTimeToInstant(LocalDateTime localDateTime) {
    if (localDateTime == null) {
      return null;
    }
    // CrazySupplier uses CET timezone (UTC+1, or UTC+2 during DST)
    ZoneId cetZone = ZoneId.of("CET");
    return localDateTime.atZone(cetZone).toOffsetDateTime();
  }

  /**
   * Converts Instant to OffsetDateTime for API responses.
   *
   * @param instant the instant to convert
   * @return offset date-time in UTC
   */
  default OffsetDateTime instantToOffsetDateTime(Instant instant) {
    if (instant == null) {
      return null;
    }
    return instant.atOffset(ZoneOffset.UTC);
  }

  /**
   * Converts OffsetDateTime to Instant for entity storage.
   *
   * @param offsetDateTime the offset date-time
   * @return instant
   */
  default Instant offsetDateTimeToInstant(OffsetDateTime offsetDateTime) {
    if (offsetDateTime == null) {
      return null;
    }
    return offsetDateTime.toInstant();
  }
}
