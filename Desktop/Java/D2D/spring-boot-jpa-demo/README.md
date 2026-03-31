# Spring Boot Demo

This is a generated Spring Boot application with the following stack:
- Maven
- Spring Data JPA
- H2 Database (In-memory SQL)
- Spring Web (REST)
- Lombok

## Prerequisites
- Java 17+
- Maven 3.6+

## How to Run
1. Open this folder in your IDE (IntelliJ IDEA, Eclipse, VS Code).
2. If using command line:
   ```bash
   mvn spring-boot:run
   ```

## API Endpoints
- `GET /users`: List all users
- `POST /users`: Create a new user

## H2 Console
Access the database console at `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- User: `sa`
- Password: `password`
