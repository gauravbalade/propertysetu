# START HERE — PropertySetU Final Package

Use this file as the entry point after extracting the ZIP.

## Which program opens what?

- **Spring Tools / Eclipse:** import only the `backend/` folder as an Existing Maven Project. Run `backend/src/main/java/com/gaurav/property/PropertyRegistrationBackendApplication.java`.
- **VS Code:** open the full extracted repository root (the folder containing `backend/`, `frontend/`, `PropertyDesk/` and this file). In its terminal, run the frontend commands from `frontend/`.
- **MySQL Workbench:** execute `database/create-local-database.sql` once, then keep MySQL Server running.
- **Browser:** open `http://localhost:5173`; backend health is `http://localhost:8080/api/health`.

## Read these in order

1. [Windows IDE Workflow](documentation/IDE_WORKFLOW_WINDOWS.md) — exact Spring Tools and VS Code steps.
2. [Local Windows Setup](documentation/LOCAL_SETUP_WINDOWS.md) — database, .env.local files, launcher and troubleshooting.
3. [Advanced Product & Engineering Research](documentation/research/ADVANCED_PRODUCT_AND_ENGINEERING_SPEC.md) — full research specification.
4. [Research Implementation Status](documentation/research/IMPLEMENTATION_STATUS_AFTER_FINAL_PASS.md) — implemented vs partial vs not tested.
5. [Final Handoff](documentation/FINAL_HANDOFF.md) — release notes and verification boundary.

## First run checklist

- [ ] Extract into a fresh folder, for example `D:\PropertySetU-Final`.
- [ ] Start MySQL and create `property_registration_db`.
- [ ] Copy `backend/.env.example` to `backend/.env.local` and set local MySQL username/password.
- [ ] If this is a fresh database with no administrator, optionally set all three `ADMIN_BOOTSTRAP_*` values in `backend/.env.local`. Remove the bootstrap password after the first startup.
- [ ] Import `backend/` in Spring Tools and run the main Spring Boot class (or use the one-click launcher, but not both at once).
- [ ] Open repository root in VS Code; run `npm ci`, `npm run lint`, `npm run build`, then `npm run dev` inside `frontend/`.
- [ ] Use synthetic names and sample files only; use Razorpay Test Mode only.

`PropertyDesk/` is a separate Java classroom project. Do not import it as the main PropertySetU backend. PropertySetU is an academic workflow demonstration, not an official registration portal.
