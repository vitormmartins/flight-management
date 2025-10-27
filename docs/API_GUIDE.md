# Flight Data Management API - Complete Guide

## Overview

This guide provides detailed information about the Flight Data Management API endpoints, request/response formats, and usage examples.

## Base URL

```
http://localhost:8080/api/v1
```

## Authentication

Currently, the API does not require authentication. In production, implement OAuth2 or JWT authentication.

## Common Headers

```
Content-Type: application/json
Accept: application/json
```

## Response Formats

### Success Response

All successful responses return the appropriate HTTP status code and JSON body.

### Error Response

```json
{
  "timestamp": "2025-10-24T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: Airline is required",
  "path": "/api/v1/flights"
}
```

## Endpoints

### 1. Create Flight

Creates a new flight record in the database.

**Endpoint:** `POST /flights`

**Request Body:**
```json
{
  "airline": "American Airlines",
  "supplier": "GlobalSupplier",
  "fare": 299.99,
  "departureAirport": "JFK",
  "destinationAirport": "LAX",
  "departureTime": "2025-10-25T10:30:00Z",
  "arrivalTime": "2025-10-25T16:45:00Z"
}
```

**Validation Rules:**
- `airline`: Required, max 100 characters
- `supplier`: Required, max 100 characters
- `fare`: Required, must be >= 0
- `departureAirport`: Required, exactly 3 uppercase letters
- `destinationAirport`: Required, exactly 3 uppercase letters
- `departureTime`: Required, ISO 8601 format (UTC)
- `arrivalTime`: Required, ISO 8601 format (UTC), must be after departureTime

**Response:** `201 Created`
```json
{
  "id": 1,
  "airline": "American Airlines",
  "supplier": "GlobalSupplier",
  "fare": 299.99,
  "departureAirport": "JFK",
  "destinationAirport": "LAX",
  "departureTime": "2025-10-25T10:30:00Z",
  "arrivalTime": "2025-10-25T16:45:00Z"
}
```

**Error Responses:**
- `400 Bad Request`: Validation error
- `500 Internal Server Error`: Server error

---

### 2. Get Flight by ID

Retrieves a specific flight by its ID.

**Endpoint:** `GET /flights/{id}`

**Path Parameters:**
- `id` (required): Flight ID (integer)

**Response:** `200 OK`
```json
{
  "id": 1,
  "airline": "American Airlines",
  "supplier": "GlobalSupplier",
  "fare": 299.99,
  "departureAirport": "JFK",
  "destinationAirport": "LAX",
  "departureTime": "2025-10-25T10:30:00Z",
  "arrivalTime": "2025-10-25T16:45:00Z"
}
```

**Error Responses:**
- `404 Not Found`: Flight not found
- `500 Internal Server Error`: Server error

---

### 3. Update Flight

Updates an existing flight record. Only provided fields will be updated.

**Endpoint:** `PUT /flights/{id}`

**Path Parameters:**
- `id` (required): Flight ID (integer)

**Request Body:**
```json
{
  "airline": "Delta Airlines",
  "fare": 399.99
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "airline": "Delta Airlines",
  "supplier": "GlobalSupplier",
  "fare": 399.99,
  "departureAirport": "JFK",
  "destinationAirport": "LAX",
  "departureTime": "2025-10-25T10:30:00Z",
  "arrivalTime": "2025-10-25T16:45:00Z"
}
```

**Error Responses:**
- `400 Bad Request`: Validation error
- `404 Not Found`: Flight not found
- `500 Internal Server Error`: Server error

---

### 4. Delete Flight

Deletes a flight record.

**Endpoint:** `DELETE /flights/{id}`

**Path Parameters:**
- `id` (required): Flight ID (integer)

**Response:** `204 No Content`

**Error Responses:**
- `404 Not Found`: Flight not found
- `500 Internal Server Error`: Server error

---

### 5. Search Flights

Searches for flights with optional filters. Combines results from database and CrazySupplier API.

**Endpoint:** `GET /flights`

**Query Parameters:**
- `origin` (optional): 3-letter departure airport code (e.g., "JFK")
- `destination` (optional): 3-letter destination airport code (e.g., "LAX")
- `airline` (optional): Airline name (partial match, case-insensitive)
- `departureFrom` (optional): Minimum departure time (ISO 8601)
- `departureTo` (optional): Maximum departure time (ISO 8601)
- `arrivalFrom` (optional): Minimum arrival time (ISO 8601)
- `arrivalTo` (optional): Maximum arrival time (ISO 8601)
- `page` (optional): Page number, 0-indexed (default: 0)
- `size` (optional): Page size (default: 20, max: 100)

**Examples:**

Search all flights:
```
GET /flights?page=0&size=20
```

Search by route:
```
GET /flights?origin=JFK&destination=LAX
```

Search by airline:
```
GET /flights?airline=American
```

Search by time range:
```
GET /flights?departureFrom=2025-10-25T00:00:00Z&departureTo=2025-10-25T23:59:59Z
```

Combined search:
```
GET /flights?origin=JFK&destination=LAX&airline=American&departureFrom=2025-10-25T10:00:00Z&departureTo=2025-10-25T18:00:00Z&page=0&size=20
```

**Response:** `200 OK`
```json
{
  "flights": [
    {
      "id": 1,
      "airline": "American Airlines",
      "supplier": "GlobalSupplier",
      "fare": 299.99,
      "departureAirport": "JFK",
      "destinationAirport": "LAX",
      "departureTime": "2025-10-25T10:30:00Z",
      "arrivalTime": "2025-10-25T16:45:00Z"
    },
    {
      "id": null,
      "airline": "Lufthansa",
      "supplier": "CrazySupplier",
      "fare": 350.00,
      "departureAirport": "JFK",
      "destinationAirport": "LAX",
      "departureTime": "2025-10-25T14:00:00Z",
      "arrivalTime": "2025-10-25T20:15:00Z"
    }
  ],
  "pagination": {
    "currentPage": 0,
    "pageSize": 20,
    "totalElements": 2,
    "totalPages": 1
  }
}
```

**Notes:**
- Flights from CrazySupplier have `id: null` and `supplier: "CrazySupplier"`
- CrazySupplier results are only included when `origin` and `destination` are provided
- Airport codes are case-insensitive (automatically converted to uppercase)
- All times are in UTC timezone

**Error Responses:**
- `400 Bad Request`: Invalid query parameters
- `500 Internal Server Error`: Server error

---

## CrazySupplier Integration

The API integrates with CrazySupplier to provide additional flight options.

### How It Works

1. When searching with `origin` and `destination`, the API queries both:
    - Internal database
    - CrazySupplier API

2. Results are combined and returned together

3. CrazySupplier flights are identified by:
    - `supplier: "CrazySupplier"`
    - `id: null` (not stored in database)

### Timezone Conversion

- **CrazySupplier uses CET timezone**
- All times are converted to UTC for consistency
- You should always provide times in UTC when searching

### Error Handling

If CrazySupplier API is unavailable:
- The search continues with database results only
- No error is returned to the client (graceful degradation)
- Check logs for CrazySupplier errors

---

## Pagination

All list endpoints support pagination using `page` and `size` parameters.

**Default Values:**
- `page`: 0
- `size`: 20

**Maximum page size:** 100

**Pagination Response:**
```json
{
  "pagination": {
    "currentPage": 0,
    "pageSize": 20,
    "totalElements": 150,
    "totalPages": 8
  }
}
```

---

## Status Codes

| Code | Description |
|------|-------------|
| 200 | OK - Request successful |
| 201 | Created - Resource created successfully |
| 204 | No Content - Resource deleted successfully |
| 400 | Bad Request - Validation error or invalid input |
| 404 | Not Found - Resource not found |
| 500 | Internal Server Error - Server error |

---

## Rate Limiting

Currently, no rate limiting is implemented. Consider implementing rate limiting for production use.

---

## Best Practices

### 1. Always validate input
```java
// Frontend validation example
if (!/^[A-Z]{3}$/.test(airportCode)) {
  throw new Error('Invalid airport code');
}
```

### 2. Handle errors gracefully
```javascript
try {
  const response = await fetch('/api/v1/flights/1');
  if (!response.ok) {
    const error = await response.json();
    console.error(error.message);
  }
} catch (error) {
  console.error('Network error:', error);
}
```

### 3. Use proper date formats
Always use ISO 8601 format in UTC:
```
2025-10-25T10:30:00Z
```

### 4. Implement retry logic for external services
The CrazySupplier integration uses automatic retries, but implement client-side retries for robustness.

---

## Examples with cURL

### Create Flight
```bash
curl -X POST http://localhost:8080/api/v1/flights \
  -H "Content-Type: application/json" \
  -d '{
    "airline": "American Airlines",
    "supplier": "GlobalSupplier",
    "fare": 299.99,
    "departureAirport": "JFK",
    "destinationAirport": "LAX",
    "departureTime": "2025-10-25T10:30:00Z",
    "arrivalTime": "2025-10-25T16:45:00Z"
  }'
```

### Get Flight
```bash
curl http://localhost:8080/api/v1/flights/1
```

### Update Flight
```bash
curl -X PUT http://localhost:8080/api/v1/flights/1 \
  -H "Content-Type: application/json" \
  -d '{"fare": 399.99}'
```

### Delete Flight
```bash
curl -X DELETE http://localhost:8080/api/v1/flights/1
```

### Search Flights
```bash
curl "http://localhost:8080/api/v1/flights?origin=JFK&destination=LAX&page=0&size=20"
```

---

## Support

For questions or issues, please contact: support@flightdata.com