# HRMS Project Report

## 1. Introduction

This report documents the design and implementation of a Human Resource Management System (HRMS) built as part of a software engineering project. The system is intended to manage employee records and leave requests within an organization, providing different views and capabilities to administrators versus regular employees.

The project was developed using Java 17, Spring Boot 3, Maven, and a suite of testing techniques to verify the correctness and robustness of the application.

---

## 2. Design Problem

### 2.1 Problem Definition

Organizations that manage employees often struggle with manual processes for handling leave applications, employee records, and role-based access. Without a centralized system, HR teams deal with scattered spreadsheets, email-based approvals, and no historical tracking of leave balances.

The problem is to design and implement a web-based HRMS that allows:
- Admins to manage employee records and approve or reject leave requests
- Employees to view their own information, submit leave requests, and track balances
- The system to enforce business rules around leave eligibility and approvals

### 2.2 Design Requirements

**Functions:**
- Full CRUD operations on employee records
- Leave request submission with type selection (VACATION, SICK, PERSONAL, EMERGENCY)
- Leave balance tracking per employee per year
- Manager approval workflow with comment functionality
- Secure login with role-based access (ADMIN and EMPLOYEE)
- Dashboard views tailored per role

**Objectives:**
- Provide a clean, usable interface using Bootstrap 5
- Keep the system simple enough to run locally without external dependencies
- Ensure test coverage across multiple testing techniques
- Enforce data integrity (unique emails, positive salary, valid dates)

**Constraints:**
- Must use Java 17 and Spring Boot 3.x
- Must use H2 as the database (file-based for persistence)
- Must use Thymeleaf for server-side rendering
- Must use Maven for build management
- All tests must use JUnit 5

---

## 3. Solution

### 3.1 Solution 1 — Monolithic REST API

The first approach considered was a pure REST API with a separate JavaScript frontend. This would provide a more modern client-side experience, but introduces complexity in managing CORS, JWT tokens, and a separate build system for the frontend. Given the project constraints (Thymeleaf is required), this approach was ruled out.

### 3.2 Solution 2 — Plain Servlet-based MVC

An older approach using raw Java servlets and JSP templates was considered. While it is technically feasible, Spring Boot and Thymeleaf provide far more productivity and a better developer experience. Managing session, security, and form binding manually would have been unnecessarily complex. This approach was rejected.

### 3.3 Final Solution — Spring Boot + Thymeleaf MVC

The final solution uses a classic server-side MVC pattern with Spring Boot as the application framework. Thymeleaf renders HTML templates on the server, Spring Security handles authentication and authorization, and Spring Data JPA handles database operations through repositories.

The architecture follows a layered approach:
- **Controller layer**: Handles HTTP requests and delegates to services
- **Service layer**: Contains business logic, validation, and transaction management
- **Repository layer**: Provides database access through JPA interfaces
- **Model layer**: Defines the data entities mapped to H2 tables

This approach was chosen because it maps cleanly to the required technology stack, produces testable code, and can be run with a single Maven command.

---

## 4. Team Work

This project was completed as an individual assignment. All code, tests, templates, and documentation were written by a single developer working through each layer of the application systematically.

The development order followed:
1. Model entities (Employee, LeaveRequest, LeaveBalance)
2. Repository interfaces
3. Service layer with business logic
4. Security configuration
5. Controllers
6. Thymeleaf templates
7. Test classes for all 8 testing techniques
8. Documentation

---

## 5. Project Management

Development was tracked informally. Major milestones:

| Milestone | Status |
|-----------|--------|
| Project setup and pom.xml | Done |
| Entity and repository layer | Done |
| Service layer | Done |
| Security config | Done |
| Controllers | Done |
| HTML templates | Done |
| All 8 test classes | Done |
| Sample data seeding | Done |
| Documentation | Done |

The H2 file-based database stores data between restarts, making it easy to test the application without resetting state every time. The `data.sql` file seeds initial users using BCrypt-encoded passwords so login works immediately on first run.
