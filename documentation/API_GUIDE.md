# API Guide

Base URL: `http://localhost:8080`

Protected endpoints require:

```http
Authorization: Bearer <JWT returned by login>
```

## Authentication

### Register applicant

`POST /api/auth/register`

```json
{
  "username": "new_applicant",
  "password": "strong-password",
  "email": "new@example.com",
  "phone": "9876543211"
}
```

### Login

`POST /api/auth/login`

```json
{
  "username": "your-private-officer-username",
  "password": "your-private-officer-password"
}
```

The response includes the safe user profile and a JWT. Passwords are never returned.

## Applicant workflow

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/owners/me` | Resume the current applicant owner profile |
| POST | `/api/owners` | Create owner profile |
| GET | `/api/properties/me` | List the current applicant’s properties |
| POST | `/api/properties` | Create property |
| GET | `/api/locations/property/{propertyId}` | Resume a saved property location |
| POST | `/api/locations` | Save property location |
| GET | `/api/applications` | List only the current applicant’s applications, or all applications for officers |
| POST | `/api/applications` | Create or reuse an active application |
| POST | `/api/applications/{id}/documents` | Upload supporting document |
| GET | `/api/applications/{id}/documents` | List application documents |
| POST | `/api/applications/{id}/submit` | Submit application |
| GET | `/api/payments/application/{id}` | Resume the latest payment order |
| POST | `/api/payments/order` | Create or reuse a test payment order |
| POST | `/api/payments/verify` | Complete test payment |

## Officer workflow

| Method | Endpoint | Role | Purpose |
|---|---|---|---|
| GET | `/api/applications` | authenticated | Load dashboard applications |
| POST | `/api/verifications` | `OFFICER` or `ADMIN` | Verify or reject an eligible application |

Verification request:

```json
{
  "applicationId": 3,
  "verifiedByUserId": 1,
  "status": "VERIFIED",
  "remarks": "Documents checked successfully."
}
```

Valid verification statuses are `VERIFIED` and `REJECTED`.

## Application states

```text
DRAFT → SUBMITTED → PAYMENT_PENDING → PAID → UNDER_VERIFICATION → COMPLETED
                                                               └→ REJECTED
```

The applicant status centre displays the current state and allows a refresh without exposing another applicant's records. The backend rejects invalid state transitions. Completed applications cannot be verified again.

## Security summary

- Register and login are public.
- Application, property, owner, location, document and payment routes require JWT authentication.
- Verification routes require `OFFICER` or `ADMIN`.
- Passwords use BCrypt hashing.
- JWT secret, database password, upload directory and CORS origin support environment configuration.
