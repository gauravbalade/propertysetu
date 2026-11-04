# PropertySetU — final local setup (Windows)

This repository contains two different applications. **PropertySetU** is the full React + Spring Boot application in `frontend/` and `backend/`. `PropertyDesk/` is a separate project and is intentionally not merged into PropertySetU.

## Requirements

- Windows 10/11
- JDK 17 or newer (your existing Java 21 installation is compatible with the backend's Java 17 compilation target)
- Node.js 22.12+ and npm (Vite 8 requires a recent supported Node release)
- MySQL Server and MySQL Workbench
- Internet access on the first Maven/npm dependency download

## 1. Prepare the database once

Start the local MySQL service. Open MySQL Workbench and execute:

```sql
CREATE DATABASE IF NOT EXISTS property_registration_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

The same safe statement is in `database/create-local-database.sql`. It does not drop the schema or delete existing records.

Default connection: `jdbc:mysql://localhost:3306/property_registration_db`. If MySQL uses another port or host, update `DB_URL` in the backend local environment file below.

## 2. Configure the backend once

From the repository root, copy:

- `backend/.env.example` → `backend/.env.local`
- `frontend/.env.example` → `frontend/.env.local` (optional; the Vite development default already points to localhost)

Open `backend/.env.local` and set your local MySQL username and password:

```dotenv
DB_URL=jdbc:mysql://localhost:3306/property_registration_db?useSSL=false&serverTimezone=Asia/Kolkata
DB_USERNAME=root
DB_PASSWORD=your-local-mysql-password
FRONTEND_ORIGIN=http://localhost:5173
DDL_AUTO=update
```

Do not commit `.env.local`. The repository ignores local environment files and generated uploads.

### First administrator (new local database only)

If your database already has an administrator account, keep using it and leave the bootstrap settings blank.

For a fresh database that has no admin, set all three optional values in `backend/.env.local`:

```dotenv
ADMIN_BOOTSTRAP_USERNAME=admin
ADMIN_BOOTSTRAP_EMAIL=admin@example.com
ADMIN_BOOTSTRAP_PASSWORD=choose-a-unique-password-at-least-12-characters
```

On startup, PropertySetU creates this account only if that username and email are unused. It never changes an existing account or resets its password. After the first successful startup, remove `ADMIN_BOOTSTRAP_PASSWORD` from the local file and restart the backend. Use the Admin dashboard to create Officer accounts.

Applicant registration is available from the website. Public registration creates applicant accounts only; it cannot create admin or officer accounts.

## 3. Start the complete application

Double-click **`START_PROPERTYSETU.bat`** in the repository root.

It opens two terminal windows:

- Backend: Spring Boot API on `http://localhost:8080`
- Frontend: Vite website on `http://localhost:5173`

Keep both terminal windows open while using the website. The backend terminal asks for the MySQL password only when `DB_PASSWORD` is blank. The launcher generates a fresh local JWT signing secret when none is configured and uses a local uploads folder at `demo/uploads/`.

You can also start each part manually:

**Backend terminal:**

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

**Frontend terminal:**

```powershell
cd frontend
npm ci
npm run dev
```

Open `http://localhost:5173`. Check the API at `http://localhost:8080/api/health`.

## 4. Razorpay payment setup

Payment checkout is locked to **Razorpay Test Mode**. To use it, add your own Test Mode values to `backend/.env.local`:

```dotenv
RAZORPAY_KEY_ID=rzp_test_your_key_id
RAZORPAY_KEY_SECRET=your_test_key_secret
RAZORPAY_WEBHOOK_SECRET=your_webhook_secret
```

The key ID must start with `rzp_test_`. Do not use live credentials. Webhook processing is optional for the local UI flow; checkout verification also queries Razorpay from the backend. Without Test Mode credentials, the rest of the application can start, but checkout will show that payment is not configured.

Never enter actual card details, CVV, UPI PIN, bank passwords, or real payment OTPs in an academic demonstration.

## 5. Main workflows to demonstrate

1. Register an Applicant and sign in.
2. Create the applicant profile, property and location.
3. Create an application and upload the three required synthetic files: Identity Proof, Address Proof and Property Document.
4. Review the summary and submit. Complete the Razorpay Test Mode checkout if configured.
5. Sign in with the Admin account. Create one or more Officer accounts and assign the paid application to an officer.
6. Sign in as the assigned Officer. Review the uploaded files and mark each required document verified, or request a correction by marking a document rejected.
7. Sign back in as the Applicant. Replace the rejected document with a new version and resubmit. Earlier document versions are retained in the record.
8. Sign in as the assigned Officer again and complete verification once the latest required document versions are verified.

The Admin can monitor cases and assign/reassign them. The assigned Officer performs document review and final verification. An Officer cannot list or open another officer's unassigned cases.

## 6. Troubleshooting

- **MySQL connection refused:** start MySQL in Windows Services / MySQL Workbench, verify the port and credentials in `backend/.env.local`.
- **Unknown database:** execute `database/create-local-database.sql` in MySQL Workbench.
- **Port 8080 already in use:** stop the other backend or change `SERVER_PORT` in `backend/.env.local`.
- **Port 5173 already in use:** stop the other Vite process before starting the frontend.
- **Maven dependency resolution error:** verify internet access, then rerun `.\mvnw.cmd test` from `backend/`.
- **Frontend dependency error:** delete only `frontend/node_modules` and rerun `npm ci` from `frontend/`.
- **Invalid JWT after backend restart:** the launcher generates an ephemeral JWT secret if one is not configured. Sign in again; old sessions are expected to expire.
- **Payment not configured:** add Razorpay Test Mode credentials; do not replace them with live keys.
- **Upload not found:** keep the backend running and confirm the upload folder is writable.

## Project boundary

PropertySetU is an academic workflow demonstration. It is not an official government registration portal, a title certificate, a registrar, a legal title search, or legal advice. “Payment confirmed”, “documents reviewed” and “academic workflow completed” are distinct statuses; completion here does not mean an official property registration has taken place. Use synthetic applicant details and demo files only.
