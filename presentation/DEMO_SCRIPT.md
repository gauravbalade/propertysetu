# 5–7 Minute Demonstration Script

## Before the presentation

- Prefer the local app if the backend, MySQL and frontend have all been started and smoke-tested.
- If using the deployed frontend, verify that the page and API are responding immediately before the demo. A published URL is not proof that every service is currently healthy.
- Use synthetic applicant details and fake documents only.
- Use Razorpay Test Mode only. Never enter a real card, CVV, UPI PIN, banking password or real OTP.
- Do not assume an officer/demo account exists unless you have configured and tested it beforehand.

## 0:00–0:40 — Introduction

“This is PropertySetu, an academic property-application and document-review workspace. It organises applicant, owner, property, location and supporting-document information in a guided workflow. It records the application status and provides a structured review trail. It is a demonstration platform, not an official government registration portal.”

## 0:40–2:40 — Applicant journey

If the environment is confirmed working:
- Register a fresh synthetic applicant account and log in.
- Complete owner profile, property details and location.
- Create the application and point out its generated reference number.
- Upload separate synthetic files for identity proof, address proof and property document.
- Show the review/submission acknowledgement and submit.

Explain the intended lifecycle and show the actual current status. If a step fails during the live demo, state that it needs further testing instead of claiming success.

## 2:40–3:40 — Payment boundary

- Show the clearly labelled ₹500 Razorpay Test Mode demonstration fee.
- Explain that the payment flow is sandbox-only and is not an official registration fee.
- Only complete checkout if the environment is confirmed to be Razorpay Test Mode and you have the provider's documented test values.

Suggested wording: “This is a test-mode integration for academic demonstration, not a live collection of government fees. Do not use real financial credentials.”

## 3:40–5:20 — Officer review

Only if a separately configured officer/admin account is available and has been tested:
- Sign in with that account.
- Find the application by its reference number.
- Review the applicant/property/purpose and supporting documents.
- Show the available review decision and audit/status history.

Do not invent or publish credentials. If this role is not configured, show the relevant source/API design and explain that an authorised officer workflow requires a provisioned role.

## 5:20–6:10 — Architecture and engineering

Show the repository structure and explain:
- React + Vite frontend.
- Spring Boot REST API.
- MySQL persistence with JPA.
- Spring Security/JWT and role-based access.
- Protected document handling and application status/audit records.
- Automated checks: frontend ESLint/production build and backend Maven tests/package.

Be clear that CI passing is not the same as a fresh end-to-end test of every screen and live integration.

## 6:10–7:00 — Limitations and next steps

“PropertySetu is an academic MVP. Before real-world use it needs a full security/privacy review, migration-managed schema, protected object storage, malware scanning, retention controls, monitoring, backups, rate limiting, and independent end-to-end testing. It does not provide legal approval or replace the official registering office.”

## Never claim

Do not describe the project as an official government service, legal-title verification engine, production fraud detector, live payment platform, or fully end-to-end verified deployment unless those claims are independently demonstrated.
