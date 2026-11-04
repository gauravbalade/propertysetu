# PropertySetU — Research-to-Implementation Status

This document connects the Advanced Product & Engineering Specification (1 November 2026) to the current `propertysetu/windows-verification` source branch.

## Research preserved in this repository

- Full research baseline and target-state specification: [research/ADVANCED_PRODUCT_AND_ENGINEERING_SPEC.md](research/ADVANCED_PRODUCT_AND_ENGINEERING_SPEC.md)
- This status ledger: what was addressed in the final implementation pass and what is still a release gate.
- Main stack retained: React 19 + Vite, Spring Boot + Spring Security/JWT, Spring Data JPA, MySQL, Razorpay Test Mode only.
- Architecture decision retained: modular monolith; no premature microservices or speculative blockchain.
- Domain boundary retained: PropertySetu records an academic workflow and evidence trail. It does not establish legal ownership or perform official government registration.
- `PropertyDesk/` remains a separate classroom project; it is not merged into the PropertySetu backend.

The complete 17-page engineering PDF was used as a source for the risk/gap review. This repository preserves its detailed written specification and the implementation status below. The original PDF remains a separate source artifact in the conversation/library.

## Status vocabulary

- **Implemented in source:** relevant source code exists in this branch.
- **Partial / not fully verified:** code exists, but an end-to-end, negative-security, database or provider test has not proven it.
- **Target / not implemented:** the research describes the desired capability, but it is not part of this release.
- **Not tested here:** requires a working local Java/Maven environment, MySQL, browser or external provider.

## P0 — trust and safety

| Research ID | Area | Current branch status | Evidence / remaining gate |
|---|---|---|---|
| G-01 / AUTH-001 | Officer assignment and scope | **Partial / implemented in source** | Application entity has optional assigned officer/time; admin assignment endpoint accepts active OFFICER accounts; officer collection is restricted to assigned cases; document review and application audit require assigned scope. Run two-officer negative tests across list, detail, document download, payment and audit routes on MySQL. Jurisdiction scope is not implemented. |
| G-02 | CORS origins | **Implemented in source; deployment check remains** | SecurityConfig now uses the configured exact frontend origin and explicit FRONTEND_ORIGINS entries; wildcard Vercel preview patterns were removed. Test preflight requests against the actual hosted origin before deploying. |
| G-03 / AUTH-002 | Disabled accounts / changed roles | **Implemented in source; integration test remains** | JWT filter reloads the current account and role from the database and rejects inactive/missing users on each authenticated request. Test with a previously issued token after deactivation and role change. |
| G-04 / APP-001 | Central workflow policy | **Partial** | ApplicationWorkflowPolicy centralises key submission, assignment, upload and review predicates; focused JUnit tests were added. It is not a complete exhaustive event-sourced state machine, concurrency/optimistic locking and all transition reasons are not yet proven. Maven tests could not run in this workspace. |
| G-05 | Contact verification / OTP | **Existing feature; production readiness not verified** | Original MSG91/demo OTP code remains. Keep demo fallback explicitly labelled and test expiry, replay, attempt throttling and provider configuration. Registration must not be represented as government-grade identity verification. |
| G-06 / DOC-001 | Private evidence lifecycle | **Partial** | Upload enforces a 5 MB limit, extension and PDF/JPEG/PNG magic-byte checks, generated storage names, private server-side download authorization and application-scoped document versions. Malware scanning/quarantine, checksum, retention/deletion policy and production object storage are not included. |
| G-07 / PAY-001 | Payment truth and idempotency | **Partial / provider untested** | Server checks signed checkout, captured status, order ID, INR and ₹50000 minor units; webhook accepts a signed payment.captured event for a known order and returns on duplicate success. Run Razorpay Test Mode duplicate/delayed/failure/mismatch/reconcile tests with your own sandbox keys. |
| G-08 / SEC-001 | Secret hygiene | **Partial / must complete locally** | Local files are ignored and example files contain placeholders. Review Git history and rotate any keys ever exposed in a public commit, chat, archive or screenshot. A clean working tree is not proof of a clean Git history. |

## P1 — complete end-to-end workflow

| Research ID | Area | Current branch status | Evidence / remaining gate |
|---|---|---|---|
| G-09 | Split large React App.jsx | **Not implemented** | App.jsx remains a large single file. Refactor only after critical smoke/E2E tests exist so working screens are not lost. |
| G-10 | Correction/resubmission | **Partially implemented in source** | Officer can reject a document with remarks/reviewer/time; case moves to NEEDS_CORRECTION; applicant can replace with a new version and resubmit as RESUBMITTED; previous version is retained as SUPERSEDED. Test complete applicant→officer round trip in browser/DB. |
| G-11 | Notification outbox/retry | **Not implemented** | Current NotificationService is best-effort; no transactional outbox, delivery ledger or in-app notification inbox is proven. |
| G-12 | Assignment/SLA/handover | **Partial** | Admin assignment/reassignment and an audit reason are present. Due dates, overdue queues, escalation, supervisor role and concurrency handling are not implemented. |
| G-13 | Property lifecycle history | **Not implemented** | No dedicated append-only ownership/occupancy/renovation/subdivision event model. |
| G-14 | Pagination and scale | **Not implemented / unverified** | Application collections are still unbounded and N+1/query performance has not been measured. |
| G-15 | Threat model, retention, incident response | **Research/documentation only** | Define data classification, retention, deletion/legal hold, audit access and incident response before any real personal data. |
| G-16 | Integration and end-to-end test suite | **Partial** | Workflow policy unit tests were added; comprehensive security, DB migration, payment-provider, browser E2E and accessibility suites remain. |
| G-17 | OCR/AI, registry integrations, mobile | **Not implemented / deliberately gated** | Do not claim these features. Require official API/contract validation, user evidence, privacy review, confidence/provenance and human oversight before any future implementation. |

## Release verification observed during this pass

| Check | Result |
|---|---|
| Frontend `npm run lint` | PASS |
| Frontend `npm run build` | PASS |
| Java syntax parse of 86 source/test files | PASS (syntax only; not a dependency-aware compile) |
| `git diff --check` | PASS |
| Backend Maven test/package | NOT RUN to completion — Maven Central was unreachable from the build environment before dependency resolution |
| Live MySQL schema upgrade / migration | NOT TESTED |
| Razorpay Test Mode and webhook matrix | NOT TESTED |
| Browser E2E and two-officer negative authorization tests | NOT TESTED |

Do not convert a "NOT TESTED" item into a pass in the project report or viva.

## Explicitly outside this academic release

- Official land-registry integration or legal title verification.
- Government-approved registration, stamp-duty calculation or legal advice.
- Production use of identity documents, Aadhaar or sensitive personal records.
- Live Razorpay payments or live financial credentials.
- OCR/AI-based document authenticity decisions.
- Blockchain, microservices, public registry publication of personal records.
- SLA/escalation operations and multi-jurisdiction production policy.

These are research roadmap items, not hidden features in the current source.
