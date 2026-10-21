# PropertyDesk — Property Registration & Visit Management

PropertyDesk is a small academic property registration and record-management project built with Java, Spring Boot and MySQL. It manages owner profiles, property records, addresses and visit requests. It is **not an e-commerce website** and is **not an official government registration portal**.

## Features
- Browser dashboard served directly by Spring Boot at `http://localhost:8081/`
- Add and view owners with basic validation
- Register properties linked to an existing owner
- Add one address/location record per property
- Create property visit requests and update their status
- Change property status between AVAILABLE, UNDER_REVIEW and REGISTERED
- Consistent JSON response wrapper and readable validation/database-conflict messages
- Responsive layout for laptop and mobile browser screens

## Technology
- Java 17, Spring Boot 3.4.5, Maven
- Spring Web, Spring Data JPA, Bean Validation, MySQL
- Lombok, HTML, CSS and JavaScript
- Postman can be used for API checks

## Package structure
`com.gaurav.propertydesk`
- `controllers`: REST endpoints using `@RestController`, `@RequestMapping` and `@Autowired`
- `services`: business logic and database workflow
- `repositories`: Spring Data interfaces extending `JpaRepository`
- `entities`: JPA entities mapped to database tables
- `enums`: controlled visit status values
- `response_wrapper`: shared response format and API exception handling
- `src/main/resources/static`: browser dashboard files (`index.html`, `styles.css`, `app.js`)

The backend flow is **Browser/Postman → Controller → Service → Repository → MySQL**. The response returns to the caller through the controller. Spring Data JPA creates repository implementations automatically. Field injection with `@Autowired` is used intentionally to match the classroom Shopify example.

## Database relationships
- One owner can be linked to multiple property records (`Owner 1 — many Properties`).
- Each property has at most one location record (`Property 1 — 1 Location`).
- One property can have multiple visit requests (`Property 1 — many Visit Requests`).

## Run on Windows
1. Install JDK 17 and MySQL Server.
2. In MySQL Workbench, run:
   ```sql
   CREATE DATABASE propertydesk_db;
   ```
3. Open `src/main/resources/application.properties` and set your local MySQL username and password. The defaults are username `root`, blank password, and port `3306`. Never commit your real database password to a public repository.
4. Open the `PropertyDesk` folder in Eclipse/Spring Tool Suite as **Existing Maven Project**, or open it in VS Code with Java and Spring extensions. If Eclipse reports missing Lombok-generated methods, install Lombok for Eclipse and enable annotation processing, then refresh the Maven project.
5. Run `PropertyDeskApplication.java`, or open a terminal in the `PropertyDesk` folder and run:
   ```powershell
   .\\mvnw.cmd spring-boot:run
   ```
6. Open **http://localhost:8081/** for the dashboard. The REST API base URL is **http://localhost:8081/api/v1**. Hibernate creates/updates tables for this learning project.

## API examples
Create an owner — `POST /api/v1/owners`
```json
{"fullName":"Asha Patil","email":"asha@example.com","phone":"9876543210"}
```

Create a property — `POST /api/v1/properties`
```json
{"title":"Family Apartment","propertyType":"Apartment","areaSqFt":850,"status":"AVAILABLE","owner":{"id":1}}
```

Add a location — `POST /api/v1/locations`
```json
{"property":{"id":1},"address":"Example Road","city":"Navi Mumbai","district":"Thane","state":"Maharashtra","pincode":"400708"}
```

Request a visit — `POST /api/v1/visits`
```json
{"property":{"id":1},"visitDate":"2030-12-20","note":"Evening visit"}
```

Other endpoints:
- `GET /api/v1/owners`, `GET /api/v1/owners/{ownerId}`
- `GET /api/v1/properties`, `GET /api/v1/properties/{propertyId}`, `GET /api/v1/properties/owner/{ownerId}`
- `PATCH /api/v1/properties/{propertyId}/status?status=REGISTERED`
- `DELETE /api/v1/properties/{propertyId}` (will conflict if dependent records still reference the property)
- `GET /api/v1/locations`, `GET /api/v1/locations/property/{propertyId}`
- `GET /api/v1/visits`, `GET /api/v1/visits/property/{propertyId}`
- `PATCH /api/v1/visits/{visitId}/status?status=APPROVED`

## Build and test
From the `PropertyDesk` directory:
```powershell
.\\mvnw.cmd test
.\\mvnw.cmd package
```
GitHub Actions builds the project with Java 17, runs the unit tests, and packages the application. Tests cover the shared response wrapper, invalid property status handling, and protection against deleting properties that still have linked location or visit records. These automated checks do not replace a full live MySQL/browser acceptance test.

## Important
- Use sample/demo information only; do not enter real identity documents, financial details or sensitive personal data.
- This is an academic demonstration. Validation and database constraints are starter-level, not a production security system.
- A successful cloud build verifies compilation and unit tests, not your laptop's local MySQL configuration. Follow the setup steps and use demo data before presenting the project.
