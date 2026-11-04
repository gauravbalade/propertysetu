# PropertySetU — Windows IDE Guide (Spring Tools + VS Code)

This guide is for the full React + Spring Boot PropertySetU application. Keep the MySQL database, Spring Boot backend, and React frontend running as separate parts.

## 0. Extract the right folder

1. Download and extract `PropertySetU-Final.zip` into a new folder, for example `D:\PropertySetU-Final`.
2. Open that extracted folder. The correct root contains:
   - `backend\pom.xml`
   - `frontend\package.json`
   - `START_PROPERTYSETU.bat`
   - `PropertyDesk\pom.xml` (a separate classroom project)
3. Do not open `PropertyDesk` as the main PropertySetU backend. Do not extract only the frontend.

The main application is the combination of `backend/` + `frontend/`. `PropertyDesk/` stays separate.

## 1. Software and versions

- MySQL Server + MySQL Workbench
- Java Development Kit (JDK) 17 or newer
- Node.js 22 LTS (22.12 or newer) + npm
- Spring Tools 4 / Spring Tool Suite (or Eclipse IDE for Enterprise Java)
- VS Code

The backend Maven file explicitly sets `<java.version>17</java.version>`. Your Java 21 installation can run this project while Maven compiles against Java 17; in Eclipse, use a full JDK (not a JRE) and set the project's compiler compliance to 17. For the least confusing setup, install/use JDK 17. Do not downgrade the source target by editing the POM just to match a machine.

## 2. Database — do this first

1. Open MySQL Workbench and connect to your local MySQL Server.
2. Open `database/create-local-database.sql` and execute it. It creates `property_registration_db` only if it does not already exist; it does not drop tables.
3. Confirm the database appears in the SCHEMAS panel.
4. Keep MySQL running.

If your MySQL server uses port 3307 instead of 3306, use that port in `DB_URL` in the backend environment file.

## 3. Configure local environment files once

Create the backend local file:

```powershell
Copy-Item backend\.env.example backend\.env.local
```

Open `backend\.env.local` in VS Code / Notepad and set your own MySQL details. Example:

```dotenv
DB_URL=jdbc:mysql://localhost:3306/property_registration_db?useSSL=false&serverTimezone=Asia/Kolkata
DB_USERNAME=root
DB_PASSWORD=YOUR_LOCAL_MYSQL_PASSWORD
JWT_SECRET=
DDL_AUTO=update
SERVER_PORT=8080
UPLOAD_DIR=../demo/uploads
FRONTEND_ORIGIN=http://localhost:5173
```

Do not paste the example password above literally. Use the password configured on your MySQL Server. Do not share or commit `.env.local`.

For a completely new database with no administrator, optionally set these three values together:

```dotenv
ADMIN_BOOTSTRAP_USERNAME=admin
ADMIN_BOOTSTRAP_EMAIL=admin@example.com
ADMIN_BOOTSTRAP_PASSWORD=YOUR_UNIQUE_PASSWORD_AT_LEAST_12_CHARACTERS
```

This creates an administrator only when that username/email is unused. It never overwrites an existing account. After the first successful startup, remove `ADMIN_BOOTSTRAP_PASSWORD` from `backend\.env.local`. If you already have an admin, leave all three values blank.

Create the frontend file:

```powershell
Copy-Item frontend\.env.example frontend\.env.local
```

Keep `VITE_API_URL=http://localhost:8080` in it. Frontend variables must never contain private server secrets.

## 4. What exactly to open in Spring Tools / Eclipse

**Import only the backend as a Maven project.**

1. Start Spring Tools.
2. Select a workspace, for example `D:\Spring-Workspace\PropertySetU`.
3. Choose **File → Import… → Maven → Existing Maven Projects → Next**.
4. For **Root Directory**, browse to the extracted project's `backend` folder, for example `D:\PropertySetU-Final\backend`.
5. Wait for Maven to scan. The project shown should be `property-registration-backend`.
6. Select it and click **Finish**.
7. Wait for Maven dependency resolution. The first import requires internet access.
8. In Package Explorer, expand:
   `property-registration-backend → src/main/java → com.gaurav.property`.

Important files to open:

| What you need to show/edit | Exact file |
|---|---|
| Main Spring Boot entry point | `backend/src/main/java/com/gaurav/property/PropertyRegistrationBackendApplication.java` |
| API controllers / REST endpoints | `backend/src/main/java/com/gaurav/property/controller/` |
| Business logic / workflow | `backend/src/main/java/com/gaurav/property/service/` |
| Database entities / table mappings | `backend/src/main/java/com/gaurav/property/entity/` |
| JPA repositories / SQL queries | `backend/src/main/java/com/gaurav/property/repository/` |
| Request and response DTOs | `backend/src/main/java/com/gaurav/property/dto/` |
| JWT, CORS and role security | `backend/src/main/java/com/gaurav/property/config/SecurityConfig.java` and `backend/src/main/java/com/gaurav/property/security/` |
| Maven dependencies and Java version | `backend/pom.xml` |
| Database URL, port and upload path | `backend/src/main/resources/application.properties` |

### Check the JDK in Spring Tools

1. Right-click the imported backend project → **Properties → Java Build Path → Libraries**.
2. Confirm it uses a full **JDK**, not a JRE.
3. If your installed JDK is Java 21, choose that JDK as the runtime. The Maven compiler target remains Java 17 as configured in `pom.xml`.
4. Open **Properties → Java Compiler** and confirm compliance is 17 if Eclipse asks for a project compliance level.
5. Right-click project → **Maven → Update Project…** → select the backend project → **Force Update of Snapshots/Releases** only if dependencies appear stale → OK.

### Run Spring Boot from the IDE

1. In Package Explorer open `PropertyRegistrationBackendApplication.java`.
2. Right-click the file → **Run As → Spring Boot App**. If that menu item is unavailable, use **Run As → Java Application**.
3. The Console should show Spring Boot starting on port 8080. Wait for the successful startup message.
4. Open **Run → Run Configurations… → Spring Boot App → your configuration → Arguments** and set **Working Directory → Other** to the backend project directory (for example `${workspace_loc:property-registration-backend}`). This ensures the relative upload path resolves to the repository's `demo/uploads` folder.
5. Keep this Spring Tools window running.

**Environment variables inside Spring Tools:** `backend/.env.local` is read by the supplied PowerShell launcher, not automatically by a normal Eclipse Run Configuration. For an IDE-launched backend, open **Run → Run Configurations… → Spring Boot App → your configuration → Environment** and add the same DB/JWT/upload/admin/payment variables from `backend/.env.local` one by one. Set `JWT_SECRET` to a private random value of at least 32 characters (the IDE does not run the PowerShell helper that generates it). If this is a new database, also enter all three `ADMIN_BOOTSTRAP_*` values for the first run. Do not paste a password into screenshots or source code.

**Simple alternative:** run `START_PROPERTYSETU.bat` and let the PowerShell helper import `backend/.env.local` automatically. Do not do that while the same backend is already running from Spring Tools, or port 8080 will conflict.

## 5. What exactly to open in VS Code

For the best view, open the **repository root** in VS Code, not just one source file:

1. Start VS Code.
2. Choose **File → Open Folder…**.
3. Select `D:\PropertySetU-Final` (the folder containing both `backend` and `frontend`).
4. Trust the folder if VS Code asks.
5. In Explorer expand `frontend → src`.

Important frontend files:

| What you need | Exact file |
|---|---|
| Main React application / screens / user flows | `frontend/src/App.jsx` |
| Main application styling | `frontend/src/App.css` |
| Global styles | `frontend/src/index.css` |
| React entry point | `frontend/src/main.jsx` |
| Frontend dependencies and commands | `frontend/package.json` |
| API URL for local development | `frontend/.env.local` |
| Vite configuration | `frontend/vite.config.js` |

Open a VS Code integrated terminal (**Terminal → New Terminal**) and run:

```powershell
cd frontend
npm ci
npm run lint
npm run build
npm run dev
```

Keep this terminal running. Open `http://localhost:5173` in your browser.

The backend remains in Spring Tools (or its own terminal). Do not run two React dev servers or two Spring Boot servers on the same ports.

## 6. Recommended run arrangement

| Window | Open / run | Keep running? |
|---|---|---|
| MySQL Workbench / Windows service | MySQL Server + `property_registration_db` | Yes |
| Spring Tools | Imported `backend` Maven project; run `PropertyRegistrationBackendApplication` | Yes |
| VS Code | Repository root; VS Code terminal runs `npm run dev` from `frontend` | Yes |
| Browser | `http://localhost:5173` | While testing |

Check backend health at `http://localhost:8080/api/health`. This endpoint confirms the API process responds; it does not by itself prove MySQL, Razorpay or every workflow is healthy.

## 7. One-click alternative (do not combine with the IDE backend)

If you prefer not to start the backend manually in Spring Tools:

1. Close/stop the backend running from Spring Tools.
2. Double-click `START_PROPERTYSETU.bat` in the repository root.
3. It opens separate PowerShell windows for backend and frontend and opens the browser.
4. Keep both console windows open. Use Ctrl+C in each to stop them.

## 8. Safe final verification

After dependencies have downloaded, you can run the provided Windows verification helper from the repository root:

```powershell
.\VERIFY_PROPERTYSETU_WINDOWS.bat
```

It runs frontend `npm ci`, lint and production build, then backend Maven tests/package. The backend test phase may require MySQL and the project's configured environment. Review the exact error if Maven fails; do not call an incomplete run a pass.

For manual commands:

```powershell
# Frontend
cd frontend
npm ci
npm run lint
npm run build

# Backend (in a separate terminal)
cd backend
.\mvnw.cmd test
```

First-time Maven setup downloads the Maven distribution and Spring dependencies, so internet access is required. If Maven reports that Maven Central is unreachable, the backend tests have not run yet.

## 9. What to demonstrate to the committee

Use fictional applicant data and synthetic PDF/JPG/PNG documents only.

1. Applicant registers and logs in.
2. Applicant saves owner, property and location details.
3. Applicant creates an application and uploads the three required demo categories.
4. Applicant submits and completes Razorpay Test Mode payment only if sandbox credentials have been configured.
5. Administrator logs in, creates an Officer account and assigns the paid case.
6. Assigned Officer reviews document versions. Demonstrate both accepting a document and requesting a correction with an explicit reason.
7. Applicant logs back in, sees the reason, uploads a replacement version and resubmits.
8. Officer verifies the latest required versions and records the final decision.
9. Show the audit history and distinguish payment confirmation, academic workflow completion and official government registration.

Do not use real identity documents, live card details, UPI PINs or real OTPs. PropertySetU is an academic workflow demonstration, not a government registration portal or legal title certificate.
