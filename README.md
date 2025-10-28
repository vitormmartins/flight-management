# Flight Data Management Application

A production-ready RESTful API for managing flight data with support for CRUD operations and integration with external suppliers like CrazySupplier.

## 📋 Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Technologies](#technologies)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Docker Deployment](#docker-deployment)
- [Project Structure](#project-structure)
- [Contributing](#contributing)

## ✨ Features

- **CRUD Operations**: Create, read, update, and delete flight records
- **Advanced Search**: Filter flights by origin, destination, airline, and time ranges
- **External Integration**: Seamless integration with CrazySupplier API
- **Pagination**: Efficient handling of large datasets (database-based pagination)
- **Data Validation**: Comprehensive input validation
- **Error Handling**: Graceful error responses with detailed messages
- **API Documentation**: Interactive Swagger UI
- **Containerization**: Docker and Docker Compose support
- **Production-Ready**: Health checks, metrics, and logging
- **Graceful Degradation**: Application continues working if external API fails

## 🏗️ Architecture

This application follows **Domain-Driven Design (DDD)** principles with a layered architecture:

```
┌─────────────────────────────────────┐
│     Presentation Layer              │
│  (Controllers, DTOs, Exception      │
│   Handlers)                         │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│     Application Layer               │
│  (Application Services, Mappers,    │
│   FlightRecord, FlightTimeRange)    │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│     Domain Layer                    │
│  (Entities, Repositories,           │
│   Domain Services)                  │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│     Infrastructure Layer            │
│  (JPA, External Clients, Config)    │
└─────────────────────────────────────┘
```

## 🛠️ Technologies

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **MySQL 8.3**
- **Maven 3.9+**
- **OpenAPI 3.0** (Code Generation)
- **MapStruct** (Object Mapping)
- **Lombok** (Boilerplate Reduction)
- **RestClient** (Modern Synchronous HTTP Client)
- **Spring Retry** (Fault Tolerance)
- **Docker & Docker Compose**
- **JUnit 5** (Testing)
- **Mockito** (Mocking Framework)
- **AssertJ** (Fluent Assertions)
- **WireMock** (API Mocking)
- **Testcontainers** (Integration Testing)

## 📦 Prerequisites

- Java 17 or higher
- Maven 3.9 or higher
- Docker and Docker Compose (for containerized deployment)
- MySQL 8.3 (if running without Docker)

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/vitormmartins/flight-management.git
cd flight-management
```

### 2. Build the Project

```bash
mvn clean install
```

This will:
- Generate OpenAPI classes from the specification
- Generate MapStruct implementations
- Compile the application
- Run all tests
- Create the JAR file

### 3. Run with Docker Compose (Recommended)

```bash
docker-compose up -d
```

This starts:
- MySQL 8.3 database on port 3306
- Flight Management App on port 8080

### 4. Run Locally (Without Docker)

Configure MySQL connection in `application.yml`, then:

```bash
mvn spring-boot:run
```

### 5. Access the Application

- **API Base URL**: http://localhost:8080/api/v1/flights
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs
- **Health Check**: http://localhost:8080/actuator/health

## 📚 API Documentation

### Endpoints

#### Create Flight
```http
POST /api/v1/flights
Content-Type: application/json

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

#### Get Flight by ID
```http
GET /api/v1/flights/{id}
```

#### Update Flight (Partial Update Supported)
```http
PUT /api/v1/flights/{id}
Content-Type: application/json

{
  "fare": 399.99
}
```

#### Delete Flight
```http
DELETE /api/v1/flights/{id}
```

#### Search Flights
```http
GET /api/v1/flights?origin=JFK&destination=LAX&airline=American&page=0&size=20
```

Query Parameters:
- `origin`: 3-letter departure airport code
- `destination`: 3-letter destination airport code
- `airline`: Airline name (partial match, case-insensitive)
- `departureFrom`: Minimum departure time (ISO 8601, UTC)
- `departureTo`: Maximum departure time (ISO 8601, UTC)
- `arrivalFrom`: Minimum arrival time (ISO 8601, UTC)
- `arrivalTo`: Maximum arrival time (ISO 8601, UTC)
- `page`: Page number (default: 0, 0-indexed)
- `size`: Page size (default: 20, max: 100)

**Note on Pagination**: When searching with origin and destination, results will include both database flights (paginated) and CrazySupplier flights (additional). Pagination info (totalElements, totalPages) reflects database results only.

For complete API documentation, visit the Swagger UI at http://localhost:8080/swagger-ui.html

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=FlightApplicationServiceTest
```

### Test Coverage

The project includes **75+ comprehensive tests**:

- **Unit Tests**: 
  - FlightMapperTest (22 tests) - Mapper logic and transformations
  - FlightDomainServiceTest (11 tests) - Business logic and validation
  - FlightApplicationServiceTest (19 tests) - Orchestration and coordination
  
- **Integration Tests**: 
  - CrazySupplierClientTest (14 tests) - External API mocking with WireMock
  - FlightControllerTest (9 tests) - REST endpoints with MockMvc

### Test Categories

| Layer | Type | Tests | Tool |
|-------|------|-------|------|
| Mapper | Unit | 22 | MapStruct, Mockito |
| Domain Service | Unit | 11 | Mockito |
| Application Service | Unit | 19 | Mockito |
| External Client | Integration | 14 | WireMock |
| Controller | Integration | 9 | MockMvc |
| **TOTAL** | | **75+** | |

### Generate Javadoc

```bash
mvn javadoc:javadoc
```

Documentation will be available in `target/site/apidocs/`

## 🐳 Docker Deployment

### Build Docker Image

```bash
docker build -t flight-management:1.0.0 .
```

### Run with Docker Compose

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop all services
docker-compose down

# Stop and remove volumes (fresh start)
docker-compose down -v
```

### Environment Variables

Configure the application using environment variables:

```bash
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/flightdb
SPRING_DATASOURCE_USERNAME=flightuser
SPRING_DATASOURCE_PASSWORD=flightpassword
CRAZY_SUPPLIER_BASE_URL=https://api.crazy-supplier.com
CRAZY_SUPPLIER_TIMEOUT=5000
CRAZY_SUPPLIER_MAX_RETRIES=3
CRAZY_SUPPLIER_ENABLED=true
```

## 📁 Project Structure

```
Flight-Data-Management-Application/
├── src/
│   ├── main/
│   │   ├── java/com/flightdata/management/
│   │   │   ├── domain/              # Domain layer
│   │   │   │   ├── model/           # Entities (Flight)
│   │   │   │   ├── repository/      # Repository interfaces
│   │   │   │   └── service/         # Domain services
│   │   │   ├── application/         # Application layer
│   │   │   │   ├── dto/             # DTOs (FlightRecord, FlightTimeRange)
│   │   │   │   ├── mapper/          # MapStruct mappers
│   │   │   │   └── service/         # Application services
│   │   │   ├── infrastructure/      # Infrastructure layer
│   │   │   │   ├── persistence/     # JPA implementations
│   │   │   │   ├── external/        # External API clients
│   │   │   │   └── config/          # Configuration classes
│   │   │   └── presentation/        # Presentation layer
│   │   │       ├── controller/      # REST controllers
│   │   │       └── exception/       # Exception handlers
│   │   └── resources/
│   │       ├── api/                 # OpenAPI specifications
│   │       └── application.yml      # Application configuration
│   └── test/                        # Test classes (75+ tests)
├── docs/                            # Additional documentation
├── postman/                         # Postman collections
├── docker/                          # Docker-related files
│   └── mysql/init/                  # Database initialization scripts
├── Dockerfile                       # Multi-stage Docker build
├── docker-compose.yml               # Docker Compose configuration
├── pom.xml                          # Maven configuration
└── README.md                        # This file
```

## 🔧 Configuration

### Application Profiles

- **default**: Local development with local MySQL
- **docker**: Docker container deployment
- **test**: Test environment with H2 in-memory database

### Key Configuration Properties

```yaml
# Database
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/flightdb
    username: flightuser
    password: flightpassword

# CrazySupplier Integration
application:
  crazy-supplier:
    base-url: https://api.crazy-supplier.com
    timeout: 5000
    max-retries: 3
    enabled: true

# Pagination
  pagination:
    default-page-size: 20
    max-page-size: 100
```

## 🔑 Key Design Decisions

### Why RestClient instead of WebClient?

**Decision**: Use `RestClient` (synchronous) instead of `WebClient` (reactive)

**Reasoning**:
- Application uses blocking JPA/Hibernate for database access
- Mixing blocking and non-blocking operations creates thread pool issues
- `RestClient` is Spring's modern synchronous HTTP client (Spring 6+)
- Better performance for blocking architectures
- Simpler code and easier debugging

For a fully reactive solution, would need to replace JPA with R2DBC.

### Why FlightRecord and FlightTimeRange?

**FlightRecord**: Immutable data transfer object between application and domain layers
- Reduces method parameter count
- Groups related data together
- Thread-safe and immutable
- Clear separation of concerns

**FlightTimeRange**: Encapsulates time filtering criteria
- Validates time ranges at construction
- Reduces coupling between layers
- Makes APIs cleaner and more maintainable

### Pagination Strategy

**Database-only pagination**: Pagination info (totalElements, totalPages) is based on database results only, not including CrazySupplier results.

**Reasoning**:
- CrazySupplier data cannot be reliably paginated
- External API might be slow or unavailable
- Provides consistent pagination experience
- CrazySupplier results are "bonus data" added to each page

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 Key Features & Assumptions

### Features
1. **CrazySupplier Integration**: 
   - Timezone conversion (CET ↔ UTC)
   - Automatic retry with exponential backoff
   - Graceful degradation on failure

2. **Search & Filter**:
   - Multiple filter combinations
   - Partial airline name matching (case-insensitive)
   - Time range filtering for departure and arrival
   - Results combined from database and external API

3. **CRUD Operations**:
   - Full and partial updates supported
   - Business rule validation at domain level
   - Transactional operations

### Assumptions
1. **CrazySupplier API**: Returns data in CET timezone; we convert to UTC for storage
2. **Airport Codes**: All airport codes are validated as 3-letter IATA codes (uppercase)
3. **Fare Calculation**: CrazySupplier total fare = basePrice + tax
4. **Error Handling**: CrazySupplier errors return empty results (graceful degradation)
5. **Duplicate Detection**: Flights are considered duplicates if they have the same airline, route, and departure time
6. **Pagination**: Calculated from database results only; CrazySupplier results are additional

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Authors

Vítor Matosinho Martins

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- OpenAPI Initiative for API specification standards
- All contributors and maintainers

---
