# PropertySetu
## Property Registration and Document Verification System

### Abstract

PropertySetu is a full-stack academic system for guiding applicants through property-registration preparation and enabling authorised officers to review submitted records. It combines a React interface, a Spring Boot REST API, MySQL persistence, JWT authentication, protected document access, test-mode payment state management, and an auditable verification workflow.

### Problem statement

Property-registration preparation involves owner information, property details, location information, supporting documents, payment, and review. A fragmented or unclear process can lead to incomplete submissions and poor visibility into application status. Officers also need a structured way to review documents and record decisions.

### Objectives

- Provide a guided digital application workflow.
- Enforce required document categories before submission.
- Separate applicant and officer responsibilities.
- Protect documents through authenticated, authorised endpoints.
- Support a clearly labelled local test-payment flow.
- Record officer decisions and accountability history.
- Provide a responsive interface suitable for demonstration.

### Technology stack

| Layer | Technology |
|---|---|
| Frontend | React 19, Vite, responsive CSS |
| Backend | Spring Boot 4.1.1, Java, Spring Web |
| Persistence | Spring Data JPA, Hibernate, MySQL 8 |
| Security | Spring Security, BCrypt, JWT |
| Testing | Maven tests, ESLint, Vite production build, Postman workflow |
| Development tools | Eclipse/Spring Tools, VS Code, MySQL Workbench |

### System workflow

```text
Applicant registration and login
        ↓
Owner profile → Property → Location
        ↓
Registration application
        ↓
Required document checklist
        ↓
Submission → Test-mode payment
        ↓
Officer document-by-document review
        ↓
Verification decision and accountability history
```

### Main modules

1. **Authentication** — applicant registration, login, BCrypt password storage, and JWT sessions.
2. **Owner and property management** — applicant-owned profile, property, and location records.
3. **Application management** — controlled status transitions and ownership validation.
4. **Document management** — allowed type/size checks, file-signature checks, protected downloads, and review status.
5. **Payment module** — local test-mode order and success/failure transition with explicit disclosure.
6. **Officer verification** — role-protected review, required-document enforcement, rejection remarks, and final decision.
7. **Audit module** — persistent action history containing actor, role, timestamp, action, and details.

### Security and responsibility

The API does not trust applicant-supplied ownership or officer identity alone. Authenticated relationships are checked on the server. Stored file paths and passwords are not exposed in safe API responses. Protected files are served through a relationship-checked endpoint rather than a public static directory. JWTs use signature, expiry, and issuer validation.

### Validation

The project has been verified with:

- Backend Maven test suite connected to local MySQL.
- Frontend ESLint.
- Frontend production build.
- Protected-route and role-separation checks.
- Required-document and invalid-state-transition checks.
- Protected document access checks.
- Test-mode payment transition checks.
- Officer verification and audit-history checks.

### Limitations

This is an academic MVP and demonstration platform. It is not a government service and does not provide legal approval. Payment is test-mode. Production deployment would require HTTPS, migration-managed schema changes, private object storage, malware scanning, retention/deletion controls, managed secrets, rate limiting, monitoring, backups, privacy review, accessibility review, and a verified payment-provider integration.

### Future scope

- Android client using the same secured REST API.
- Office-specific checklist configuration.
- Notification centre and email/SMS integration.
- Accessibility and multilingual guidance.
- Private object-storage adapter with scanning and retention policies.
- Deployment observability and incident response workflows.

### Conclusion

PropertySetu demonstrates that a strong property-registration project is more than a collection of forms. It combines clarity for applicants, protection for sensitive documents, role separation for administration, and accountability for decisions.
