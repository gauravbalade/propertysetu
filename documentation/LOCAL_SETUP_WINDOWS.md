# Run PropertySetu on Windows (Eclipse / Spring Tool Suite / VS Code)

This guide runs the **actual PropertySetu backend and frontend source code** from this repository. The deployed website is only the UI; the Java, database, API and React code are in the `backend/` and `frontend/` folders.

## Before starting

Install:
- JDK 17 or the compatible JDK required by the current `backend/pom.xml`
- Git
- Node.js LTS and npm
- MySQL Server and MySQL Workbench
- Eclipse IDE for Enterprise Java and Web Developers / Spring Tool Suite, or VS Code

## 1. Download the complete source

In GitHub, open the repository and select **Code → Download ZIP**. Extract the ZIP to a simple path, for example `D:\PropertySetu`. Alternatively, clone it:

```powershell
git clone https://github.com/gauravbalade/propertysetu.git
cd propertysetu
```

Do not download only the frontend folder. Both backend and frontend are required for the full local application.

## 2. Prepare MySQL

Open MySQL Workbench and connect to your local MySQL Server. Run:

```sql
CREATE DATABASE property_registration_db;
```

The backend default URL uses port `3307`. If your MySQL server uses the common port `3306`, set `DB_URL` to:

```text
jdbc:mysql://localhost:3306/property_registration_db?useSSL=false&serverTimezone=Asia/Kolkata
```

Set `DB_USERNAME` and `DB_PASSWORD` to your local MySQL credentials. Do not commit real passwords or secrets.

## 3. Configure backend environment

Open `backend/src/main/resources/application.properties` and review the environment variable names. For a local run, configure these in your terminal before starting the backend:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/property_registration_db?useSSL=false&serverTimezone=Asia/Kolkata"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="YOUR_LOCAL_MYSQL_PASSWORD"
$env:JWT_SECRET="REPLACE_WITH_A_LONG_RANDOM_SECRET_FOR_LOCAL_DEVELOPMENT"
$env:FRONTEND_ORIGIN="http://localhost:5173"
```

Replace the example password and secret with your own local values. Never publish them. If your local MySQL server uses port 3307, keep that port instead.

## 4. Start Spring Boot backend

From PowerShell:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Wait for Spring Boot to finish starting. The API is normally at `http://localhost:8080`; check `/api/health` for the health response.

### Import into Eclipse / Spring Tool Suite

1. Open Eclipse / Spring Tool Suite.
2. Choose **File → Import → Maven → Existing Maven Projects**.
3. Select the repository's `backend` folder.
4. Finish the import and wait for Maven dependencies to download.
5. Open `PropertyRegistrationBackendApplication.java` and run it as a Java Application (or Spring Boot App if available).

### Open in VS Code

1. Open the full repository folder in VS Code.
2. Install the Extension Pack for Java and Spring Boot Extension Pack.
3. Open the `backend` folder or the Java main class.
4. Run `PropertyRegistrationBackendApplication.java`, or use the PowerShell command above.

## 5. Start React frontend

Open a **second** terminal at the repository root:

```powershell
cd frontend
npm ci
npm run dev
```

Open the local Vite URL printed in the terminal, usually `http://localhost:5173`. If necessary, set `VITE_API_URL=http://localhost:8080` in `frontend/.env.local`. Do not commit private keys or secrets in frontend environment files.

## 6. Know where the code lives

| If the examiner asks to change... | Open this source |
|---|---|
| Button colours, spacing, fonts | `frontend/src/App.css` and `frontend/src/index.css` |
| Page content, forms, screen flow | `frontend/src/App.jsx` |
| REST endpoint / URL | `backend/src/main/java/com/gaurav/property/controller/` |
| Business rule / workflow | `backend/src/main/java/com/gaurav/property/service/` |
| Database table / fields / relationships | `backend/src/main/java/com/gaurav/property/entity/` |
| Database query | `backend/src/main/java/com/gaurav/property/repository/` |
| Request validation / API input | `backend/src/main/java/com/gaurav/property/dto/` |
| Login, JWT, permissions | `backend/src/main/java/com/gaurav/property/config/` and `security/` |
| DB URL, upload path, port | `backend/src/main/resources/application.properties` |

## 7. Explain the layered architecture in viva

**Controller → Service → Repository → Entity → Database**

- Controller receives the HTTP request and returns a response.
- DTO carries validated input/output data.
- Service contains the business rules and authorization checks.
- Repository uses Spring Data JPA to query or save records.
- Entity maps Java objects to database tables.
- Security configuration validates the bearer token and role.
- React calls the backend API and displays the result.

The project is a classroom demonstration, not an official government portal. Use synthetic data and Razorpay Test Mode only. Do not enter real identity documents or live payment credentials.
