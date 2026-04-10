# Human Resource Management System (HRMS)

A full-featured HR management system built with Java 17, Spring Boot 3.x, Thymeleaf, Spring Security, H2 database, and Bootstrap 5.

## Features

- Employee management (create, read, update, delete)
- Leave request submission and approval workflow
- Role-based access control (Admin and Employee)
- Leave balance tracking (vacation and sick days)
- Admin and employee dashboards

## Technology Stack

- Java 17
- Spring Boot 3.1.5
- Maven
- Spring Security with BCrypt
- Spring Data JPA + H2 Database (file-based)
- Thymeleaf templates
- Bootstrap 5

## Setup Instructions

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Running the Application

1. Navigate to the `hrms` directory:
   ```bash
   cd hrms
   ```

2. Run the application:
   ```bash
   mvn spring-boot:run
   ```

3. Open your browser and go to:
   ```
   http://localhost:8080
   ```

### Running Tests

```bash
mvn test
```

To run a specific test class:
```bash
mvn test -Dtest=EmployeePathTest
```

## Login Credentials

| Role     | Email                   | Password     |
|----------|-------------------------|--------------|
| Admin    | admin@hrms.com          | admin123     |
| Employee | john.smith@hrms.com     | employee123  |
| Employee | jane.doe@hrms.com       | employee123  |

## Project Structure

```
src/main/java/com/hrms/
├── controller/       - HTTP request handlers
├── model/            - JPA entity classes
├── repository/       - Spring Data JPA interfaces
├── service/          - Business logic
├── config/           - Security configuration
└── HrmsApplication.java

src/main/resources/
├── templates/        - Thymeleaf HTML templates
├── application.properties
└── data.sql          - Sample data

src/test/java/com/hrms/
├── path/             - Path testing
├── dataflow/         - Data flow testing
├── integration/      - Integration testing
├── boundary/         - Boundary value testing
├── equivalence/      - Equivalence class testing
├── decisiontable/    - Decision table testing
├── statetransition/  - State transition testing
└── usecase/          - Use case testing
```

## Database Console

The H2 console is available at `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:./data/hrmsdb`
- Username: `sa`
- Password: (empty)

## Admin Features

- View all employees in a searchable, filterable table
- Add, edit, and delete employees
- View and approve/reject all leave requests
- Dashboard showing total employees, pending leaves, and monthly stats

## Employee Features

- View personal dashboard with leave balance
- Submit leave requests (VACATION, SICK, PERSONAL, EMERGENCY)
- View own leave history and status
- View personal profile
