# Testing Checklist

Use synthetic accounts and Razorpay Test Mode only.

## Application startup
- [ ] MySQL database is running and property_registration_db exists.
- [ ] Backend starts with configured environment variables.
- [ ] Frontend development server starts and can reach the API.
- [ ] No real secret or uploaded personal document is tracked by Git.

## Authentication and authorization
- [ ] Applicant registration and login return expected responses.
- [ ] Wrong password and malformed inputs are rejected.
- [ ] Protected routes reject missing/invalid JWTs.
- [ ] Applicant cannot read/edit another applicant's resources or download their files.
- [ ] Inactive users lose access on the next request, even if they still hold an unexpired JWT.
- [ ] Officer A cannot list/read/download/review/audit Officer B's cases.
- [ ] Admin can assign/reassign eligible cases only to active OFFICER accounts.
- [ ] Applicant cannot call officer/admin-only operations.

## Domain behavior
- [ ] Property and location persist and reload.
- [ ] Draft applications can be edited according to current rules.
- [ ] Submitted applications reject prohibited edits.
- [ ] File upload enforces size/type/signature rules and stores files outside the public frontend.
- [ ] Admin can inspect documents but cannot change document decisions or complete an application.
- [ ] Rejecting a required document moves the application to NEEDS_CORRECTION and records the reason.
- [ ] Applicant can upload a corrected version and resubmit; the prior version remains marked SUPERSEDED.
- [ ] Resubmission does not create a second payment obligation.
- [ ] Final verification checks the latest active required document versions.
- [ ] Invalid application state transitions are rejected.
- [ ] Officer decisions persist with the authorized officer and remarks.

## Payment (Test Mode)
- [ ] Backend creates/reuses the gateway order with the server-decided amount.
- [ ] Checkout never receives private gateway secrets.
- [ ] Invalid signature is rejected.
- [ ] Wrong amount, currency, order reference or uncaptured payment is rejected.
- [ ] Only signed payment.captured webhook events for known orders are accepted.
- [ ] Duplicate webhook deliveries do not duplicate the successful payment transition.
- [ ] Only a verified payment causes the allowed application-state transition.

## Build checks
- [ ] Run .\mvnw.cmd test from backend.
- [ ] Run npm run lint and npm run build from frontend.
- [ ] Record actual output; do not describe an unrun test as passed.
