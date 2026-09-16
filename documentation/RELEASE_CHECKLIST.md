# PropertySetu Release Checklist

## Local demonstration

- [ ] MySQL service is running on the configured port.
- [ ] Database exists and the backend connects successfully.
- [ ] Backend tests pass with `D:\Spring-Workspace\property-registration-backend\.\mvnw.cmd test -q`.
- [ ] Frontend lint and build pass.
- [ ] Applicant can register and log in.
- [ ] Applicant can create owner, property, location, and application.
- [ ] Three required demo document categories can be uploaded.
- [ ] Submission blocks when required categories are missing.
- [ ] Test-mode payment is visibly labelled and completes.
- [ ] Officer can review documents individually.
- [ ] Officer can inspect accountability history.
- [ ] Applicant can refresh and see the application status.
- [ ] No real identity documents or credentials appear in screenshots or video.

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
