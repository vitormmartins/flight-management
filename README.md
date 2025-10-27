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
- **Pagination**: Efficient handling of large datasets
- **Data Validation**: Comprehensive input validation
- **Error Handling**: Graceful error responses with detailed messages
- **API Documentation**: Interactive Swagger UI
- **Containerization**: Docker and Docker Compose support
- **Production-Ready**: Health checks, metrics, and logging

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
│  (Application Services, Mappers)    │
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
- **MySQL 8.0**
- **Maven 3.9+**
- **OpenAPI 3.0** (Code Generation)
- **MapStruct** (Object Mapping)
- **Lombok** (Boilerplate Reduction)
- **WebClient** (Reactive HTTP Client)
- **Docker & Docker Compose**
- **JUnit 5** (Testing)
- **WireMock** (API Mocking)
- **Testcontainers** (Integration Testing)

## 📦 Prerequisites

- Java 17 or higher
- Maven 3.9 or higher
- Docker and Docker Compose (for containerized deployment)
- MySQL 8.0 (if running without Docker)

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/flight-management.git
cd flight-management
```

### 2. Build the Project

```bash
mvn clean install
```

This will:
- Generate OpenAPI classes from the specification
- Compile the application
- Run all tests
- Create the JAR file

### 3. Run with Docker Compose (Recommended)

```bash
docker-compose up -d
```

This starts:
- MySQL database on port 3306
- Flight Management App on port 8080
- phpMyAdmin on port 8081 (dev profile)

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

#### Update Flight
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
- `airline`: Airline name (partial match)
- `departureFrom`: Minimum departure time (ISO 8601)
- `departureTo`: Maximum departure time (ISO 8601)
- `arrivalFrom`: Minimum arrival time (ISO 8601)
- `arrivalTo`: Maximum arrival time (ISO 8601)
- `page`: Page number (default: 0)
- `size`: Page size (default: 20, max: 100)

For complete API documentation, visit the Swagger UI at http://localhost:8080/swagger-ui.html

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=FlightDomainServiceTest
```

### Test Coverage

The project includes:
- **Unit Tests**: Domain services, mappers, and utilities
- **Integration Tests**: Controllers with MockMvc
- **API Mocking**: WireMock for CrazySupplier API
- **Container Tests**: Testcontainers for database integration

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

# Stop and remove volumes
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
```

## 📁 Project Structure

```
flight-management/
├── src/
│   ├── main/
│   │   ├── java/com/flightdata/management/
│   │   │   ├── domain/              # Domain layer (entities, repositories)
│   │   │   ├── application/         # Application layer (services, DTOs)
│   │   │   ├── infrastructure/      # Infrastructure (config, external clients)
│   │   │   └── presentation/        # Presentation (controllers, exception handlers)
│   │   └── resources/
│   │       ├── api/                 # OpenAPI specifications
│   │       ├── application.yml      # Application configuration
│   │       └── db/migration/        # Database migrations
│   └── test/                        # Test classes
├── docs/                            # Additional documentation
├── postman/                         # Postman collections
├── docker/                          # Docker-related files
├── Dockerfile                       # Application Dockerfile
├── docker-compose.yml               # Docker Compose configuration
├── pom.xml                          # Maven configuration
└── README.md                        # This file
```

## 🔧 Configuration

### Application Profiles

- **default**: Local development
- **docker**: Docker container deployment
- **test**: Test environment with H2 database

### Key Configuration Properties

```yaml
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/flightdb
spring.datasource.username=flightuser
spring.datasource.password=flightpassword

# CrazySupplier Integration
application.crazy-supplier.base-url=https://api.crazy-supplier.com
application.crazy-supplier.timeout=5000
application.crazy-supplier.max-retries=3
application.crazy-supplier.enabled=true

# Pagination
application.pagination.default-page-size=20
application.pagination.max-page-size=100
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 Assumptions

1. **CrazySupplier API**: Returns data in CET timezone; we convert to UTC for storage
2. **Airport Codes**: All airport codes are validated as 3-letter IATA codes
3. **Fare Calculation**: CrazySupplier fare = basePrice + tax
4. **Error Handling**: CrazySupplier errors return empty results (graceful degradation)
5. **Duplicate Detection**: Flights are considered duplicates if they have the same airline, route, and departure time

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Authors

Vítor Matosinho Martins

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- OpenAPI Initiative for API specification standards
- All contributors and maintainers

---

For questions or support, please contact: support@flightdata.com