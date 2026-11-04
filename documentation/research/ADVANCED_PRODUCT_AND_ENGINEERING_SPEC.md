# PropertySetu — Advanced Product & Engineering Specification

**Document type:** implementation baseline and target-state specification  
**Version:** 1.0 — 1 November 2026  
**Status:** target specification; not a claim that all listed capabilities are implemented  
**Primary stack retained:** React 19 + Vite; Java 21 target (existing Maven file currently targets Java 17); Spring Boot; Spring Security/JWT; Spring Data JPA; MySQL; Razorpay only in test mode until production readiness is proven.

> **Truth boundary.** This document separates (A) what the inspected source package demonstrably contains, (B) gaps or risks inferred from source inspection, and (C) target requirements. A feature described in the target requirements is not implemented merely because it appears here. PropertySetu is not a government registry, legal title certificate, registrar, or legal adviser.

## 1. Executive decision

PropertySetu should evolve from a form-and-review academic MVP into a **trustworthy property workflow platform**. The product's defensible core is not “more screens” or speculative blockchain. It is the combination of:

1. **Evidence provenance:** every submitted file and consequential fact has an origin, upload time, version, checksum, reviewer, and explicit verification state.
2. **Workflow correctness:** an application can only move through allowed states; every transition has an actor, timestamp, reason, and notification outcome.
3. **Scoped authority:** applicants see their own records; officers see only assigned work and permitted jurisdiction; administrators manage accounts/configuration without silently altering case evidence.
4. **Honest status:** “uploaded”, “submitted”, “payment confirmed”, “document reviewed”, “application approved”, and “officially registered” are different claims and must never be conflated.
5. **Recoverability:** retries, duplicate callbacks, browser closure, network failure, notification failure, and staff handover must not silently lose work or fabricate success.
6. **Lifecycle continuity:** property identity and historical events are preserved; corrections append history rather than rewriting past decisions.

**Architecture decision:** keep a modular monolith first. Do not split into microservices before the domain boundaries, security model, and operational workload are measured. Use MySQL as the transactional source of truth. Add object storage, queueing, search, or a graph database only when an explicit use case and measurable need justify them.

## 2. Current source baseline — what is actually present

The inspected ZIP contains a React/Vite frontend (`frontend/src/App.jsx` is a very large single-file application), a Spring backend under `backend/src/main/java/com/gaurav/property`, a separate `PropertyDesk` Maven project, SQL helpers, deployment notes, and presentation/report material. Backend domains currently include accounts, owners, properties, locations, registration applications, documents, payments, verification, audit, notifications, and public property browsing. The API includes auth, application, document, payment, property, owner, location, verification, audit, admin-user, health, and public-property controllers.

The existing source already has useful foundations: JWT-based authentication, password hashing, DTOs for many API responses, owner relationship checks, a required-document submission check, document type/size/signature validation in the upload service, a payment order/verification/webhook path, a public listing intended to expose only completed and verified records, audit events, and explicit test-payment disclosure in the UI/documentation.

### Baseline constraints to preserve

- Keep the existing visual identity and working screens unless usability tests justify a redesign.
- Keep the current frontend/backend separation and REST API approach.
- Keep a local developer path that works on Windows with VS Code or Spring Tools.
- Keep public property views separate from private application/document data.
- Do not merge `PropertyDesk` blindly into PropertySetu; establish whether it is a separate experiment, sample, or intended bounded context.
- Do not remove a feature merely because it is not part of the first release. Mark it as planned, gated, or out of scope with a reason.

## 3. Critical gap register from source inspection

Severity definitions: **P0** blocks a trustworthy demo or creates a high-impact security/data-integrity risk; **P1** required for a credible pilot; **P2** expansion after the core workflow is reliable.

| ID | Priority | Observed gap / risk | Required correction | Evidence of completion |
|---|---|---|---|---|
| G-01 | P0 | Officer listing path in `ApplicationService.getAllApplications()` returns `findAll()` when the caller is an officer; no assignment/jurisdiction filter is evident in the inspected application entity/service. | Add explicit case assignment and jurisdiction scope; default deny if no assignment. Admin access must be separately audited. | Integration test proves Officer A cannot list/read Officer B's cases by collection, ID, document, payment, or audit endpoint. |
| G-02 | P0 | CORS config includes broad Vercel preview patterns and both origin APIs. | Use exact allowlisted production origins per environment; local origins only in dev. Remove preview wildcard from production. | Automated config test and deployed preflight test. |
| G-03 | P0 | JWT filter takes role from token claims; source path must be verified for disabled-account and role-change revocation behavior. | Load current account state or use short-lived tokens plus revocation/version checks; deny disabled users and changed roles. | Disabled user and changed-role tests invalidate prior access. |
| G-04 | P0 | Current application states include DRAFT/SUBMITTED/PAYMENT_PENDING/PAID/UNDER_VERIFICATION/VERIFIED/REJECTED/COMPLETED, but the service has no single centralized transition policy visible in the reviewed path. | Create guarded state machine with allowed source→target transitions, role, preconditions, reason and audit record. | Unit matrix covers every legal and illegal transition. |
| G-05 | P0 | Contact verification and demo OTP flows exist; do not assume registration is equivalent to email/phone verification. | Persist verified flags and verification timestamp/provider; never set verified until a server-side challenge succeeds. Keep demo OTP explicitly isolated. | Registration without challenge remains unverified; expired/replayed/wrong OTP tests fail safely. |
| G-06 | P0 | Upload/download workflow exists; production private storage, malware scanning, retention/deletion, quarantine, and object versioning are not established by the source package. | Private storage adapter, server-side authorization for every read, MIME+magic-byte checks, size limits, quarantine/scanning integration, retention policy and access log. | Direct URL guessing fails; malicious test file is quarantined; deletion policy is tested. |
| G-07 | P0 | Razorpay code has order/verify/webhook/reconcile paths, but no live end-to-end sandbox run is proven by the inspected artifacts. | Make provider state authoritative; verify webhook signature on raw body; idempotently process events; reconcile pending orders; never trust client “success”. | Sandbox test matrix for duplicates, delayed webhooks, failed checkout, mismatched amount/currency and retry. |
| G-08 | P0 | Previously uploaded configuration reportedly contained sensitive values. | Rotate exposed provider credentials; use environment/secret manager; invalidate leaked JWT signing secret; scan Git history and artifacts. Do not put secrets in README, ZIP, logs, or screenshots. | Secret scanning clean; old keys revoked; deployment uses secret configuration. |
| G-09 | P1 | Single giant `App.jsx` makes changes difficult to isolate and test. | Split by feature: auth, applicant workspace, applications, documents, payments, officer queue, registry, shared API/hooks/components. | Existing flows still work and component tests cover critical interactions. |
| G-10 | P1 | Application review workflow needs explicit correction request/resubmission with preserved versions and reasons. | Add per-document review outcomes, correction items, applicant response, resubmission version and history. | Correction round-trip test preserves previous versions and shows next steps. |
| G-11 | P1 | Notifications are represented by service calls, but delivery/retry/outbox guarantees are not proven. | Transactional outbox; delivery status and retry; in-app notification inbox; provider failure does not erase business event. | Force provider failure; event remains visible and retryable. |
| G-12 | P1 | No robust officer assignment, SLA/overdue queue, escalation, or handover model is evident. | Assignment, due dates, reasoned reassignment, escalation and workload dashboards. | Unassigned/overdue queues are correct under concurrent updates. |
| G-13 | P1 | Property lifecycle history (ownership/occupancy change, renovation, damage, replacement) is not represented as a dedicated immutable event model. | Add append-only lifecycle events with actor, source, effective date, confidence, links to evidence and supersession relationship. | Editing current facts does not erase old event history. |
| G-14 | P1 | Search/list scaling risks: broad collection loading and repeated related-record checks can produce unbounded responses/N+1 queries. | Pagination, filters, projections, query plans, appropriate indexes and measured query budgets. | 95th-percentile query target measured against seeded dataset. |
| G-15 | P1 | No complete threat-model, privacy/retention schedule, or incident response procedure is established by the runtime. | Document data classes, purposes, access, retention, deletion/legal hold, incident handling and audit access. | Approved checklist and tested access/deletion workflows. |
| G-16 | P1 | Test coverage appears concentrated on startup/context rather than complete user journeys. | Unit + repository + security integration + provider contract + browser E2E + accessibility tests. | CI runs all fast tests; gated sandbox suite runs before release. |
| G-17 | P2 | No verified OCR/AI extraction, anomaly detection, multilingual jurisdiction configuration, mobile client, or integrations are established. | Add only behind explicit opt-in, confidence/provenance, human review and jurisdictional validation. | Feature flag, quality benchmark, privacy review and rollback plan. |

**Important:** the current package's documentation includes historical claims that certain tests/builds passed in earlier environments. In this handoff, those claims are not treated as a fresh verification run. A fresh Maven test run, clean frontend install/build, live MySQL, Razorpay sandbox, and browser E2E suite remain separate release evidence.

## 4. Product boundaries and user roles

### Roles

- **Visitor:** sees only intentionally public, minimized registry data and public guidance.
- **Applicant:** manages their own profile, properties, draft/submitted applications, evidence, payments, corrections and notifications.
- **Review officer:** sees only assigned cases within the configured jurisdiction; reviews evidence; requests corrections; records a reasoned decision.
- **Supervisor:** assigns/reassigns work, reviews escalations, sees workload and SLA metrics; cannot silently edit applicant evidence.
- **Administrator:** manages accounts, roles, jurisdiction/configuration and operational health; privileged actions require reason and audit event.
- **Auditor/read-only reviewer:** sees approved audit records and evidence access history only within granted scope.
- **Integration service account:** narrowly scoped permissions for payment webhooks, email/SMS delivery, storage scanning or government adapters; never a generic admin account.

### Explicit boundary

PropertySetu can track the platform's workflow and evidence. It cannot state that ownership is legally proven, a document is genuine, a government office has accepted a filing, or a registration is official unless a validated authorized source actually confirms that precise claim. External registry APIs and legal requirements are jurisdiction-specific integrations, not assumptions.

## 5. User journeys and acceptance criteria

### 5.1 Account creation and access

1. User registers with normalized email/phone and password.
2. Account is created in `PENDING_CONTACT_VERIFICATION` or equivalent state.
3. Server sends a single-use, short-lived challenge using the configured provider. Demo challenge is permitted only in a clearly marked local/demo profile and cannot silently become production mode.
4. Server validates challenge, expiration, attempt count, and replay status; then sets the relevant verification flag.
5. Login returns short-lived access token; refresh/session revocation policy is explicit.
6. User can recover account without revealing whether a target email exists.

Acceptance: rate limits, duplicate registration, wrong/expired/replayed OTP, disabled account, password reset replay, role change, and token expiration are tested.

### 5.2 Applicant application lifecycle

`DRAFT → READY_TO_SUBMIT → SUBMITTED → PAYMENT_PENDING (if required) → PAID/WAIVED → ASSIGNED → UNDER_REVIEW → NEEDS_CORRECTION ↔ RESUBMITTED → APPROVED/REJECTED → CLOSED`

- `READY_TO_SUBMIT` is a derived readiness state or explicit persisted state, but must not be confused with submitted.
- Payment is a separate financial state machine; application status must not be the only payment source of truth.
- `APPROVED` means approved by the authorized reviewer in this workflow, not government registration unless a government adapter verifies it.
- `CLOSED` means the platform workflow is closed; it does not imply title transfer or legal completion.
- Rejection requires a reason and policy-specific appeal/reopen rule.
- Every transition has preconditions, actor role, event timestamp, reason/code, correlation ID and notification event.

Submission preconditions: applicant owns/controls the case, required fields pass validation, required document categories exist, files are scanned/available, applicant acknowledges the informational/legal boundary, and required fee policy is configured. Required document checklist is transaction- and jurisdiction-configurable; the current three-category checklist is an academic default, not universal legal advice.

### 5.3 Officer review

1. Queue displays assigned, unassigned (supervisor only), due-soon and overdue cases.
2. Officer opens a case and sees a minimized case summary plus access to relevant evidence.
3. Every document receives a decision such as `ACCEPTED_FOR_WORKFLOW`, `CORRECTION_REQUIRED`, `UNREADABLE`, `MISMATCH`, or `NOT_APPLICABLE`, with reason and optional location/annotation.
4. A correction request is structured, understandable, and scoped to the exact document/field; applicant sees who requested it (where policy allows), why, what to do, and deadline.
5. Resubmission creates a new document version; previous versions and review decisions remain visible to authorized actors.
6. Final decision requires all mandatory review items to be resolved and records decision reason, policy/checklist version, reviewer and time.

### 5.4 Payment

Separate states: `CREATED`, `PENDING_PROVIDER`, `AUTHORIZED`, `CAPTURED`, `FAILED`, `CANCELLED`, `REFUND_PENDING`, `REFUNDED`, `RECONCILIATION_REQUIRED`.

- Store provider order/payment IDs, expected amount in minor currency units, currency, idempotency key, timestamps and sanitized provider result.
- Verify signature server-side using exact provider contract; verify order, payment ID, amount, currency and capture state.
- Webhook processing is idempotent by provider event ID/payment ID and signature-verified against raw payload.
- UI success callback is a hint to refresh server status, never the authoritative payment record.
- If browser closes after payment, return to a recoverable pending screen; poll/read status and reconcile.
- A delayed or duplicated callback must not double-charge, double-advance the application, or duplicate notifications.
- Use test credentials only until legal entity, refund handling, terms, support and compliance are ready.

### 5.5 Public registry

- Return only deliberately public records whose disclosure policy permits them.
- Use allowlisted DTO fields, never serialize JPA entities directly.
- Public response should avoid owner names/contact details, document paths, exact sensitive coordinates, application notes and internal verification comments.
- Show source, status and last-updated time; explain whether data is self-reported, document-reviewed, or confirmed by an external authority.
- Distinguish “not found” from “service unavailable”; do not render a failed request as an empty registry.
- Add pagination and query filters. Never expose all records through an unbounded list endpoint.

## 6. Target architecture

```text
React/Vite UI
  ├─ Public Registry / Guidance
  ├─ Applicant Workspace
  ├─ Officer Workbench / Supervisor Queue
  └─ Admin & Audit Console
       │ HTTPS + versioned REST DTOs
Spring Boot Modular Monolith
  ├─ Identity & Access (accounts, challenges, roles, sessions)
  ├─ Property Identity (property, location, jurisdiction, external IDs)
  ├─ Applications (drafts, submission, assignment, state machine)
  ├─ Evidence (private files, versions, checksum, scanning, review)
  ├─ Payment Ledger (orders, provider events, reconciliation, refunds)
  ├─ Decisions & Policy (checklists, review outcomes, reason codes)
  ├─ Lifecycle History (append-only property events and links)
  ├─ Notifications (outbox, inbox, provider delivery/retry)
  ├─ Audit & Security Events
  └─ Integration Adapters (payment, SMS/email, registry APIs, OCR later)
       │
MySQL: transactional domain records, constraints, migrations, outbox
Private object storage: encrypted documents, versioned keys, scan/quarantine
Observability: structured logs, metrics, traces, alerts, redaction
```

### Package layout target (evolve incrementally, do not big-bang rewrite)

```text
backend/src/main/java/com/gaurav/property/
  identity/{controller,service,entity,repository,dto,policy}
  property/{controller,service,entity,repository,dto}
  application/{controller,service,entity,repository,dto,workflow}
  evidence/{controller,service,entity,repository,storage,scanning,dto}
  payment/{controller,service,entity,repository,provider,dto,reconciliation}
  review/{controller,service,entity,repository,policy,dto}
  lifecycle/{controller,service,entity,repository,dto}
  notification/{service,entity,repository,provider,outbox}
  audit/{service,entity,repository,dto}
  shared/{config,exception,validation,observability}
```

Keep the existing package layout during initial hardening if moving classes would destabilize the academic demo. Domain package extraction is a refactor after behavior tests exist.

## 7. Core data model (target)

### Identity and access

- `user_accounts`: id, normalized_username/email/phone, password_hash, account_status, role assignment/version, created_at, updated_at, disabled_at.
- `contact_verifications`: id, user_id, channel, destination_hash, challenge_hash, issued_at, expires_at, verified_at, attempts, consumed_at, provider_ref. Never store plaintext OTP.
- `role_assignments`: user_id, role, jurisdiction_id, valid_from, valid_until, assigned_by; administrative changes are audited.
- `officer_assignments`: case_id, officer_id, assigned_by, assigned_at, due_at, released_at, release_reason, assignment_version.

### Property identity and lifecycle

- `properties`: stable internal ID, property reference, property type, jurisdiction, normalized address/location ID, status, created/updated timestamps.
- `property_external_identifiers`: property_id, issuing_authority, identifier_type, value_hash or protected value, jurisdiction, source, verified_at; uniqueness scoped to authority/jurisdiction.
- `property_relationships`: source_property_id, target_property_id, relation (`SUBDIVIDED_FROM`, `MERGED_FROM`, `REPLACED_BY`, `SAME_SITE_AS`), effective date, evidence, decision status.
- `property_lifecycle_events`: event ID, property ID, event type, effective date, recorded_at, actor, source class, confidence/verification status, summary, evidence references, supersedes_event_id, correlation ID. Append-only; correction creates a superseding event rather than destructive overwrite.

### Application and evidence

- `registration_applications`: existing core plus jurisdiction, checklist_version, assigned_officer, submitted_at, due_at, closed_at, decision_reason_code, optimistic lock version.
- `application_state_events`: application_id, from_state, to_state, actor_id/role, reason_code, reason_text, event_time, correlation_id, metadata (allowlisted JSON).
- `documents`: existing metadata plus storage_key (not public URL), version, checksum_sha256, original_name sanitized, detected_mime, size_bytes, scan_state, uploaded_by, uploaded_at, supersedes_document_id, retention_until.
- `document_reviews`: document_id/version, reviewer, outcome, reason_code, notes, reviewed_at, checklist_item_id.
- `correction_requests`: application_id, document_id or field path, requested_by, reason, created_at, due_at, applicant_response, resolved_at.
- `checklist_versions` and `checklist_items`: jurisdiction + transaction purpose + effective period; required category, acceptable formats, help text and source URL/date.

### Payments, notifications, audit

- `payment_orders`: application_id, provider, provider_order_id, amount_minor, currency, state, idempotency_key, created_at, expires_at.
- `payment_events`: provider_event_id unique, payment/order IDs, signature_valid, event_type, received_at, processed_at, processing_result; store sanitized payload only.
- `payment_reconciliation_cases`: payment_order_id, discrepancy_type, opened_at, owner, resolution, resolved_at.
- `notification_outbox`: event ID, event type, aggregate ID, payload, created_at, next_attempt_at, attempts, delivery_state, provider_message_id, last_error_code.
- `notifications`: user_id, type, title, body, link target, created_at, read_at.
- `audit_events`: actor, role, action, resource type/id, result, reason, occurred_at, request/correlation ID, redacted metadata. Audit is append-only to normal app roles.

### Database rules

- Use versioned schema migrations (Flyway or Liquibase) rather than relying on Hibernate `ddl-auto=update` in production.
- Foreign keys, unique constraints, check constraints where supported, indexes matching real filters, and optimistic locking for case/payment transitions.
- Use UTC instants for event timestamps; render in user's configured timezone. Keep effective/legal dates separately from recording timestamps.
- Never persist monetary amounts as floating point. Use integer minor units + ISO currency code.
- Do not store raw OTPs, payment secrets, passwords, full provider payloads with PII, or private file bytes in database rows.

## 8. API contract (target; version as `/api/v1` when breaking changes are introduced)

Existing endpoints must be inventoried before renaming. New target endpoints:

| Method | Path | Purpose | Authorization |
|---|---|---|---|
| GET | `/api/v1/me` | Current safe account profile and verification state | Authenticated |
| POST | `/api/v1/applications` | Create draft | Applicant |
| GET | `/api/v1/applications?status=&page=&size=` | Own cases; officers only assigned scope | Applicant / assigned officer |
| GET | `/api/v1/applications/{id}` | Scoped case detail | Owner / assigned officer / supervisor policy |
| POST | `/api/v1/applications/{id}/submit` | Validate readiness and submit | Owner |
| POST | `/api/v1/applications/{id}/assignments` | Assign/reassign with reason | Supervisor/Admin policy |
| POST | `/api/v1/applications/{id}/corrections` | Request correction with structured items | Assigned officer |
| POST | `/api/v1/applications/{id}/resubmit` | Submit corrected evidence | Owner |
| POST | `/api/v1/applications/{id}/decision` | Approve/reject with reason and preconditions | Assigned officer; elevated policy for final decision |
| POST | `/api/v1/applications/{id}/documents` | Upload new document version | Owner, allowed case state |
| GET | `/api/v1/applications/{id}/documents/{docId}/content` | Stream private file after relationship check | Owner / assigned officer |
| POST | `/api/v1/payments/orders` | Create/reuse idempotent provider order | Owner; configured fee policy |
| GET | `/api/v1/payments/orders/{id}` | Authoritative payment status | Owner / authorized finance role |
| POST | `/api/v1/integrations/razorpay/webhook` | Signed provider event receiver | Provider signature validation; no user JWT |
| GET | `/api/v1/notifications` | Current user's notification inbox | Authenticated |
| PATCH | `/api/v1/notifications/{id}/read` | Mark notification read | Owner |
| GET | `/api/v1/properties/{id}/timeline` | Authorized lifecycle history | Owner/authorized staff; public projection separate |
| POST | `/api/v1/properties/{id}/lifecycle-events` | Record supported lifecycle event | Owner or authorized staff by policy |
| GET | `/api/v1/public/properties?page=&size=&q=` | Minimized, verified public listing | Public, rate-limited |
| GET | `/api/v1/admin/audit?resource=&actor=&from=&to=` | Scoped audit search | Auditor/Admin, reasoned access |

All endpoints require DTO validation, bounded pagination, consistent problem responses, correlation IDs, rate limiting on sensitive operations, and explicit access tests. Avoid returning JPA entities directly.

## 9. Authorization matrix

| Resource/action | Visitor | Applicant owner | Unrelated applicant | Assigned officer | Unassigned officer | Supervisor | Admin |
|---|---:|---:|---:|---:|---:|---:|---:|
| Public verified listing | Read minimized fields | Read | Read | Read | Read | Read | Read |
| Read private application | No | Yes, own | No | Yes, assigned scope | No by default | Scope-dependent | Emergency/admin policy only, audited |
| Upload/replace evidence | No | Yes, allowed states | No | No | No | No | No routine ability |
| Read private evidence | No | Yes, own case | No | Yes, assigned case | No | Only scoped escalation | Break-glass only with reason/audit |
| Review document/decide | No | No | No | Yes, assigned | No | Only policy-approved escalation | Not by default; privileged exception audited |
| Assign/reassign officer | No | No | No | No | No | Yes | Yes, audited |
| View payment status | No | Own payment | No | Limited to case/payment status if needed | No | Scope-dependent | Finance/ops scope only |
| Change roles/config | No | No | No | No | No | Limited | Yes, audited |

Implement **deny by default** and authorize each request against current database relationships, not just UI visibility or a JWT role string. Authorization must cover collection, detail, nested resource, download, mutation, export, search, and background jobs.

## 10. Security and privacy controls

### P0 controls before any public demo with user-submitted data

- Rotate all credentials ever included in an uploaded ZIP, Git commit, screenshot, or pasted config; invalidate the old JWT secret. Use distinct secrets for JWT and payment provider.
- Production CORS allowlist is exact and environment-specific; no wildcard deployment previews.
- JWT signing secret is strong, externally configured, and validated at startup; access tokens have explicit expiry. Account disable/role change invalidates access within a defined bound.
- Enforce server-side ownership and assignment checks on every endpoint and file stream.
- Limit login, registration, OTP send/verify, password reset, document upload and public search rates; generic recovery responses prevent account enumeration.
- Use private storage, generated storage keys, path traversal prevention, content sniffing, allowlisted formats, maximum size, antivirus/quarantine and secure download headers.
- Never log authorization headers, tokens, OTPs, full identity numbers, file contents, payment signatures or secrets.
- Configure production HTTPS, secure secret injection, database least privilege, backups, restore test and dependency scanning.
- Audit privileged reads and writes, especially document access, role changes, officer assignment, decisions, payment reconciliation and exports.

### Data minimization

Classify fields as public, account-private, case-private, restricted identity, financial, or operational secret. Define purpose and retention for each. Avoid collecting national ID numbers unless the real workflow legally needs them. For academic demos, use synthetic identities/documents. Public registry data should be a separate projection with explicit publication consent/policy, not a hidden view over private rows.

### AI/OCR policy if added later

OCR and AI may extract fields or highlight inconsistencies, but must return extracted value + source page/region + confidence + model/version + timestamp. Low confidence is “needs review,” not a failure verdict. AI must not automatically declare legal title, fraud, or authenticity. Human reviewer remains responsible for consequential decisions; users can correct extracted facts.

## 11. Reliability, consistency and failure recovery

| Failure | Correct behavior | User-facing truth | Recovery mechanism |
|---|---|---|---|
| Upload request times out | Check idempotency/checksum and current document versions before retry | “We’re checking whether the upload completed” | Safe retry; no duplicate current version |
| File scan unavailable | Keep file quarantined and block submission | “File received; security scan is still pending” | Retry scanner; alert operator |
| Applicant closes checkout | Do not infer failure or success | “Payment status is being confirmed” | Refresh status; webhook/reconciliation |
| Duplicate webhook | Process once | Same final payment state | Unique provider event ID/idempotent transition |
| Amount/currency mismatch | Do not mark captured/paid for application | “Payment needs review; do not pay again yet” | Reconciliation queue |
| Officer decision saved but email fails | Decision remains committed; notification stays queued | Show decision in portal; indicate email delivery separately | Outbox retry and delivery status |
| Concurrent officer updates | One transition wins; stale request receives conflict | Explain record changed and reload | Optimistic lock/version check |
| Database unavailable | Return controlled error; no false success | “Service temporarily unavailable; no confirmation recorded” | Retry with correlation ID, recover DB |
| User loses connection during submit | Read authoritative status before resubmission | “Checking submission status” | Idempotent command / request key |
| Case is reassigned | Old officer loses access according to policy; new officer gains access | Timeline records assignment change | Re-evaluate assignment on every request |
| Record needs correction | Preserve original and append correction/supersession | Show current version and history to authorized users | Versioned event model |
| Public registry API fails | Display service error, not empty-state success | “Registry could not be loaded” | Retry, telemetry, bounded cache if appropriate |

Use transactional outbox for side effects that must follow a database commit. Do not call external notification providers in a way that can roll back or ambiguously duplicate a committed decision. Use idempotency keys for submission/payment/decision commands where retry is possible.

## 12. Performance and scale targets

Do not claim “millions of users” as an architectural property until load tests prove it. Use measurable targets and a representative dataset. Initial pilot targets to validate:

- List endpoints paginated by default (maximum page size 100).
- Typical authenticated read p95 under 500 ms at a declared load and dataset; search p95 under 800 ms, excluding external integrations.
- No N+1 query pattern on application queue or registry; record query count and explain plans.
- Upload streaming rather than buffering large files in memory.
- All expensive exports and batch tasks are asynchronous and permission-scoped.
- Indexes start with observed filters such as `(user_id, status, created_at)`, `(assigned_officer_id, status, due_at)`, `(property_id, created_at)`, and provider event IDs; validate indexes using actual query plans rather than adding blindly.
- Load test concurrent application submissions, duplicate payment events, officer queue filtering, document access, and public search.

## 13. Observability and operations

Structured logs should include timestamp, level, service/version, environment, request ID, trace ID, actor ID (pseudonymous where appropriate), action, resource type/id, result code and duration. Exclude sensitive payloads. Metrics: HTTP latency/error rate, login/OTP throttles, upload rejection/scan backlog, queue depth, cases overdue, payment pending age, webhook signature failures, reconciliation backlog, notification retries, DB pool saturation and backup/restore status. Alerts must have an owner and runbook; telemetry without response procedures is not operational readiness.

Operational requirements:

- `/api/health` liveness is separate from readiness/dependency status.
- Backups encrypted, access-controlled, and restore-tested on a schedule.
- Database migrations run once through release pipeline; rollback strategy is documented.
- Deployments use separate dev/test/staging/prod config and secrets.
- Feature flags gate risky features and external integrations.
- Incident response includes containment, credential rotation, evidence preservation, user notification assessment and post-incident review.

## 14. Test strategy and release gates

### Unit tests

- State machine: all valid/invalid transitions, role/precondition matrix.
- Authorization policy: owner/assignment/jurisdiction/disabled user.
- Document validation: allowed extension, MIME mismatch, magic bytes, oversized, empty, path traversal, duplicate version.
- Payment transitions: duplicate/reordered provider events, mismatched amount/currency, signature failure, refund and reconciliation.
- Checklist policy: transaction/jurisdiction/version effective date.

### Integration tests

- MySQL-compatible database, migration from empty DB, migration from current schema, foreign-key and unique constraints.
- JWT invalid/expired/disabled/role-change behavior.
- Every API resource's collection/detail/nested/download authorization.
- Outbox retry and notification-provider outage.
- Officer assignment and reassignment under concurrent requests.

### End-to-end journeys

1. Register → verify contact → login → create owner/property/location → draft application.
2. Upload required evidence → submit → payment pending → sandbox payment → confirmed server state.
3. Officer assignment → evidence review → correction request → applicant resubmission → review → final decision.
4. Notification provider fails after decision; portal remains correct and notification retries.
5. Applicant A tries to access Applicant B's application, documents, payments and audit trail; all denied.
6. Unassigned officer tries to access assigned case; denied.
7. Public registry shows only approved/publicly publishable record and no personal/private fields.
8. Payment browser closes; app recovers from authoritative status.
9. Existing record corrected/superseded; old history remains visible to authorized reviewer.
10. Keyboard-only and screen-reader checks for all critical forms and error states.

### Release gates

- [ ] Secret scan clean; exposed keys rotated.
- [ ] Clean dependency install succeeds from lockfile.
- [ ] Backend compile and tests pass on supported Java/Maven.
- [ ] Database migration succeeds from empty and prior schema.
- [ ] Frontend lint and production build pass after clean install.
- [ ] All P0 authorization/security tests pass.
- [ ] Sandbox payment and webhook matrix passes.
- [ ] E2E correction/resubmission journey passes.
- [ ] Backups and restore test documented.
- [ ] Public copy explicitly states limitations and no government affiliation.
- [ ] No real sensitive personal documents used in public demo.

## 15. Delivery roadmap — ordered by dependency

### Milestone 0 — Reproducible baseline (do first)

- Clean ZIP excludes `node_modules`, `target`, IDE caches, logs and secrets.
- Record exact Java, Node, npm and Maven versions.
- Clean frontend install/build and backend test with retained logs.
- Resolve the Maven wrapper and Java target mismatch (the inspected POM sets Java 17 while product notes mention Java 21; choose and document one supported baseline).
- Establish baseline API inventory, database migration and a smoke test script.

**Exit:** another developer can clone/unzip and run the project using documented steps.

### Milestone 1 — Trust and security core (P0)

- Rotate secrets; production CORS; account status/role invalidation.
- Officer assignment and jurisdiction scope.
- Central state machine and optimistic concurrency.
- Upload authorization/storage policy and file scanning/quarantine seam.
- Payment idempotency/webhook/reconciliation verification.
- Structured audit and safe public projections.

**Exit:** security and transition tests pass; no cross-user or cross-officer access.

### Milestone 2 — Complete end-to-end application workflow (P1)

- Structured checklist versions.
- Per-document review and correction items.
- Versioned evidence and resubmission.
- In-app notifications + transactional outbox + retry.
- Officer work queue, due dates, SLA and assignment history.

**Exit:** applicant can finish the complete correction loop without external manual explanation.

### Milestone 3 — Property history and operational workflows (P1/P2)

- Append-only property lifecycle timeline.
- Maintenance/condition event workflow (report, assignment, evidence, resolution) only if interviews validate this as a priority.
- Ownership/occupancy change event model with explicit legal-source boundary.
- Supervisor workload and operational dashboards.

**Exit:** edits preserve historical context and every operational case has an owner and next action.

### Milestone 4 — Research-gated differentiators (P2)

- OCR suggestions with confidence and provenance.
- Jurisdiction-specific checklist packs and localization.
- External registry adapters after official API/contract verification.
- Analytics based on minimized, consented data.
- Mobile/offline capability only after use-case and sync-conflict analysis.

**Exit:** each feature has user evidence, privacy/security review, benchmark and rollback plan.

## 16. User research plan

Interview at least 5–8 people from each reachable group (applicants/property owners, residents, society/building managers, review/office staff if access is legitimate, and service providers) before making broad product claims. Ask them to walk through their last real task, show where allowed, identify delays/rework/uncertainty, and explain what they trusted or did not trust. Do not ask only “Would you use this app?” Record anonymized task, pain point, frequency, current workaround, consequence, evidence, and confidence. Validate willingness to use, legal boundaries, data access, accessibility and language needs.

Competitor review should compare task completion, assignment, evidence, correction loops, audit, payments, maintenance, integrations, accessibility, privacy, and pricing—not just landing-page features. Existing categories to benchmark include property management suites and resident/community portals. Avoid claiming a unique market gap until customer interviews and a feature-by-feature review support it.

## 17. Definition of done for any feature

A feature is not “done” because a button exists. It is done only when:

1. User story and non-goals are defined.
2. Permissions are specified and negative authorization tests exist.
3. DTO validation and consistent error states exist.
4. Database constraints/migration and rollback are considered.
5. Audit, privacy, retention and user-facing status are correct.
6. Retries, concurrency and provider failure are addressed.
7. Unit/integration/E2E tests cover success and failure paths.
8. UI is responsive, keyboard-accessible and understandable without hidden assumptions.
9. Logs/metrics are useful and redact secrets/PII.
10. Documentation says what the feature does and what it does not do.

## 18. Open decisions (must not be guessed)

- Which exact first jurisdiction and transaction types are in scope for the academic demo versus a real pilot?
- Is payment a demo-only fixed fee or a configurable official fee? Who is merchant of record and who handles refunds/disputes?
- Who assigns officers and defines jurisdiction? Are officer roles simulated for academic evaluation or connected to a real authority?
- What source establishes a public registry record as publishable, and what consent/legal basis permits publication?
- What data retention/deletion periods apply to identity documents, decisions, payment records and audit events?
- Is `PropertyDesk` a separate learning project or intended to become a module?
- Which notification provider is approved for production, and what delivery SLAs are realistic?
- Which languages and accessibility requirements are required for the first pilot?
- What load profile and service-level targets are realistic for the first deployment?

## 19. References and engineering anchors

Use authoritative sources for implementation and re-check them during development:

- OWASP Authorization Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html
- OWASP File Upload Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/File_Upload_Cheat_Sheet.html
- NIST Digital Identity Guidelines (SP 800-63-4): https://www.nist.gov/publications/nist-sp-800-63-4-digital-identity-guidelines
- Spring Boot observability reference: https://docs.spring.io/spring-boot/reference/actuator/observability.html
- Maharashtra Department of Registration & Stamps: https://igrmaharashtra.gov.in/ (verify current rules and relevant service pages directly)
- PRS legislative research: https://prsindia.org/ (use current reports and distinguish digitization metrics from legal title assurance)

**Final rule:** do not optimise for a bigger diagram, a larger LOC count, or a claim of “enterprise-grade”. Optimise for a user completing a consequential task safely, understanding the exact state of the task, and recovering when something goes wrong.