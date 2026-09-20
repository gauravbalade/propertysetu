# PropertySetu — Academic Property Application Workflow

## About

PropertySetu is a student-developed academic project that demonstrates
a guided digital property application workflow.

The project brings together:

- Applicant information
- Property details
- Property location
- Supporting documents
- Application review
- Test-mode payment

### Important Notice

PropertySetu is an academic demonstration project.

It is NOT an official Government of Maharashtra,
IGR Maharashtra, or government registration portal.

No official government property registration is performed
through this application.

Payment functionality is provided through Razorpay Test Mode when server-side test credentials are configured. No live government fee or official registration payment is represented.

PropertySetu is a security-conscious academic product concept that makes a property application easier to understand, submit, pay for, and review through one traceable workflow.

> **Product promise:** one guided application journey, verified contact channels, protected documents, accountable review, secure hosted payment, and a clear status trail.

This is an academic MVP and demonstration platform—not a government service or a substitute for legal advice. Exact requirements vary by transaction and registering office.

## What the system does

Applicants can create an account, verify their email and mobile number with OTPs, enter owner and property information, edit draft records, save the property location, upload/replace/remove supporting documents, submit an application, recover a forgotten password, and complete a clearly labelled Razorpay Test Mode payment. Draft applications can be edited or deleted; once an application leaves DRAFT status, its core property/location/application data is frozen by the backend.

Officers can sign in to a protected dashboard, search applications, filter by status, review the submitted information, and verify or reject eligible paid applications.

> **Payment note:** the payment layer is implemented against Razorpay's server-created Orders API and Checkout flow. The backend verifies the returned Razorpay signature and captured amount. The deployment must use Razorpay Test Mode credentials for the academic demo; no live transaction is implied.

## Project structure

```text
PropertyRegistrationSystem/
├── backend/              # Spring Boot source and deployment container
├── frontend/             # React + Vite application
├── database/             # database notes and SQL reference
├── documentation/        # API, testing, deployment and report material
├── presentation/         # presentation outline and demo script
└── demo/uploads/         # local uploaded-document directory
```

## Technology stack

- Frontend: React 19, Vite, responsive CSS
- Backend: Spring Boot 4.1.1, Spring Web, Spring Data JPA, Spring Security
- Database: MySQL 8 on local port `3307`
- Authentication: BCrypt passwords, stateless JWT bearer tokens, email OTP + mobile OTP for applicant onboarding
- Recovery: email OTP password reset
- Verification provider: Twilio Verify v2
- Uploads: multipart document upload stored outside the database
- Payment: Razorpay Orders API + Standard Checkout + server-side signature/amount verification + webhook reconciliation
- Transactional notifications: optional SendGrid application-status email

## Run locally

### 1. Start MySQL

Create the database in MySQL Workbench:

```sql
CREATE DATABASE property_registration_db;
```

For local development, configure your own MySQL connection through environment variables rather than publishing credentials. Example placeholders:

```text
DB_URL=jdbc:mysql://localhost:3307/property_registration_db?useSSL=false&serverTimezone=Asia/Kolkata
DB_USERNAME=your-local-user
DB_PASSWORD=your-local-password
JWT_SECRET=replace-with-at-least-32-characters
UPLOAD_DIR=D:/PropertyRegistrationSystem/demo/uploads
FRONTEND_ORIGIN=http://localhost:5173
FRONTEND_ORIGINS=https://frontend-six-amber-63.vercel.app
TWILIO_ACCOUNT_SID=your-twilio-account-sid
TWILIO_AUTH_TOKEN=your-twilio-auth-token
TWILIO_VERIFY_SERVICE_SID=your-twilio-verify-service-sid
RAZORPAY_KEY_ID=rzp_test_your-key-id
RAZORPAY_KEY_SECRET=your-test-key-secret
RAZORPAY_WEBHOOK_SECRET=your-webhook-secret
SENDGRID_API_KEY=your-sendgrid-api-key
SENDGRID_FROM_EMAIL=verified-sender@example.com
SENDGRID_FROM_NAME=PropertySetu
```

### 2. Start the backend

Open Spring Tools/Eclipse and run the backend from:

```text
D:\Spring-Workspace\property-registration-backend
```

Or use the wrapper in PowerShell:

```powershell
cd D:\Spring-Workspace\property-registration-backend
.\mvnw.cmd spring-boot:run
```

The API starts at `http://localhost:8080`.

### 3. Start the frontend

```powershell
cd D:\PropertyRegistrationSystem\frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

For a deployed backend, create `frontend/.env`:

```text
VITE_API_URL=https://your-backend.example.com
```

## Demonstration accounts

| Role | Username | Password |
|---|---|---|
| Officer | Provision a private officer account in the deployment database | Do not publish credentials |

Create a new applicant account from the application itself. Configure officer credentials privately in the hosting database or administration process; never publish them in the repository.

## Demonstration flow

1. Register and log in as an applicant.
2. Create the owner profile.
3. Add a property and its location.
4. Create the registration application.
5. Upload a PDF, JPG, or PNG document.
6. Submit the application.
7. Create the Razorpay Test Mode order and complete Checkout.
8. The backend verifies the Razorpay signature and captured amount.
9. Log out and sign in with the privately provisioned officer account.
10. Search for the application, open it, and verify it.
11. Confirm the application moves from `PAID` to `UNDER_VERIFICATION` to `COMPLETED`.

## Validation already completed

- Frontend ESLint: passed
- Frontend production build: passed
- Backend Maven test suite: passed
- JWT login: passed
- Protected request without JWT: returns HTTP `403`
- Officer-only verification route: protected by role
- Payment transition: `PENDING` to `SUCCESS`, signature flag true in test flow
- Verification transition: paid application to completed application
- Completed-application protection: prevents repeated verification

See `documentation/` for the API guide, test checklist, database notes, deployment plan, report text, and limitations.

## Public demo deployment notes

The repository can be published publicly, but a public code repository is not the same as a running website. Deploy the frontend and backend separately, use a managed MySQL database, and configure the environment variables from `.env.example` in the hosting dashboards. Never commit real passwords, JWT secrets, database credentials, or identity documents.

For the first free academic demonstration, use synthetic documents only, keep payment in clearly labelled test mode, restrict CORS to the deployed frontend domain, use a strong production JWT secret, and keep the database on a provider with backups or a persistent storage plan. A public demo is not a government registration service and does not provide legal approval.

Before real-world use, add migration-based schema management with `DDL_AUTO=validate`, private object storage, malware scanning, retention/deletion workflows, rate limiting, managed secrets, HTTPS, monitoring, backups, privacy/legal review, accessibility testing, and a verified payment-provider integration with server-side signature verification.


## Account verification

Applicant registration is intentionally a two-channel verification gate:

1. The backend creates the applicant as inactive.
2. Twilio Verify sends an email OTP and an SMS OTP.
3. The applicant verifies both channels.
4. Only after both checks succeed is the applicant account activated for password login.
5. OTP verification is handled by the verification provider; OTP values are never stored in the PropertySetu database.

For local/deployed use, configure the Twilio Verify service with email and SMS channels. Phone numbers are normalized to Indian E.164 format (`+91XXXXXXXXXX`).

## Razorpay payment architecture

The payment flow is intentionally server-controlled:

1. Applicant submits the application.
2. Backend creates the Razorpay Order for exactly ₹500 (50,000 paise).
3. Frontend receives only the public Razorpay Key ID and the server-created Order ID.
4. Razorpay Standard Checkout opens in the browser.
5. Checkout returns the Razorpay order ID, payment ID and signature.
6. Backend verifies the signature using the Razorpay Key Secret.
7. Backend fetches the payment and checks that it is captured and exactly ₹500.
8. A signed Razorpay webhook can reconcile the payment asynchronously.

Never put `RAZORPAY_KEY_SECRET`, `RAZORPAY_WEBHOOK_SECRET`, Twilio Auth Token, database passwords, or JWT secrets in the frontend or Git repository.

Razorpay's official guidance requires server-side order creation, signature validation, trusted order IDs, and secure handling of API secrets. Twilio Verify's current API provides verification start/check flows for SMS and email channels. The frontend also throttles resend actions to reduce accidental repeated sends; Twilio Verify provides its own verification-attempt protections and configurable service rate limits. Optional SendGrid notifications are best-effort and never block the application or payment transaction.
