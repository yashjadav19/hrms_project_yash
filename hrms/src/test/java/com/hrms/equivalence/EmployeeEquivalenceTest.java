package com.hrms.equivalence;

import com.hrms.model.Employee;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeEquivalenceTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Employee buildEmployee(String name, String email, BigDecimal salary) {
        Employee e = new Employee();
        e.setFullName(name);
        e.setEmail(email);
        e.setRole("EMPLOYEE");
        e.setSalary(salary);
        e.setHireDate(LocalDate.now().minusMonths(3));
        return e;
    }

    // valid class 1: full-time employee with salary >= 40000
    @Test
    void testFullTimeEmployeeIsValid() {
        Employee e = buildEmployee("John Full", "johnfull@test.com", new BigDecimal("55000"));
        Set<ConstraintViolation<Employee>> violations = validator.validate(e);
        // only constraint violations from bean validation, not business rules
        assertTrue(violations.isEmpty());
    }

    // valid class 2: part-time employee with salary 20000-39999
    @Test
    void testPartTimeEmployeeIsValid() {
        Employee e = buildEmployee("Jane Part", "janepart@test.com", new BigDecimal("30000"));
        Set<ConstraintViolation<Employee>> violations = validator.validate(e);
        assertTrue(violations.isEmpty());
    }

    // valid class 3: contract employee with salary >= 50000
    @Test
    void testContractEmployeeIsValid() {
        Employee e = buildEmployee("Jack Contract", "jackcontract@test.com", new BigDecimal("75000"));
        Set<ConstraintViolation<Employee>> violations = validator.validate(e);
        assertTrue(violations.isEmpty());
    }

    // invalid class 1: negative salary
    @Test
    void testNegativeSalaryIsInvalid() {
        Employee e = buildEmployee("Bad Salary", "badsalary@test.com", new BigDecimal("-500"));
        Set<ConstraintViolation<Employee>> violations = validator.validate(e);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("salary")));
    }

    // invalid class 2: empty name
    @Test
    void testEmptyNameIsInvalid() {
        Employee e = buildEmployee("", "emptyname@test.com", new BigDecimal("50000"));
        Set<ConstraintViolation<Employee>> violations = validator.validate(e);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("fullName")));
    }

    // invalid class 3: name with only spaces
    @Test
    void testBlankNameIsInvalid() {
        Employee e = buildEmployee("   ", "blankname@test.com", new BigDecimal("50000"));
        Set<ConstraintViolation<Employee>> violations = validator.validate(e);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("fullName")));
    }

    // invalid class 4: email missing @ symbol
    @Test
    void testEmailMissingAtSymbolIsInvalid() {
        Employee e = buildEmployee("No At Email", "invalidemail.com", new BigDecimal("50000"));
        Set<ConstraintViolation<Employee>> violations = validator.validate(e);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    // invalid class 5: email missing domain part
    @Test
    void testEmailMissingDomainIsInvalid() {
        Employee e = buildEmployee("No Domain", "user@", new BigDecimal("50000"));
        Set<ConstraintViolation<Employee>> violations = validator.validate(e);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    // invalid class 6: null/blank email
    @Test
    void testNullEmailIsInvalid() {
        Employee e = new Employee();
        e.setFullName("Null Email");
        e.setEmail("");
        e.setSalary(new BigDecimal("50000"));
        e.setRole("EMPLOYEE");
        e.setHireDate(LocalDate.now());

        Set<ConstraintViolation<Employee>> violations = validator.validate(e);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    // valid salary equivalence -- exactly on class boundary
    @Test
    void testSalaryExactlyAtBoundaryIsValid() {
        Employee e = buildEmployee("Boundary Sal", "boundarysal@test.com", new BigDecimal("40000"));
        Set<ConstraintViolation<Employee>> violations = validator.validate(e);
        assertTrue(violations.isEmpty());
    }
}
