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

Payment functionality is provided only in test/demo mode
for academic demonstration and does not represent a real
government payment service.

PropertySetu is a secure-by-design academic product concept that makes property registration easier to understand, submit, pay for, and verify through one transparent workflow.

> **Product promise:** one guided application journey, protected documents, accountable review, and a clear status trail.

This is an academic MVP and demonstration platform—not a government service or a substitute for legal advice. Exact requirements vary by transaction and registering office.

## What the system does

Applicants can create an account, enter owner and property information, save the property location, upload supporting documents, submit an application, and complete a clearly labelled test-mode registration payment.

Officers can sign in to a protected dashboard, search applications, filter by status, review the submitted information, and verify or reject eligible paid applications.

> **Important:** payment is currently simulated test mode. No real money is collected and no live Razorpay transaction is claimed.

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
- Authentication: BCrypt passwords and stateless JWT bearer tokens
- Uploads: multipart document upload stored outside the database
- Payment: test-mode order and payment state transition

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
7. Create and complete the ₹500 test-mode payment.
8. Log out and sign in with the privately provisioned officer account.
9. Search for the application, open it, and verify it.
10. Confirm the application moves from `PAID` to `UNDER_VERIFICATION` to `COMPLETED`.

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
