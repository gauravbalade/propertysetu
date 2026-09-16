# Database Setup and Schema Notes

## Local connection

- Host: `localhost`
- Port: `3307`
- Database: `property_registration_db`
- User: `root`
- Password: local development value only; do not commit real credentials

Create the database:

```sql
CREATE DATABASE property_registration_db;
```

The backend currently uses Hibernate `ddl-auto=update` for local development. For a production deployment, replace this with versioned Flyway or Liquibase migrations.

## Core entities

1. `user_accounts` — applicant, officer and admin identities
2. `owners` — owner identity and contact information
3. `properties` — property number, type, area, description and registration status
4. `locations` — address, city, district, state and pincode
5. `registration_applications` — application number, purpose, owner account and workflow status
6. `documents` — document type, file metadata and upload location
7. `verifications` — officer decision, date, status and remarks
8. `payments` — amount, test gateway order, payment status and signature flag

## Safe local reset

Only when local demo data can be discarded:

```sql
DROP DATABASE property_registration_db;
CREATE DATABASE property_registration_db;
```

Then restart the backend so Hibernate recreates the development schema.

## Data protection checklist

- Never commit passwords, JWT secrets, uploaded identity documents or production database dumps.
- Use fake identities for screenshots and demonstrations.
- Store uploads outside the public frontend directory.
- Add file size, MIME validation, malware scanning and authenticated download endpoints before production.
