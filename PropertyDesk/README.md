# PropertyDesk — Property Management System

A small academic Spring Boot + MySQL REST API project using the layered Java structure demonstrated in the Shopify classroom project. It manages owners, property records, property locations and visit requests. It is not an e-commerce website and is not an official government property-registration service.

## Technology
- Java 17, Spring Boot 3.4.5, Maven
- Spring Web, Spring Data JPA, Bean Validation, MySQL
- Lombok for getters/setters; Postman for API checks

## Package structure
`com.gaurav.propertydesk`
- `controllers`: REST endpoints; `@RestController`, `@RequestMapping`, `@Autowired`
- `services`: business rules and database workflow
- `repositories`: interfaces extending `JpaRepository`
- `entities`: JPA-mapped database tables
- `enums`: controlled visit status values
- `response_wrapper`: consistent API response format

The flow is **Postman → Controller → Service → Repository → MySQL**, then the response returns through the controller. Spring creates repository implementations automatically.

## Run on Windows
1. Install JDK 17 and MySQL Server.
2. Create the database: `CREATE DATABASE propertydesk_db;`
3. Open `src/main/resources/application.properties`. Set your local MySQL username and password. Defaults are username `root`, blank password, and port `3306`.
4. Open the `PropertyDesk` folder in Eclipse/Spring Tool Suite as **Existing Maven Project**, or open it in VS Code with Java and Spring extensions.
5. Run `PropertyDeskApplication.java`, or run `mvn spring-boot:run` from this folder.
6. API base URL: `http://localhost:8081/api/v1`. Hibernate creates/updates tables for this learning project.

## API examples
### Create an owner — POST `/owners`
```json
{"fullName":"Asha Patil","email":"asha@example.com","phone":"9876543210"}
```

### Create a property — POST `/properties`
```json
{"title":"Family Apartment","propertyType":"Apartment","areaSqFt":850,"status":"AVAILABLE","owner":{"id":1}}
```

### Add location — POST `/locations`
```json
{"property":{"id":1},"address":"Example Road","city":"Navi Mumbai","district":"Thane","state":"Maharashtra","pincode":"400708"}
```

### Request a visit — POST `/visits`
```json
{"property":{"id":1},"visitDate":"2030-12-20","note":"Evening visit"}
```

Other endpoints:
- GET `/owners`, GET `/owners/{ownerId}`
- GET `/properties`, GET `/properties/{propertyId}`, GET `/properties/owner/{ownerId}`
- PATCH `/properties/{propertyId}/status?status=REGISTERED`
- DELETE `/properties/{propertyId}`
- GET `/locations`, GET `/locations/property/{propertyId}`
- GET `/visits`, GET `/visits/property/{propertyId}`
- PATCH `/visits/{visitId}/status?status=APPROVED`

## Important
- Use demo data only; do not enter real identity documents or payment details.
- Validation and database constraints are starter-level, not a production security system.
- Understand the class flow and test each endpoint before presenting it. A successful build is not the same as a successful live MySQL run.
