# Testing Checklist

Use synthetic accounts and Razorpay Test Mode only.

## Application startup
- [ ] MySQL-compatible database is running.
- [ ] Backend starts with configured environment variables.
- [ ] Frontend development server starts and can reach the API.
- [ ] No real secret or uploaded personal document is tracked by Git.

## Authentication and authorization
- [ ] Applicant registration and login return expected responses.
- [ ] Wrong password and malformed inputs are rejected.
- [ ] Protected routes reject missing/invalid JWTs.
- [ ] Applicant cannot read or edit another applicant's resources.
- [ ] Applicant cannot call officer/admin-only operations.

## Domain behavior
- [ ] Property and location persist and reload.
- [ ] Draft applications can be edited according to current rules.
- [ ] Submitted applications reject prohibited edits.
- [ ] File upload enforces size/type rules and reports errors clearly.
- [ ] Invalid application state transitions are rejected.
- [ ] Officer decisions persist with authorized user and remarks.

## Payment (Test Mode)
- [ ] Backend creates the gateway order with a server-decided amount.
- [ ] Checkout never receives private gateway secrets.
- [ ] Invalid signature is rejected.
- [ ] Wrong amount/order reference or an uncaptured payment is rejected.
- [ ] Duplicate callback/webhook processing is idempotent.
- [ ] Only a verified payment causes the allowed application-state transition.

## Build checks
- [ ] Run `.\mvnw.cmd test` from `backend`.
- [ ] Run `npm run build` from `frontend`.
- [ ] Record actual test output in the report; do not describe an unrun test as passed.
