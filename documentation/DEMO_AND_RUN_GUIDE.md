# Which Project Should We Demonstrate Today?

## Short recommendation

For a live walkthrough of the **Spring Boot + MySQL PropertySetu project**, use the PropertySetu source and its current demo website as two different things:

- **Live website:** useful for showing the current deployed interface quickly. It may depend on the separately hosted API and the hosting services being awake/healthy.
- **Local source:** use the full repository to demonstrate the actual React frontend, Spring Boot backend, MySQL data model, API/security code and test results. This is the better route when faculty ask how the project works internally.
- **BlockEstate Nexus:** this is a separate project/repository, not a module of PropertySetu. It uses React + TypeScript, ASP.NET Core/.NET 10, EF Core and SQL Server, and proposes blockchain-oriented integrity references and explainable risk-review prompts. Do not copy it into PropertySetu or delete it just to simplify this demo. Keep it separate unless the team explicitly chooses it as the main project and can demonstrate its additional stack.

## PropertySetu links

- Repository: https://github.com/gauravbalade/propertysetu
- Working branch / review PR: https://github.com/gauravbalade/propertysetu/pull/6
- Deployed frontend listed in the repository: https://frontend-six-amber-63.vercel.app
- Backend health endpoint listed in the repository: https://propertysetu-backend.onrender.com/api/health

A listed URL is not proof that the service is currently healthy. Check it in a browser immediately before the presentation. The latest verified automated checks are frontend ESLint + production build and backend Maven tests/package; they do not prove every live/browser workflow was manually tested.

## Run PropertySetu locally on Windows

### Requirements
Install JDK 17, Node.js LTS/npm and MySQL Server. Use Eclipse/Spring Tool Suite for the Java backend and VS Code or a terminal for React.

### 1. Download the correct source
Open the repository, select **Code → Download ZIP**, extract it, and open the extracted root folder. For the latest changes from this review, use the `propertysetu/windows-verification` branch / PR ZIP, not the default `main` branch ZIP.

### 2. Create the database
In MySQL Workbench run:

```sql
CREATE DATABASE property_registration_db;
```

### 3. Configure and run the backend
Open PowerShell in the extracted repository root. Set your own local MySQL credentials and a private random JWT secret (do not commit these values):

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/property_registration_db?useSSL=false&serverTimezone=Asia/Kolkata"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="YOUR_LOCAL_MYSQL_PASSWORD"
$env:JWT_SECRET="REPLACE_WITH_A_LONG_RANDOM_LOCAL_SECRET"
$env:FRONTEND_ORIGIN="http://localhost:5173"
cd backend
.\mvnw.cmd spring-boot:run
```

If MySQL runs on port 3307, change the port in `DB_URL`. Keep this terminal running. Backend API is normally `http://localhost:8080`; check `http://localhost:8080/api/health`.

### 4. Run the frontend in a second terminal
From the extracted repository root:

```powershell
cd frontend
npm ci
$env:VITE_API_URL="http://localhost:8080"
npm run dev
```

Open the Vite URL printed in the terminal, normally `http://localhost:5173`. The backend, database and frontend must all be running for the complete local workflow. If login or data calls fail, inspect both terminals and the browser developer console; do not silently switch between a local UI and a remote API.

### 5. Presentation demo
Use only synthetic names, fictional addresses and synthetic PDF/image files. Demonstrate registration/login, owner/property/location, application creation, three document categories, submission, Test Mode payment disclosure, then officer review/status history if the officer account is configured. Never show real identity documents, live payment credentials, or an actual OTP. If any step has not been confirmed live immediately before the presentation, present it as a planned workflow rather than claiming it worked.

## Legal and trust boundary

Neither PropertySetu nor BlockEstate Nexus is an official government registration system. Neither can issue legal approval or establish legal title. Risk flags are review prompts, and uploaded documents are not legally verified until reviewed by an authorised person under the applicable official process. Do not send a repository or demo link to an authority as if it were an official filing or legal case. For a real dispute or registration question, contact the appropriate Sub-Registrar / IGR Maharashtra office or a qualified legal professional through official channels.

## What not to delete
- Keep `PropertyDesk/` separate: it is a small classroom-style Java starter and is not part of the main PropertySetu workflow. Do not present it as the primary application.
- Keep BlockEstate Nexus in its own repository: it is a different .NET/SQL Server blockchain-oriented project. It is not required to run PropertySetu.
