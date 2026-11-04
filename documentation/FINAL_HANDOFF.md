# PropertySetU — Current Final Development Handoff

## What this handoff covers

The main PropertySetU application is the React frontend in `frontend/` plus the Spring Boot backend in `backend/`, backed by MySQL. `PropertyDesk/` is a separate classroom project and must not be imported as the main PropertySetU backend.

This repository now includes the full Advanced Product & Engineering Specification in `documentation/research/ADVANCED_PRODUCT_AND_ENGINEERING_SPEC.md`, plus `documentation/research/IMPLEMENTATION_STATUS_AFTER_FINAL_PASS.md` that maps research requirements to current source and clearly marks incomplete and untested areas.

## Start with these documents

1. `documentation/IDE_WORKFLOW_WINDOWS.md` — exact Spring Tools/Eclipse import steps and VS Code folder/terminal setup.
2. `documentation/LOCAL_SETUP_WINDOWS.md` — database, environment, launcher and troubleshooting.
3. `documentation/API_GUIDE.md` — API route overview.
4. `documentation/TESTING_CHECKLIST.md` — local verification checklist.
5. `documentation/research/ADVANCED_PRODUCT_AND_ENGINEERING_SPEC.md` — complete target-state engineering specification.
6. `documentation/research/IMPLEMENTATION_STATUS_AFTER_FINAL_PASS.md` — what is implemented, partial, future or untested.

## Final-pass changes in the current branch

- Added explicit application-to-officer assignment and admin assignment/reassignment endpoint.
- Restricted the officer application collection to assigned cases.
- Reloaded current account role/active state from the database in the JWT request filter.
- Removed broad Vercel preview CORS patterns in favour of explicit origins.
- Added a central workflow policy for core submit/assign/upload/review eligibility and focused JUnit tests.
- Added versioned document replacements and preserved superseded document versions.
- Added reviewer name, timestamp and remarks to document review records.
- Added a correction-request and applicant resubmission loop.
- Tightened Razorpay Test Mode verification/webhook checks for signature, captured status, order, amount and currency.
- Added a safe opt-in first-admin bootstrap for a fresh local database.
- Added a Windows launcher and environment-aware PowerShell helpers.
- Added detailed Windows IDE and research-to-implementation documentation.

These items describe source changes. They are not all proven by end-to-end tests.

## Verification evidence from the final implementation pass

| Check | Result |
|---|---|
| Frontend ESLint | PASS |
| Frontend Vite production build | PASS |
| Java syntax parse of 86 source/test files | PASS — syntax only, not dependency-aware compilation |
| Git whitespace/diff check | PASS |
| Backend Maven test/package | NOT COMPLETED — this build environment could not reach Maven Central before resolving the Spring Boot parent |
| Fresh MySQL schema and upgrade | NOT TESTED |
| Razorpay sandbox checkout/webhook matrix | NOT TESTED |
| Browser applicant→officer→correction→resubmission E2E | NOT TESTED |
| Cross-officer negative authorization tests | NOT TESTED |

A passing frontend build or Java syntax parse is not a substitute for the backend Maven build, database testing or browser E2E checks. Run the Windows verification helper on the development machine with network access and local MySQL configured.

## Run locally

Preferred first launch: create MySQL database, configure `backend/.env.local`, then double-click `START_PROPERTYSETU.bat`. It opens the backend and frontend in separate PowerShell windows.

For IDE-based development:
- Import only `backend/` as an Existing Maven Project in Spring Tools/Eclipse.
- Run `PropertyRegistrationBackendApplication.java`.
- Open the full repository root in VS Code and run `npm ci`, `npm run lint`, `npm run build`, then `npm run dev` from `frontend/`.
- Keep MySQL, Spring Boot and Vite running in separate windows.
- Do not run the launcher while the same backend is already running from the IDE.

## Academic and legal boundary

PropertySetU is an academic workflow demonstration. It does not connect to an official government registrar, certify legal title, issue a registration certificate, provide legal advice or perform official property registration. The fixed ₹500 flow is a Razorpay Test Mode demonstration only. Use fictional applicant details and synthetic documents. Do not use live payment keys or real identity documents.

## Production work still required

Before any real-person or production use: reviewed database migrations, private object storage and malware scanning, retention/deletion rules, secret rotation and scanning, rate limits, notification outbox/retry, full integration and browser tests, accessibility testing, pagination/performance checks, monitoring, backups, incident response and legal/privacy review. OCR/AI, official registry integrations, property lifecycle events and jurisdiction-wide operations remain future/research-gated scope.
