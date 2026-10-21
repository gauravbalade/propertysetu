# Architecture and Entity Mapping

This document explains how PropertySetu maps classroom Spring Boot concepts to a property application domain. It is intended to make the code structure easy to demonstrate and explain in a viva.

## Package responsibility

- `config`: Spring Security filter chain, CORS and application configuration.
- `controller`: HTTP/REST boundary; validates input and delegates to services.
- `dto`: API request and response types. Do not expose password hashes or private file paths.
- `entity`: JPA persisted model and lifecycle enums.
- `repository`: Spring Data JPA data access abstractions.
- `security`: JWT parsing/signing and authentication-related components.
- `service`: business rules, ownership checks, transactions, gateway calls and state transitions.

## Domain mapping

| Classroom concept | PropertySetu domain | Why it is different |
|---|---|---|
| User / Customer | UserAccount / Owner | Applicant identities and owner profiles are distinct concerns |
| Product | Property | A real-world property record, not sale inventory |
| Category / SubCategory | Property type / application purpose | Classification for forms and review, not a product catalog |
| Cart / Order | RegistrationApplication | Tracks a document/review workflow, not a purchase |
| Order item | Application-property relationship / Document | Application data and evidence, not purchased line items |
| Vendor | Property owner | Person associated with a property, not a seller account |
| Payment | Payment | Records a test-mode application fee using gateway order references |
| Product services | PropertyService / ApplicationService | Enforce owner access and workflow rules |
| Payment service | PaymentService | Server-created order, signature checks and status reconciliation |

## Key relationship flow

```text
UserAccount ── Owner ── Property ── Location
                         │
                         └── RegistrationApplication
                                  ├── Document
                                  ├── Payment
                                  └── Verification (Officer)
```

The actual entity annotations in source are authoritative for exact cardinality.

## Core use cases

1. Applicant creates an application draft.
2. Applicant attaches supporting evidence and submits it.
3. System records the application state and creates a gateway order through the backend.
4. Payment provider returns a result; backend verifies it rather than trusting the browser.
5. Officer reviews a paid submission and records a decision with remarks.
6. Applicant sees the updated application status.

## Security and data integrity rules

- Hash passwords with BCrypt; never log passwords or OTP values.
- Sign JWTs with a private environment-configured secret.
- Enforce role and record-ownership rules on every protected endpoint.
- Enforce allowed state transitions on the server.
- Trust payment success only after gateway signature and payment status/amount validation.
- Store uploaded documents outside the frontend's public directory and authorize downloads.
- Keep provider and database secrets out of source control.

## Viva explanation

“PropertySetu follows the controller-service-repository pattern used in a standard Spring Boot full-stack application. Controllers accept HTTP requests, DTOs define the API contract, services implement rules, repositories access the database, and JPA entities map domain data. It applies the same architectural learning goals as the classroom project but models property applications, evidence, payment and verification rather than buying and selling products.”
