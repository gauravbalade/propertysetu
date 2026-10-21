# Database Setup and Schema Notes

## Local database

Create a local schema in MySQL Workbench:

```sql
CREATE DATABASE property_registration_db;
```

The backend connection is configured using environment variables. Do not publish credentials from an example configuration.

## Core domain entities

1. `user_accounts` — applicant, officer and administrator identities
2. `owners` — owner profile and contact details
3. `properties` — property description, type and owner relationship
4. `locations` — address and geographic/jurisdiction fields
5. `registration_applications` — application purpose, references and workflow state
6. `documents` — document type, metadata and storage reference
7. `verifications` — officer decision, date, state and remarks
8. `payments` — gateway order/payment references and verification status

Names and exact mappings follow the JPA entity annotations.

## Local setup

Configure at least:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `UPLOAD_DIR`
- `FRONTEND_ORIGIN`
- `RAZORPAY_KEY_ID` and `RAZORPAY_KEY_SECRET` for Test Mode payments

Never commit values for these variables. Use `.env.example` only as a checklist of expected configuration.

## Schema evolution

Hibernate's `ddl-auto=update` is convenient for a local classroom demonstration only. Before production, add Flyway or Liquibase migrations, test upgrades from prior versions, and switch deployment schema handling to validation.

## Data protection checklist

- Use fake identities and synthetic sample documents.
- Store uploads outside the public frontend directory.
- Apply file type/size validation and authenticated authorization for file access.
- Avoid public database dumps and uploaded documents.
- Keep gateway secrets and JWT signing keys private.
