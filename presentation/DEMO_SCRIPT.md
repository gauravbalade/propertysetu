# 5–7 Minute Demonstration Script

## 0:00–0:40 — Introduction

“This is PropertySetu, a property registration and document verification workspace. It connects applicants and authorised officers in one clear workflow. The goal is to capture accurate property information, protect supporting documents, record a clearly labelled test payment, and let an authorised officer complete accountable verification.”

## 0:40–2:40 — Applicant journey

- Register a fresh applicant account.
- Log in and show the applicant portal.
- Complete owner profile, property details and location.
- Create the application and point out the generated application number.
- Upload a fake identity or property document.
- Submit the application.

Explain that each stage is visible and the backend rejects invalid state transitions.

## 2:40–3:40 — Test payment

- Show the ₹500 amount.
- Create the test-mode order.
- Complete the simulated payment.
- Point out the `PAID` status and signature flag.

Say clearly: “This is a safe local test flow; it does not charge real money and does not represent a live payment-provider integration.”

## 3:40–5:20 — Officer journey

- Log out.
- Log in with the officer demo account.
- Search by application number.
- Open the application and review applicant, property, purpose and status.
- Verify the application with remarks.
- Refresh and show the `COMPLETED` state.

## 5:20–6:10 — Security and engineering

Show the repository structure and explain React, Spring Boot, MySQL, BCrypt, JWT, JPA and role-based route protection. Mention that unauthenticated protected requests are rejected and verification requires an officer or admin role.

## 6:10–7:00 — Roadmap

“Before production, PropertySetu should add migration-managed databases, private object storage, malware scanning, retention controls, rate limiting, monitoring, HTTPS, managed secrets, legal review and verified payment-provider integration. The current project is a working academic MVP with a complete demonstrable workflow.”
