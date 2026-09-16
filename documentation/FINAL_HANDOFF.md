# PropertySetu — Final Development Handoff

## Current milestone

The local academic MVP is complete and verified for demonstration. The product supports applicant registration, secure login, guided property-registration preparation, document submission, simulated payment, officer review, document-level decisions, application status tracking, and audit history.

## Start order

1. Start MySQL80 and confirm the `property_registration_db` database exists.
2. Start the backend from `D:\Spring-Workspace\property-registration-backend` in Spring Tools/Eclipse, using the configured Java environment.
3. Start the frontend from `D:\PropertyRegistrationSystem\frontend` with `npm run dev`.
4. Open `http://localhost:5173`.

## Demonstration flow

- Create a new applicant account.
- Log in and complete owner, property, and location details.
- Create an application.
- Upload synthetic demo files for identity proof, address proof, and property document.
- Submit the application.
- Create and complete the labelled ₹500 test-mode payment.
- Log out.
- Log in with the local officer account documented in the private/local setup notes.
- Search for the application, open protected files, mark each required document `VERIFIED`, then complete verification.
- Show the status and audit history.

## Verified commands

Frontend:

```powershell
cd D:\PropertyRegistrationSystem\frontend
npm run lint
npm run build
```

Backend:

```powershell
cd D:\Spring-Workspace\property-registration-backend
.\mvnw.cmd test -q
```

Both completed successfully during final verification. The backend connected to MySQL on port `3307` and loaded nine JPA repositories.

## Deliberate scope boundaries

- Payment is local test mode; it does not charge money.
- The system is an academic MVP, not a government service.
- No legal approval, public deployment, live gateway, GitHub publication, or public URL is claimed.
- Use synthetic files only in demonstrations.
- Android development should start after the web/API workflow has been demonstrated and accepted.

## Next production engineering phase

Before real-world use, add migration-managed schema changes, private object storage, malware scanning, retention/deletion workflows, rate limiting, managed secrets, HTTPS, monitoring, backups, privacy/legal review, accessibility testing, and a verified payment-provider integration.
