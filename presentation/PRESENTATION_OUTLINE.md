# PropertySetu — Presentation Outline

## Recommended 5–7 minute story

### 1. Problem (30 seconds)
Property registration involves multiple parties, property details, supporting documents, payment, and review. Applicants often lack a clear status view, while officers need structured evidence and accountability.

### 2. Solution (30 seconds)
PropertySetu provides one guided workflow from applicant registration to protected document review and final status.

### 3. Architecture (45 seconds)
React provides the user experience. Spring Boot exposes secured REST APIs. Spring Security and JWT protect role-based access. MySQL stores structured records. Uploaded files remain outside the database and are accessed through protected endpoints.

### 4. Applicant demonstration (2 minutes)
Register/login → create owner → create property → save location → create application → upload required documents → submit → complete clearly labelled test payment.

### 5. Officer demonstration (1.5 minutes)
Login as officer → search/filter application → open protected document → review each document → inspect accountability history → verify or reject.

### 6. Engineering strengths (45 seconds)
Mention ownership checks, DTOs, validation, file-size/type/signature checks, JWT issuer validation, protected downloads, status transitions, audit events, and test-mode payment disclosure.

### 7. Honest limitations and future scope (30 seconds)
The project is an academic MVP. Production would require private object storage, malware scanning, retention controls, HTTPS, managed secrets, migration tooling, monitoring, legal review, and a verified payment-provider integration.

### 8. Closing (20 seconds)
PropertySetu focuses on clarity, protection, and accountable review—not just storing forms in a database.

## Demo rule

Never show real identity documents, real passwords, or real payment credentials in a recording. Use synthetic demo data and clearly label test-mode behaviour.
