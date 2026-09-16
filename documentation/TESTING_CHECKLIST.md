# Testing Checklist and Result

## Automated checks

| Check | Result |
|---|---|
| `npm run lint` | Passed |
| `npm run build` | Passed |
| `./mvnw test` / `mvnw.cmd test` | Passed: 1 test, 0 failures, 0 errors |
| Spring context starts | Passed |
| MySQL connection on port 3307 | Passed |

## End-to-end checks

- [x] Applicant registration
- [x] Applicant login returns JWT
- [x] Owner profile creation
- [x] Property creation
- [x] Location creation
- [x] Application creation
- [x] Document upload
- [x] Application submission
- [x] Test payment order creation
- [x] Test payment success and signature flag
- [x] Officer login
- [x] Officer dashboard loading
- [x] Application search
- [x] Status filtering
- [x] Officer verification
- [x] Rejection path
- [x] Completed application cannot be verified again
- [x] Unauthenticated protected API returns `403`
- [x] Applicant cannot access officer verification route
- [x] Password is absent from JSON user responses
- [x] Logout clears the browser JWT

## Final manual smoke test

1. Start MySQL, backend and frontend.
2. Create a fresh applicant account with a unique username and email.
3. Complete one complete applicant flow using a small fake PDF or image.
4. Copy the generated application number.
5. Log out and sign in as the demo officer.
6. Search and verify the application.
7. Capture screenshots only after replacing personal data with demo values.

## Known limitations

- Payment is test mode, not live Razorpay.
- Current development schema uses Hibernate update rather than migrations.
- Local uploads are not yet backed by cloud object storage.
- Public deployment still requires platform configuration, secrets and a managed database.
