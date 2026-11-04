# Database Setup and Schema Notes

## Local database

Run the safe bootstrap SQL in MySQL Workbench:

```sql
CREATE DATABASE IF NOT EXISTS property_registration_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

The statement creates the schema only when it does not exist. It does not drop tables or delete existing records.

The backend connection is configured using environment variables. Do not publish credentials from an example configuration.

## Core domain entities

1. user_accounts — applicant, officer and administrator identities
2. owners — owner profile and contact details
3. properties — property description, type and owner relationship
4. locations — address and geographic/jurisdiction fields
5. registration_applications — application purpose, references, workflow state and optional assigned officer/timestamp
6. documents — document type, metadata, private storage reference, version number and review state
7. verifications — officer decision, date, state and remarks
8. payments — gateway order/payment references and verification status
9. audit_events — actor, action, target entity and event time

Exact names and mappings follow the JPA entity annotations in source. Existing database records should be backed up before schema changes.

## Local configuration

Configure at least:

- DB_URL
- DB_USERNAME
- DB_PASSWORD
- JWT_SECRET (the Windows launcher generates an ephemeral secret when omitted)
- UPLOAD_DIR
- FRONTEND_ORIGIN
- ADMIN_BOOTSTRAP_USERNAME / ADMIN_BOOTSTRAP_EMAIL / ADMIN_BOOTSTRAP_PASSWORD only for a new local database without an administrator
- RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET for Razorpay Test Mode checkout
- RAZORPAY_WEBHOOK_SECRET for signed webhook processing

Never commit values for these variables. Use backend/.env.example only as a checklist of expected configuration; copy it to backend/.env.local for your machine.

## Schema evolution

Hibernate's ddl-auto=update is configured for the local academic demonstration so that nullable officer-assignment fields and document-version fields can be added without dropping existing rows. This is not a production migration strategy. Before production, add Flyway or Liquibase migrations, test upgrades from prior versions, and switch deployment schema handling to validate.

## Data protection checklist

- Use fake identities and synthetic sample documents.
- Store uploads outside the public frontend directory.
- Apply file type/size validation and authenticated, case-scoped authorization for file access.
- Preserve superseded document metadata for review history.
- Avoid public database dumps and uploaded documents.
- Keep gateway secrets and JWT signing keys private.
