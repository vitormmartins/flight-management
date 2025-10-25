-- Flight Data Management Application - Database Initialization Script
-- Creates the database schema and sample data

-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS flightdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE flightdb;

-- Create flights table
CREATE TABLE IF NOT EXISTS flights (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    airline VARCHAR(100) NOT NULL,
    supplier VARCHAR(100) NOT NULL,
    fare DECIMAL(10, 2) NOT NULL CHECK (fare >= 0),
    departure_airport CHAR(3) NOT NULL,
    destination_airport CHAR(3) NOT NULL,
    departure_time TIMESTAMP NOT NULL,
    arrival_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Indexes for performance
    INDEX idx_departure_airport (departure_airport),
    INDEX idx_destination_airport (destination_airport),
    INDEX idx_airline (airline),
    INDEX idx_departure_time (departure_time),
    INDEX idx_supplier (supplier),
    INDEX idx_route (departure_airport, destination_airport),

    -- Constraints
    CONSTRAINT chk_different_airports CHECK (departure_airport != destination_airport),
    CONSTRAINT chk_valid_times CHECK (arrival_time > departure_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample data for testing
INSERT INTO flights (airline, supplier, fare, departure_airport, destination_airport, departure_time, arrival_time) VALUES
('American Airlines', 'GlobalSupplier', 299.99, 'JFK', 'LAX', '2025-10-25 10:30:00', '2025-10-25 16:45:00'),
('Delta Airlines', 'GlobalSupplier', 320.50, 'JFK', 'LAX', '2025-10-25 14:00:00', '2025-10-25 20:15:00'),
('United Airlines', 'GlobalSupplier', 285.00, 'JFK', 'SFO', '2025-10-25 08:15:00', '2025-10-25 14:30:00'),
('American Airlines', 'GlobalSupplier', 450.75, 'JFK', 'LHR', '2025-10-26 18:00:00', '2025-10-27 06:15:00'),
('British Airways', 'GlobalSupplier', 475.00, 'JFK', 'LHR', '2025-10-26 20:30:00', '2025-10-27 08:45:00'),
('Lufthansa', 'EuroSupplier', 520.00, 'JFK', 'FRA', '2025-10-26 17:45:00', '2025-10-27 07:30:00'),
('Air France', 'EuroSupplier', 495.50, 'JFK', 'CDG', '2025-10-26 19:15:00', '2025-10-27 08:45:00'),
('Emirates', 'GlobalSupplier', 850.00, 'JFK', 'DXB', '2025-10-27 22:00:00', '2025-10-28 18:30:00'),
('Singapore Airlines', 'AsiaSupplier', 920.00, 'JFK', 'SIN', '2025-10-28 01:00:00', '2025-10-29 06:30:00'),
('Qantas', 'AsiaSupplier', 1100.00, 'JFK', 'SYD', '2025-10-28 10:00:00', '2025-10-29 18:45:00'),
('American Airlines', 'GlobalSupplier', 310.00, 'LAX', 'JFK', '2025-10-25 07:00:00', '2025-10-25 15:15:00'),
('Delta Airlines', 'GlobalSupplier', 295.00, 'LAX', 'JFK', '2025-10-25 11:30:00', '2025-10-25 19:45:00'),
('Southwest Airlines', 'GlobalSupplier', 275.00, 'LAX', 'LAS', '2025-10-25 09:00:00', '2025-10-25 10:15:00'),
('Alaska Airlines', 'GlobalSupplier', 189.99, 'LAX', 'SEA', '2025-10-25 12:00:00', '2025-10-25 14:45:00'),
('JetBlue', 'GlobalSupplier', 249.00, 'LAX', 'BOS', '2025-10-25 06:30:00', '2025-10-25 14:50:00');

-- Create view for flight statistics (optional, for reporting)
CREATE OR REPLACE VIEW flight_statistics AS
SELECT
    airline,
    COUNT(*) as total_flights,
    AVG(fare) as average_fare,
    MIN(fare) as min_fare,
    MAX(fare) as max_fare,
    COUNT(DISTINCT departure_airport) as unique_origins,
    COUNT(DISTINCT destination_airport) as unique_destinations
FROM flights
GROUP BY airline;

-- Create view for route statistics
CREATE OR REPLACE VIEW route_statistics AS
SELECT
    departure_airport,
    destination_airport,
    COUNT(*) as flight_count,
    AVG(fare) as average_fare,
    COUNT(DISTINCT airline) as airline_count
FROM flights
GROUP BY departure_airport, destination_airport
ORDER BY flight_count DESC;

-- Grant privileges to application user
GRANT SELECT, INSERT, UPDATE, DELETE ON flightdb.* TO 'flightuser'@'%';
FLUSH PRIVILEGES;

-- Display summary
SELECT 'Database initialization completed!' as Status;
SELECT COUNT(*) as 'Total Flights' FROM flights;
SELECT COUNT(DISTINCT airline) as 'Unique Airlines' FROM flights;
SELECT COUNT(DISTINCT departure_airport) as 'Unique Departure Airports' FROM flights;
SELECT COUNT(DISTINCT destination_airport) as 'Unique Destination Airports' FROM flights;