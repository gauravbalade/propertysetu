# Property Registration and Document Verification System

## Abstract

This project implements a digital workflow for property registration and document verification. It provides a structured path from applicant registration through property data capture, document upload, payment simulation, officer review and final verification.

## Problem statement

Manual property-registration workflows can be difficult to track, slow to review and vulnerable to missing documents or unclear status communication. The system provides a single workflow with visible states and role-based access.

## Objectives

- Capture owner, property and location information.
- Accept supporting documents through a web interface.
- Track applications through controlled states.
- Simulate registration-fee payment for demonstration.
- Give officers a focused review and decision dashboard.
- Protect authenticated APIs with JWT and role-based authorization.

## Architecture

The React frontend communicates with Spring Boot REST controllers. Spring services enforce workflow rules and Spring Data JPA persists the domain model in MySQL. Uploaded files are stored in a configured local directory. JWT bearer tokens secure protected requests.

## Modules

- Authentication and user management
- Owner and property management
- Location management
- Registration application workflow
- Document upload
- Test payment workflow
- Officer verification dashboard

## Result

The working MVP was validated with frontend lint/build checks, backend Maven tests, authenticated API calls, role restrictions, payment state transitions and verification state transitions.

## Conclusion

The system demonstrates a complete, understandable property-registration journey and provides a strong foundation for future production work such as migrations, cloud storage, audited permissions, real payment integration and deployment hardening.
