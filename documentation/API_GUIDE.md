# API Guide

Base URL: `http://localhost:8080`

Protected APIs require `Authorization: Bearer <JWT>`. Exact request fields are defined by DTOs and controller validation in source.

## Auth and account

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/auth/register` | Register an applicant |
| POST | `/api/auth/login` | Authenticate and receive a JWT |
| POST | `/api/auth/forgot-password` | Start password recovery |
| POST | `/api/auth/reset-password` | Complete password recovery |
| GET | `/api/health` | Check backend health |

Provider-backed OTP endpoints depend on deployment configuration; never use real OTPs or real personal data in screenshots.

## Applicant workflow

| Method | Endpoint | Purpose |
|---|---|---|
| GET/POST | `/api/owners` | Read/create owner profile |
| GET/POST | `/api/properties` | List/create properties |
| GET/POST | `/api/locations` | Read/create property location |
| GET/POST | `/api/applications` | List/create applications |
| POST | `/api/applications/{id}/submit` | Submit a draft application |
| POST | `/api/applications/{id}/documents` | Upload an application document |
| GET | `/api/applications/{id}/documents` | List application document metadata |
| GET | `/api/payments/application/{id}` | Read latest payment information |
| POST | `/api/payments/order` | Create/reuse a Test Mode order |
| POST | `/api/payments/verify` | Verify the gateway result server-side |

## Officer workflow

| Method | Endpoint | Access |
|---|---|---|
| GET | `/api/applications` | Applicant's own applications; officer view depends on role |
| POST | `/api/verifications` | Officer or administrator |
| GET | `/api/audit/**` | Officer/administrator |
| GET/POST/PATCH | `/api/admin/**` | Administrator |

Use actual controller annotations and SecurityConfig as the source of truth for every endpoint and role. This summary is a navigation guide.

## Typical lifecycle

```text
DRAFT → SUBMITTED → PAYMENT_PENDING → PAID → UNDER_VERIFICATION
                                                ├→ COMPLETED
                                                └→ REJECTED
```

The API must reject invalid state changes and unauthorized access on the server. A UI state or client response is not evidence that a payment was captured.

## Payment safeguards

- Only the backend creates trusted gateway orders.
- Use Razorpay Test Mode credentials for this academic project.
- Verify checkout signatures server-side using the private secret.
- Confirm captured status, order reference and amount before updating application state.
- Validate signed webhooks if webhook reconciliation is enabled.
- Do not commit or expose gateway secrets in frontend code.
