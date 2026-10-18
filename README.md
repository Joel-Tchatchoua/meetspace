# MeetSpace — Project Description

**Goal**: Build MeetSpace, a meeting room reservation platform, as a small set of independent microservices. This document describes **what** to build. **How** to build it is up to you, as long as you use the required stack and meet the requirements below. Section 10 lists how your work will be assessed.

---

## Table of Contents

1. [Product Overview](#1-product-overview)
2. [Technical Stack](#2-technical-stack)
3. [Architecture](#3-architecture)
4. [Domain Model](#4-domain-model)
5. [Security](#5-security)
6. [Room Service — Features & API](#6-room-service--features--api)
7. [Booking Service — Features & API](#7-booking-service--features--api)
8. [Business Rules](#8-business-rules)
9. [Communication Between Services](#9-communication-between-services)
10. [Testing Expectations](#10-testing-expectations)
11. [Deliverables & Assessment](#11-deliverables--assessment)

---

## 1. Product Overview

MeetSpace lets employees of a company with several office sites find and book meeting rooms.

An employee chooses a site, a date, and a time slot. The application then shows which rooms are free, and the employee can filter by capacity and equipment. The employee books a room and invites colleagues. When the meeting starts, the employee checks in. A room that nobody checks into is released automatically so others can use it.

Administrators manage sites, rooms, and user accounts.

### Users

| Role | What they can do |
|---|---|
| **Employee** | Register, log in, manage their profile, browse rooms, book, reschedule, cancel, transfer, invite colleagues, respond to invitations, and check in |
| **Administrator** | Everything an employee can do, plus manage sites and rooms, manage users, and see or cancel any reservation |

### The two microservices

| Service | Responsibility |
|---|---|
| **room-service** | Sites, meeting rooms, room equipment, and **room availability per site** |
| **booking-service** | **Reservations**, invitations, check-in, and **user management** (registration, login, profiles, administration) |

---

## 2. Technical Stack

Use the following technologies. Do not substitute them without discussing it with your mentor first.

| Area | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4 |
| Build | Maven, as a multi-module repository |
| Persistence | Spring Data JPA with MySQL 8, using one database schema per service |
| Database migrations | Flyway |
| Messaging | RabbitMQ |
| Identity provider | Keycloak 26 |
| Security | Spring Security as an OAuth2 Resource Server, validating JWTs issued by Keycloak |
| Service-to-service calls | Spring HTTP interfaces (`@HttpExchange`) |
| Boilerplate reduction | Lombok and MapStruct |
| Tests | JUnit 5, Testcontainers for real MySQL and RabbitMQ, and `WebTestClient` |
| Local environment | Docker Compose |

### Expected practices

- Enable **virtual threads**.
- Make all DTOs and API responses Java **records**.
- Bind all configuration to **`@ConfigurationProperties` records**. Never hard-code values or secrets.
- Return every error in the **Problem Details (RFC 9457)** format.
- Write security configuration in the **lambda DSL** style.
- Retry calls to the other service using Spring's built-in resilience support (`@Retryable`).
- Note that Spring Boot 4 uses **Jackson 3**, and that `@MockBean` has been replaced by `@MockitoBean`.

---

## 3. Architecture

### 3.1 Overview

```
                 ┌────────────┐
                 │  Keycloak  │  issues all tokens
                 └─────┬──────┘
          ┌────────────┴─────────────┐
┌─────────▼────────┐  REST calls  ┌──▼───────────────┐
│   room-service   │◄─────────────│ booking-service  │
│   own database   │              │  own database    │
│                  │◄─────────────│                  │
└──────────────────┘   events     └──────────────────┘
                     (RabbitMQ)
```

### 3.2 Key principles

- **Each service owns its data.** A service never reads or writes the other service's database. When one service refers to data in the other, it stores only an ID, with no foreign key.
- **Communication goes one way.**
  - booking-service **calls** room-service through REST to get room details (capacity, status, and the site's timezone) when someone books.
  - booking-service **publishes events** whenever a reservation is created, rescheduled, cancelled, released, or completed.
  - room-service **listens** to those events and keeps its own record of which rooms are occupied. It uses that record to answer availability searches.
  - room-service never calls booking-service.
- **The services are independent.** Each one builds, runs, and deploys on its own, with its own Dockerfile.

### 3.3 Repository structure

| Module | Type | Content |
|---|---|---|
| `meetspace-parent` | Parent POM | Shared versions, plugins, and build rules |
| `meetspace-common` | Library | Base entity classes, JWT role conversion, common error handling |
| `meetspace-events` | Library | Definitions of the events exchanged between the services |
| `room-service` | Application | The room microservice |
| `booking-service` | Application | The booking and user microservice |

The two application modules must **not** depend on each other.

Use `com.accenture.meetspace` as the base package.

---

## 4. Domain Model

Every table has a technical ID and creation and update timestamps, stored in UTC. Sites, rooms, user profiles, and reservations use **optimistic locking** to detect conflicting updates.

### 4.1 room-service data

**Site**: an office location.
- It has a unique code, a unique name, an address, and a timezone. The timezone defaults to `Indian/Mauritius`.
- Sites are **soft-deleted**: they are never removed from the database.
- A site that still has rooms cannot be deleted.

**Meeting room**: a room that belongs to a site.
- It has a code (unique within its site), a name, a floor, a capacity (1 to 200 people), and a status: `ACTIVE`, `MAINTENANCE`, or `CLOSED`.
- It has a list of **amenities**: projector, video conference, whiteboard, TV screen, conference phone, and wheelchair access.
- Rooms are soft-deleted.

**Room occupancy**: room-service's own record of when each room is busy.
- It stores the reservation ID, the room, the start and end times, and whether the occupancy is still active.
- It is filled **only** from events sent by booking-service. It cannot be changed through the API.
- room-service also records which events it has already processed, so that receiving the same event twice has no effect.

### 4.2 booking-service data

**User profile**: one per registered user.
- It holds the Keycloak user ID, the username and email (both unique), first and last name, and optionally a department, a phone number, and a default site.
- It also stores the user's role (`ADMIN` or `EMPLOYEE`) and an active/inactive flag.

**Reservation**: a booking of one room by one organizer.
- It has a title, an optional description, start and end times, the number of attendees, and a status.
- The possible statuses are `CONFIRMED`, `CHECKED_IN`, `COMPLETED`, `CANCELLED`, and `NO_SHOW`.
- When the reservation is booked, it saves a **copy** of the room's code and name and the site's name and timezone. This copy must never change afterwards, even if the room is renamed later.

**Reservation attendee**: a colleague invited to a reservation.
- Each attendee has a response: `INVITED`, `ACCEPTED`, or `DECLINED`.
- A user can appear only once per reservation.

**Outbox**: a table of events waiting to be sent to RabbitMQ (see Section 9).

---

## 5. Security

All tokens come from **Keycloak**. Both services only validate tokens; they never create them.

### 5.1 Roles

| Keycloak role | Used by |
|---|---|
| `ADMIN` | Administrators |
| `EMPLOYEE` | Employees |
| `SERVICE` | booking-service, when it calls room-service |

### 5.2 Requirements

- Read user roles from the Keycloak token's `realm_access.roles` claim. Use `preferred_username` as the username.
- Write this logic once in the common module, so both services share it.
- Check roles on each endpoint with `@PreAuthorize`.
- Make the register, login, logout, and health endpoints public. Every other endpoint requires a valid token.
- When booking-service calls room-service, authenticate with its **own service-account token** obtained from Keycloak, not with the user's token.
- When a deactivated user calls any endpoint, return `403`, even if their token is still valid.
- Never put secrets in the code or in configuration files. Read them from environment variables instead.

---

## 6. Room Service — Features & API

Base path: `/api/v1`.

All list endpoints are **paginated**. Use `page`, `size`, and `sort` query parameters, and return the result in this shape:

```json
{ "content": [ ... ], "page": { "size": 20, "number": 0, "totalElements": 1, "totalPages": 1 } }
```

### 6.1 Sites

| Endpoint | Who | Description |
|---|---|---|
| `GET /sites` | Everyone logged in | List the sites that have not been deleted |
| `GET /sites/{id}` | Everyone logged in | Get one site, or `404` if it does not exist |
| `POST /sites` | Admin | Create a site. Returns `409` if the code or name already exists |
| `PUT /sites/{id}` | Admin | Update a site |
| `DELETE /sites/{id}` | Admin | Soft-delete a site. Returns `409` if it still has rooms |

### 6.2 Rooms

| Endpoint | Who | Description |
|---|---|---|
| `GET /sites/{siteId}/rooms` | Everyone logged in | List a site's rooms. Can filter by minimum capacity, amenities, floor, and status |
| `GET /rooms/{id}` | Everyone logged in | Get room details, including its site and the site's timezone |
| `POST /sites/{siteId}/rooms` | Admin | Create a room |
| `PUT /rooms/{id}` | Admin | Update a room |
| `PATCH /rooms/{id}/status` | Admin | Change a room's status, for example to put it into maintenance |
| `DELETE /rooms/{id}` | Admin | Soft-delete a room |

### 6.3 Availability (the main feature of this service)

**`GET /sites/{siteId}/rooms/available`** returns the rooms of a site that are free for the **entire** requested time slot.

The caller provides:
- **Required:** a date, a start time, and an end time, all in the site's **local time**.
- **Optional:** a minimum capacity and a list of required amenities.

A room is returned only if all of these are true:
- it belongs to the site and has not been deleted;
- its status is `ACTIVE`;
- it is big enough and has every requested amenity;
- no active occupancy overlaps the requested slot. A meeting that ends exactly when the requested slot starts does **not** count as overlapping.

Filter the rooms in the database query, not by loading all rooms and filtering them in Java.

**`GET /rooms/{id}/schedule?date=...`** shows the busy time ranges of one room on one day. It returns only the time ranges, never who booked the room or why.

**Note**: availability data can lag a few seconds behind actual bookings, because it is updated through events. booking-service always re-checks availability before it confirms a booking.

---

## 7. Booking Service — Features & API

Base path: `/api/v1`.

### 7.1 Authentication

| Endpoint | Who | Description |
|---|---|---|
| `POST /auth/register` | Public | Create an account with the `EMPLOYEE` role |
| `POST /auth/login` | Public | Exchange a username and password for tokens from Keycloak |
| `POST /auth/logout` | Public | Revoke the refresh token |

Registration rules:
- The username and email must not already be taken. Otherwise, return `409`.
- The password and its confirmation must match. Write this as a custom validation annotation.
- The password must be strong: at least 8 characters, including an upper-case letter, a digit, and a symbol.
- Registration creates the user in Keycloak, assigns the `EMPLOYEE` role, and creates the user profile.
- If saving the profile fails, delete the Keycloak user again, so that no half-created accounts are left behind.

### 7.2 Profile and user management

| Endpoint | Who | Description |
|---|---|---|
| `GET /profile` | Logged in | Show my profile |
| `PATCH /profile` | Logged in | Update my profile. Only the fields I send are changed. If I set a default site, it must exist in room-service |
| `GET /users/directory` | Logged in | Search active colleagues by name, for example to invite them. Returns only basic public fields |
| `GET /users` | Admin | List all users, with filters for username, email, department, role, and active status |
| `GET /users/{id}` | Admin | Get one user |
| `PATCH /users/{id}/status` | Admin | Activate or deactivate a user (see the rules below) |
| `PUT /users/{id}/role` | Admin | Change a user's role, in both Keycloak and the profile |

Rules for status and role changes:
- Deactivating a user also disables them in Keycloak and **cancels all their upcoming reservations**.
- An admin cannot deactivate their own account.
- The last active admin cannot be demoted.

### 7.3 Reservations

| Endpoint | Who | Description |
|---|---|---|
| `POST /reservations` | Logged in | Book a room for a date and time slot, with a title, invitees, and an optional number of external guests |
| `GET /reservations` | Logged in | List reservations I organize, filtered by status and date range |
| `GET /reservations/invitations` | Logged in | List reservations I am invited to |
| `GET /reservations/{id}` | Organizer, attendee, or admin | Get reservation details. Returns `404` for anyone else |
| `PUT /reservations/{id}` | Organizer | Reschedule, or change the title or invitees. The room cannot be changed |
| `DELETE /reservations/{id}` | Organizer or admin | Cancel a confirmed reservation, with an optional reason |
| `POST /reservations/{id}/transfer` | Organizer | Hand the reservation over to another user |
| `POST /reservations/{id}/check-in` | Organizer or accepted attendee | Confirm the meeting is taking place |
| `POST /reservations/{id}/attendees/me/response` | Invitee | Accept or decline an invitation |
| `GET /admin/reservations` | Admin | List all reservations, filtered by site, room, status, and dates |

Example of a booking request:

```json
{
  "roomId": 12,
  "title": "Sprint planning",
  "date": "2026-11-03",
  "startTime": "09:00",
  "endTime": "10:00",
  "inviteeUsernames": ["jsmith", "akumar"],
  "externalGuests": 1
}
```

The number of attendees is the organizer, plus the invitees, plus the external guests.

A reservation response should include:
- the room and site, taken from the saved copy;
- the organizer;
- the start and end times, both in UTC and in the site's local time;
- the attendees and their responses;
- the number of attendees and the status.

---

## 8. Business Rules

### 8.1 Booking rules

When someone creates or reschedules a reservation, check the rules **in this order**. Stop at the first rule that fails.

| # | Rule | If it fails |
|---|---|---|
| 1 | The end time is after the start time | 400 |
| 2 | The times are aligned to 15-minute slots | 400 |
| 3 | The room exists. booking-service asks room-service for the room details | 404, or 503 if room-service is unreachable |
| 4 | The meeting starts in the future, in the site's timezone | 409 |
| 5 | The duration is between 15 minutes and 4 hours | 409 |
| 6 | The day is an allowed working day (Monday to Friday) | 409 |
| 7 | The meeting is within business hours (08:00 to 19:00) | 409 |
| 8 | The room is `ACTIVE` | 409 |
| 9 | The number of attendees does not exceed the room's capacity | 409 |
| 10 | All invitees exist, are active, and are not the organizer | 404 or 409 |
| 11 | The room is not already booked for an overlapping time | 409 |
| 12 | The organizer has no other meeting at the same time | 409 |
| 13 | The organizer has fewer than 4 reservations that day | 409 |

Every limit above (durations, days, hours, quota) must be **configurable** in the application settings. Do not hard-code them.

**Double bookings must be impossible.** If two people try to book the same room for the same slot at the same moment, exactly one of them succeeds. Choose and explain a locking strategy that guarantees this.

**If room-service is down**, booking must fail cleanly with `503`. It must never fail with an unexplained `500`.

### 8.2 Other reservation rules

- **Cancel**: only `CONFIRMED` reservations can be cancelled.
- **Reschedule**: the organizer can reschedule only while the reservation is `CONFIRMED` and before it starts. All the booking rules apply again.
- **Transfer**, checked in this order:
  1. The reservation must be `CONFIRMED`.
  2. Only the organizer can transfer it, and not to themselves.
  3. The new organizer must exist and be active.
  4. The new organizer must have no conflicting meeting and must not exceed their daily quota.
  5. If the new organizer was an attendee, they leave the attendee list, and the previous organizer becomes an accepted attendee.
- **Check-in**: allowed from 10 minutes before the start until 15 minutes after the start.
- **No-show**: a background job runs every minute. If a confirmed reservation is not checked in 15 minutes after its start, the job marks it `NO_SHOW` and **frees the room**.
- **Completion**: a background job marks checked-in reservations as `COMPLETED` once their end time has passed.

### 8.3 Errors

Return every error in the Problem Details format, with:
- a clear title;
- the name of the service;
- a timestamp;
- for business rule errors, a **machine-readable code** such as `ROOM_SLOT_TAKEN`, `CAPACITY_EXCEEDED`, or `CHECK_IN_WINDOW_CLOSED`.

| Situation | Status |
|---|---|
| Resource not found | 404 |
| Invalid input. List each invalid field | 400 |
| Business rule violated | 409 |
| Conflicting update, detected by optimistic locking | 409 |
| Missing permission or disabled account | 403 |
| The other service is unavailable | 503 |
| Unexpected error. Do not expose internal details | 500 |

---

## 9. Communication Between Services

### 9.1 REST call (booking-service → room-service)

- Define the call as a declarative HTTP interface (`@HttpExchange`).
- Authenticate it with booking-service's service-account token.
- Configure short timeouts and a small number of retries.
- Pass a **correlation ID** from the incoming request to the outgoing call, and include it in the logs of both services. This lets you follow one request across both services.

### 9.2 Events (booking-service → room-service)

booking-service publishes an event every time a reservation is **created, rescheduled, cancelled, released (no-show), completed, or transferred**. Each event contains a unique event ID, the event type, when it happened, the reservation ID, the room ID, and the start and end times.

Requirements:
- **Never lose an event.** Save the event in the database in the same transaction as the reservation change (the "transactional outbox" pattern). A background job then sends saved events to RabbitMQ.
- **Process each event only once.** room-service must ignore an event it has already processed.
- **Ignore old events.** If an event arrives after a newer one for the same reservation, room-service must ignore it.
- **Don't block on bad messages.** After a few failed attempts, move a message to a dead-letter queue.

### 9.3 Health

Both services expose health endpoints, with separate liveness and readiness checks. The readiness check covers the database and RabbitMQ.

---

## 10. Testing Expectations

- Write **integration tests** for each service. Run them against **real MySQL and RabbitMQ** using Testcontainers. Do not use an in-memory database.
- Call the API in tests with `WebTestClient`.
- Generate **test JWTs** that look like Keycloak's tokens, so tests don't need a running Keycloak.
- **Mock** the room-service client in booking-service tests, and mock the calls to Keycloak.
- Make time controllable in tests. Inject a clock, so that rules like check-in windows and no-shows can be tested reliably.
- Organize tests with `@Nested` and `@DisplayName`. Each test checks **both the status code and the response body**.

### Minimum scenarios to cover

**room-service**
- Site and room CRUD: success, not found, duplicates, permission denied for employees, and deleting a site that still has rooms.
- Availability:
  - free rooms are returned;
  - booked rooms are excluded;
  - back-to-back slots are allowed;
  - rooms in maintenance are excluded;
  - invalid time ranges are rejected.
- Events:
  - a "created" event makes a room busy;
  - a "cancelled" event frees it;
  - a duplicate event is processed only once;
  - an older event that arrives late is ignored.

**booking-service**
- Registration: success, duplicate username, password mismatch, weak password, and rollback when the profile save fails.
- Profile: read the profile, and a partial update that leaves other fields unchanged.
- User administration:
  - filtering works;
  - an employee cannot access admin endpoints;
  - deactivating a user cancels their reservations;
  - a deactivated user is blocked;
  - the last admin cannot be demoted.
- Reservations:
  - a successful booking;
  - each booking rule failing with the correct code;
  - room-service down;
  - cancel, reschedule, transfer, check-in, and invitation responses, including their failure cases.
- Concurrency: two simultaneous bookings of the same slot result in exactly one success.
- Background jobs: no-show release, and sending of outbox events.

The whole project must build and pass all tests with a single `mvn verify` from the root, with at least **80 % test coverage** per service.

---

## 11. Deliverables & Assessment

### Deliverables

1. The full source code in one Git repository, with the module structure from Section 3.3.
2. A `docker-compose.yml` that starts MySQL, Keycloak (with a pre-configured realm, roles, clients, and one admin user), RabbitMQ, and both services.
3. An OpenAPI 3 description for each service, in `docs/api/`.
4. A Bruno collection in `bruno/`, with one folder per service, covering every endpoint.
5. A `README.md` that explains:
   - how to run the project locally;
   - how to run the tests;
   - the architecture;
   - the main design decisions, especially how you prevent double bookings.

### Assessment criteria (equal weight)

| # | Area | What we look for |
|---|---|---|
| 1 | Microservice design | Clear service boundaries, separate databases, no coupling between services |
| 2 | Project structure | A clean multi-module Maven build with shared versions |
| 3 | Data model | Correct entities and constraints, migrations, soft deletes, locking |
| 4 | REST API | Correct methods, status codes, pagination, validation, consistent errors |
| 5 | Security | Roles read from Keycloak, access rules enforced, secure service-to-service calls |
| 6 | Business logic | All booking rules applied in order, plus check-in, transfer, reschedule, and no-show |
| 7 | Service communication | A reliable REST client, no lost or duplicated events, graceful handling of failures |
| 8 | Spring Boot 4 usage | Modern features used where they fit |
| 9 | Code quality | Readable code, no duplication, records for DTOs, no secrets in code |
| 10 | Testing | Realistic integration tests covering the scenarios above |
| 11 | Documentation | An up-to-date README, OpenAPI files, a Bruno collection, and a working Docker Compose setup |

---

*Questions are welcome. If something in this document is unclear, ask before guessing.*
