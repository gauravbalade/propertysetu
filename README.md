# PropertySetu — Academic Property Application Workflow

## Overview

PropertySetu is a student-developed full-stack property application workflow. It demonstrates the layered Spring Boot architecture taught through the Shopify classroom project while applying it to a distinct, non-e-commerce domain.

- Applicant and role management
- Owner, property, and location records
- Document uploads and application review
- Application lifecycle and officer decisions
- Razorpay Test Mode payment workflow
- JWT authentication and role-based authorization
- React + Vite user interface and MySQL-compatible persistence

> **Academic project notice:** This is a learning/demo platform, not an official government property-registration portal. It does not perform official registration or provide legal approval. Keep payments in Razorpay Test Mode and use synthetic documents.

## Architecture

```text
PropertySetu/
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/gaurav/property/
│       ├── config/       # Spring Security, CORS and app configuration
│       ├── controller/   # REST API endpoints
│       ├── dto/          # Request/response transfer objects
│       ├── entity/       # JPA entities and enums
│       ├── repository/   # Spring Data JPA interfaces
│       ├── security/     # JWT services and authentication helpers
│       └── service/      # Business rules, transactions and gateway calls
├── frontend/             # React + Vite application
├── database/             # schema notes and local setup
├── documentation/        # API, testing, deployment and design docs
├── presentation/         # demo script and project report
└── demo/uploads/          # local demo upload directory
```

## Classroom concepts mapped to PropertySetu

| Classroom Spring concept | PropertySetu equivalent |
|---|---|
| ShopifyApplication | PropertyRegistrationBackendApplication |
| User / Customer | UserAccount / Owner |
| Product | Property |
| Category / SubCategory | Property type / application purpose |
| Cart / Order workflow | RegistrationApplication workflow |
| OrderItem | ApplicationDocument / application-to-property relationship |
| Vendor | PropertyOwner |
| Product/Category repositories | Property, Owner, Location and Application repositories |
| Services | PropertyService, ApplicationService, PaymentService, DocumentService |
| Controllers | REST controllers for property, application, document, auth, verification and payment |
| DTOs | Validated API request and response types |
| Security configuration + JWT | Stateless token authentication and role-based access |
| PaymentController / PaymentService | Razorpay Test Mode order creation and server-side verification |
| Specifications/search | Application and property filtering/search where supported |
| Response wrapper | Consistent REST error/success responses where implemented |

This is a conceptual mapping, not a claim that every classroom class has a one-to-one equivalent. PropertySetu models property records and review workflows rather than products, shopping carts, inventory or e-commerce orders.

## Domain model and relationships

Core domain records include:

1. **UserAccount** — authenticated identity and role
2. **Owner** — owner profile belonging to an applicant
3. **Property** — property information maintained by an owner
4. **Location** — address and jurisdiction details for a property
5. **RegistrationApplication** — application lifecycle, owner, property and purpose
6. **Document** — metadata and safe storage reference for supporting files
7. **Verification** — officer decision, status, timestamp and remarks
8. **Payment** — gateway order/payment references and verification state

Simplified relationship view:

```text
UserAccount (Applicant)
   └── Owner
        └── Property ─── Location
             └── RegistrationApplication
                    ├── Documents
                    ├── Payment
                    └── Verification (Officer)
```

Actual cardinality follows the JPA annotations in the source.

## Main user journeys

### Applicant
1. Register and authenticate.
2. Create or update owner details.
3. Add a property and location.
4. Create a draft application and upload supporting documents.
5. Submit the application.
6. Create a Razorpay Test Mode order and complete hosted Checkout.
7. View payment and application status.

### Officer / administrator
1. Sign in with a privately provisioned account.
2. Search/filter applications and inspect submitted details.
3. Review supporting records.
4. Verify or reject eligible applications with remarks.
5. Review the resulting application status/audit trail.

## Application lifecycle

```text
DRAFT → SUBMITTED → PAYMENT_PENDING → PAID → UNDER_VERIFICATION
                                                ├── COMPLETED
                                                └── REJECTED
```

The backend should enforce legal state transitions on the server; the frontend is not trusted to determine payment success or officer authorization.

## Payment architecture

The academic demo uses Razorpay Test Mode when server-side credentials are configured.

1. The backend creates/reuses an order using an amount derived from server-side application rules.
2. The frontend receives the public Key ID and order ID.
3. Checkout collects payment details on the gateway-hosted interface.
4. The backend validates the returned signature and confirms payment/order amount and status with the provider.
5. Webhook reconciliation can update payment state after validating the webhook signature.

Never commit gateway secrets, database credentials, JWT signing keys, SMS-provider tokens, or uploaded personal documents. Never represent a browser-only success message as proof of payment. Keep the displayed fee clearly identified as an academic test fee.

## Technology stack

- Java 17
- Spring Boot 4.x
- Spring Web MVC, Spring Data JPA, Spring Security and Bean Validation
- MySQL locally; compatible MySQL service may be used for the hosted demo
- JWT bearer authentication and BCrypt password hashing
- Razorpay Java SDK (Test Mode)
- React 19, Vite and responsive CSS
- Maven wrapper for reproducible backend commands

## Local setup

For step-by-step Windows instructions for MySQL, Spring Boot, Eclipse / Spring Tool Suite, VS Code and React, see [Run PropertySetu on Windows](documentation/LOCAL_SETUP_WINDOWS.md).


### 1. Create the database

In MySQL Workbench:

```sql
CREATE DATABASE property_registration_db;
```

Configure backend environment variables for your local database connection and a strong JWT signing secret. The repository's `.env.example` files are templates only; do not store real values in Git.

### 2. Run the backend (PowerShell)

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

The default API port is `8080`.

### 3. Run the frontend

```powershell
cd frontend
npm install
npm run dev
```

Open the Vite URL printed by the command (normally `http://localhost:5173`). Set `VITE_API_URL` to the backend URL if needed.

## Verification checklist

- [ ] MySQL service is running and the database exists.
- [ ] Backend starts without dependency or datasource errors.
- [ ] Frontend development server starts and can reach the API.
- [ ] Registration and login work with test accounts.
- [ ] Protected endpoints reject missing/invalid bearer tokens.
- [ ] Applicants cannot inspect or modify another applicant's records.
- [ ] Officer-only decisions are rejected for applicant roles.
- [ ] Document upload validates type/size and stores files outside the public frontend.
- [ ] Invalid application-state transitions are rejected.
- [ ] Payment amount and signatures are verified server-side in Test Mode.
- [ ] Secrets and private uploads are absent from Git history.
- [ ] Frontend production build and backend test suite complete successfully.

See `documentation/API_GUIDE.md`, `documentation/DATABASE.md`, `documentation/LOCAL_SETUP_WINDOWS.md` and `documentation/TESTING_CHECKLIST.md` for setup, API and verification details.

## Future improvements

- Use Flyway/Liquibase migrations and validate the deployment schema.
- Add fine-grained authorization tests and concurrency protections for review.
- Add malware scanning, private object storage, retention controls and authenticated downloads.
- Add throttling, structured audit logging, monitoring, backups and secret management.
- Add integration tests for payment webhooks and failure/retry states.

## License

See [LICENSE](LICENSE).


## New classroom-style Java project

A separate Spring Boot + MySQL starter project following the layered conventions demonstrated in the Shopify classroom code is available in [`PropertyDesk/`](PropertyDesk/README.md). It manages property owners, property records, locations, and visit requests; it is separate from the existing full-stack PropertySetu application.
