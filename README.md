# Room Reservation Service

**Room Reservation Service** is a Spring Boot-based microservice for managing hotel room reservations. It supports multiple payment methods (Cash, Bank Transfer, Credit Card), ensures concurrency-safe booking, and integrates with Kafka for asynchronous payment events.

---

## Table of Contents

* [Features](#features)
* [Tech Stack](#tech-stack)
* [Architecture](#architecture)
* [Getting Started](#getting-started)
* [Configuration](#configuration)
* [Database](#database)
* [API Endpoints](#api-endpoints)
* [Payment Handling](#payment-handling)
* [Testing](#testing)
* [Scheduler](#scheduler)
* [Error Handling](#error-handling)

---

## Features

* Room reservation with **availability check**
* Supports **Cash, Bank Transfer, and Credit Card payments**
* Bank Transfer payment processed asynchronously via **Kafka events**
* Credit Card payment validated via **external service integration**
* **Optimistic locking** for concurrency control
* Idempotent handling of multiple payment events
* Daily automatic cancellation of pending bank transfer reservations
* Input validation with **Jakarta Bean Validation**
* Detailed error handling and consistent API responses

---

## Tech Stack

* **Java 25**
* **Spring Boot 4.0.2**

    * Spring WebMVC
    * Spring WebFlux
    * Spring Data JPA
    * Spring Kafka
    * Spring Validation
* **PostgreSQL** (production) 
* **Kafka** (Bank Transfer Payment events)
* **JUnit 5 + Mockito** for unit testing
* **Lombok** for boilerplate reduction

---

## Architecture

* **ReservationService** – core business logic
* **PaymentStrategyFactory** – selects appropriate payment strategy
* **BankTransferPaymentListener** – Kafka listener for bank transfer confirmations
* **CreditCardPaymentClient** – external HTTP client for credit card payments
* **ReservationRepository** – JPA repository with projections and custom queries
* **Scheduler** – nightly task to cancel pending bank transfer reservations
* **GlobalExceptionHandler** – central exception handling for REST API

---

## Getting Started

### Prerequisites

* Java 25
* PostgreSQL database 
* Docker
* Kafka broker running
* Maven/Gradle build tool

### Running the Application

```bash
# Run Containers
docker compose -f docker-compose-dev.yaml up -d
 
# Build project
./gradlew clean build

# Run the service
./gradlew bootRun

# Kafka Profile
# Since there is no Kafka broker running locally, the Kafka-related beans are activated
# only when the 'kafka' profile is active.
# To run the application with Kafka enabled:
./gradlew bootRun --args='--spring.profiles.active=kafka'

```

---

## Configuration

All application configuration is located in `application.properties`:

---

## Database

**Reservation Table**

| Column           | Type    | Notes                                                |
| ---------------- |---------| ---------------------------------------------------- |
| id               | LONG    | PK, auto-generated                                   |
| roomNumber       | INT     | Room number                                          |
| customerName     | VARCHAR | Not null                                             |
| startDate        | DATE    | Not null                                             |
| endDate          | DATE    | Not null                                             |
| roomSegment      | ENUM    | SMALL, MEDIUM, LARGE, EXTRA_LARGE                    |
| paymentMode      | ENUM    | CASH, BANK_TRANSFER, CREDIT_CARD                     |
| status           | ENUM    | PENDING_PAYMENT, CONFIRMED, CANCELLED                |
| paymentReference | VARCHAR | Optional for Bank Transfer, required for Credit Card |
| totalAmount      | LONG  | Must be positive                                     |
| version          | LONG  | Optimistic lock version                              |

---

## API Endpoints

**Base URL:** `/api/v1/reservations`

| Method | Endpoint               | Request Body       | Response            | Description                                                            |
| ------ | ---------------------- | ------------------ | ------------------- | ---------------------------------------------------------------------- |
| POST   | `/confirm-reservation` | ReservationRequest | ReservationResponse | Confirms a reservation and processes payment according to payment mode |

**Example Request**

```json
{
  "roomNumber": 101,
  "customerName": "Alex Bill",
  "reservationStartDate": "2026-02-02",
  "reservationEndDate": "2026-02-15",
  "roomSegment": "EXTRA_LARGE",
  "paymentMode": "CREDIT_CARD",
  "paymentReference": "ABC123",
  "totalAmount": 500
}
```

---

## Payment Handling

* **Cash:** Reservation is immediately confirmed.
* **Bank Transfer:** Reservation remains `PENDING_PAYMENT`. Kafka event triggers confirmation once payment is received.
* **Credit Card:** Synchronous validation against an external payment service. Uses WebClient.

**Idempotency:** Multiple payment events for the same reservation are ignored if already processed.

---

## Testing

* Unit tests written with **JUnit5 + Mockito**
* Test coverage includes:

    * Reservation confirmation for all payment types
    * Bank Transfer payment events
    * Edge cases (partial payment, missing references, max 30-day reservations)
    * Scheduler logic for cancelling pending reservations

**Running Tests**

```bash
./gradlew test
```

---

## Scheduler

* Runs **daily at midnight**
* Cancels pending bank transfer reservations that are within 2 days of start date
* Uses `ReservationExpirationScheduler` with `@Scheduled`

---

## Error Handling

* Global exception handling with consistent JSON API responses:

    * `400 BAD REQUEST` – Validation errors
    * `404 NOT FOUND` – Reservation not found
    * `409 CONFLICT` – Room Already Booked 
    * `500 INTERNAL SERVER ERROR` – Unexpected errors

**Example Error Response**

```json
{
  "status": "BAD_REQUEST",
  "message": "Validation failed",
  "timestamp": "2026-01-31T12:00:00",
  "validationErrors": {
    "reservationStartDate": "Reservation start date cannot be in the past"
  }
}
```

---

