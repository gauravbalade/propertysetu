# PropertySetu Release Checklist

## Local demonstration

- [x] MySQL service is running on the configured port.
- [x] Database exists and the backend connects successfully.
- [x] Backend tests pass with the repository Maven wrapper.
- [x] Frontend lint and build pass.
- [x] Applicant can register and log in.
- [x] Applicant can create or resume owner, property, location, and application.
- [x] Three required demo document categories can be uploaded.
- [x] Submission blocks when required categories are missing.
- [x] Test-mode payment is visibly labelled and completes.
- [x] Existing payment orders resume safely after login.
- [x] Officer can review documents individually.
- [x] Officer can inspect accountability history.
- [x] Applicant can refresh and see the application status.
- [x] No real identity documents or credentials appear in screenshots or video.

## Before public deployment

- [ ] Replace development defaults with managed secrets.
- [ ] Use migration-managed schema changes instead of Hibernate update mode.
- [ ] Configure HTTPS and a strict production CORS origin.
- [ ] Move documents to private object storage with malware scanning.
- [ ] Add retention, deletion, backup, and incident-recovery policies.
- [ ] Add login rate limiting and account lockout/monitoring.
- [ ] Add production logging, metrics, alerts, and log redaction.
- [ ] Verify payment-provider sandbox signatures server-side.
- [ ] Complete legal, privacy, accessibility, and threat-model review.
- [ ] Run dependency, secret, and container scans.
- [ ] Perform authenticated end-to-end testing in a clean environment.

## Truthful project claims

Do not describe the academic MVP as a government service, legal approval engine, live payment platform, public deployment, or fraud-detection system unless those capabilities are actually implemented and independently verified.
