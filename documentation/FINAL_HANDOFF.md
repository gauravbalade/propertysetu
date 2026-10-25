# PropertySetu — Final Development Handoff

## Current milestone

The academic MVP source is maintained in this repository. On the `propertysetu/windows-verification` working branch, the latest GitHub Actions checks passed: frontend dependency installation, ESLint, and production build; backend Maven tests and packaging with the workflow MySQL service. These checks confirm build/test health, not a fresh end-to-end browser test of every user journey. The product supports applicant registration, secure login, guided property-registration preparation, document submission, simulated payment, officer review, document-level decisions, application status tracking, and audit history.

## Start order

1. Extract the repository ZIP and open the extracted `propertysetu` folder.
2. Start MySQL and confirm the `property_registration_db` database exists.
3. Start the backend from the repository's `backend` folder in Spring Tools/Eclipse, using the configured Java environment.
4. Start the frontend from the repository's `frontend` folder with `npm ci` and `npm run dev`.
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

The latest branch CI runs confirmed frontend lint/build and backend Maven test/package success using the workflow's MySQL service. A fresh local run on another Windows machine is not implied by CI.

## Deliberate scope boundaries

- Payment is local test mode; it does not charge money.
- The system is an academic MVP, not a government service.
- No legal approval, public deployment, live gateway, GitHub publication, or public URL is claimed.
- Use synthetic files only in demonstrations.
- Android development should start after the web/API workflow has been demonstrated and accepted.

## Next production engineering phase

Before real-world use, add migration-managed schema changes, private object storage, malware scanning, retention/deletion workflows, rate limiting, managed secrets, HTTPS, monitoring, backups, privacy/legal review, accessibility testing, and a verified payment-provider integration.
