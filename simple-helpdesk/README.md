# Simple Helpdesk — Spring Boot CRUD Project

A small beginner-friendly project inspired by a standard layered Spring Boot application. It is **not an e-commerce application**: users create service requests, and the team can edit, track, close, or delete them.

## Features
- Create a service request
- View all requests on a dashboard
- Edit request details
- Change status: Open, In progress, Closed
- Delete a request
- Dashboard counts by status
- Form validation
- H2 file database; no separate MySQL installation is needed for the demo
- Responsive HTML/CSS interface

## Technology
Java 17, Spring Boot 3.3.5, Spring MVC, Thymeleaf, Spring Data JPA, H2, Maven.

## Run on Windows
1. Install JDK 17 or later and Maven.
2. Open a terminal inside the `simple-helpdesk` folder.
3. Run `mvn spring-boot:run`.
4. Open http://localhost:8080.

### Run from Eclipse / Spring Tool Suite
Choose File → Import → Maven → Existing Maven Projects. Select the `simple-helpdesk` folder and finish. Run `HelpdeskApplication.java` as a Java Application.

The database is stored locally in a `data` folder. The application creates the table automatically.

## Project architecture
- `entity/ServiceRequest.java`: Java model mapped to a database table.
- `repository/ServiceRequestRepository.java`: database access using JpaRepository.
- `service/ServiceRequestService.java`: business operations.
- `controller/ServiceRequestController.java`: browser routes and form handling.
- `templates/requests.html`: dashboard.
- `templates/request-form.html`: create/edit form.
- `static/css/style.css`: colours and layout.
- `application.properties`: server and database configuration.

## Viva explanation
The user submits a service request through an HTML form. The Controller receives the form and calls the Service. The Service calls the Repository, which uses Spring Data JPA to store the Entity in the H2 database. The Controller then returns the Thymeleaf page showing the updated records.

CRUD means Create, Read, Update, Delete. This project demonstrates all four operations.

## Change the button colour
Open `src/main/resources/static/css/style.css`. Near the top, change `--primary:#2563eb` to `--primary:#dc2626` to change the main blue theme to red. Refresh the browser. If the old style remains, use Ctrl+F5.

This is a classroom/demo project and does not represent an official institutional service.
