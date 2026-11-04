# API Guide

Base URL: http://localhost:8080

Protected APIs require Authorization: Bearer <JWT>. Exact request fields are defined by DTOs and controller validation in source.

## Auth and account

| Method | Endpoint | Purpose |
|---|---|---|
| POST | /api/auth/register | Register an applicant |
| POST | /api/auth/login | Authenticate and receive a JWT |
| POST | /api/auth/forgot-password | Start password recovery |
| POST | /api/auth/reset-password | Complete password recovery |
| GET | /api/health | Check backend health |

Provider-backed OTP endpoints depend on deployment configuration; never use real OTPs or real personal data in screenshots.

## Applicant workflow

| Method | Endpoint | Purpose |
|---|---|---|
| GET/POST | /api/owners | Read/create owner profile |
| GET/POST | /api/properties | List/create properties |
| GET/POST | /api/locations | Read/create property location |
| GET/POST | /api/applications | List/create applications |
| POST | /api/applications/{id}/submit | Submit a draft or correction-requested application |
| POST | /api/applications/{id}/documents | Upload an application document |
| GET | /api/applications/{id}/documents | List application document versions |
| GET | /api/payments/application/{id} | Read latest payment information |
| POST | /api/payments/order | Create/reuse a Test Mode order |
| POST | /api/payments/verify | Verify the gateway result server-side |
| POST | /api/payments/reconcile | Reconcile a pending Test Mode payment with Razorpay |

## Officer and administrator workflow

| Method | Endpoint | Access |
|---|---|---|
| GET | /api/applications | Applicant sees own; Officer sees assigned; Admin sees all |
| POST | /api/applications/{id}/documents/{documentId}/review | Assigned Officer only |
| POST | /api/verifications | Assigned Officer only |
| GET | /api/audit/applications/{id} | Applicant owner, assigned Officer, or Admin |
| POST | /api/admin/applications/{id}/assignment | Admin only |
| GET/POST/PUT | /api/admin/users | Admin only |

The assignment request body contains officerId and a non-blank reason. Only active accounts with the OFFICER role can be assigned. Assignment and reassignment are recorded in application audit history.

## Typical lifecycle

```text
DRAFT → SUBMITTED → PAYMENT_PENDING → PAID → UNDER_VERIFICATION
                                               ├→ COMPLETED
                                               ├→ REJECTED
                                               └→ NEEDS_CORRECTION → RESUBMITTED
                                                                      ↓
                                                               UNDER_VERIFICATION
```

Correction resubmission does not create a second payment obligation. The latest active version of each required document must be verified before the assigned Officer can complete verification.

Use actual controller annotations and SecurityConfig as the source of truth for every endpoint and role. This summary is a navigation guide.

## Payment safeguards

- Only the backend creates trusted gateway orders.
- Use Razorpay Test Mode credentials for this academic project.
- Verify checkout signatures server-side using the private secret.
- Confirm captured status, order reference, INR currency and amount before updating application state.
- Accept only signed payment.captured webhooks for the known order and expected amount.
- Do not commit or expose gateway secrets in frontend code.
