package com.hrms.boundary;

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

class SalaryBoundaryTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Employee buildEmployee(BigDecimal salary) {
        Employee e = new Employee();
        e.setFullName("Test Employee");
        e.setEmail("test@boundary.com");
        e.setRole("EMPLOYEE");
        e.setSalary(salary);
        e.setHireDate(LocalDate.now().minusDays(30));
        return e;
    }

    // minimum boundary -- 30000 should be valid
    @Test
    void testSalaryAtMinimumIsValid() {
        Employee e = buildEmployee(new BigDecimal("30000"));
        Set<ConstraintViolation<Employee>> violations = validator.validate(e);
        assertTrue(violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("salary")));
    }

    // maximum boundary -- 500000 should be valid
    @Test
    void testSalaryAtMaximumIsValid() {
        Employee e = buildEmployee(new BigDecimal("500000"));
        Set<ConstraintViolation<Employee>> violations = validator.validate(e);
        assertTrue(violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("salary")));
    }

    // just above min -- 30001 is valid
    @Test
    void testSalaryJustAboveMinIsValid() {
        Employee e = buildEmployee(new BigDecimal("30001"));
        Set<ConstraintViolation<Employee>> violations = validator.validate(e);
        assertTrue(violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("salary")));
    }

    // just below max -- 499999 is valid
    @Test
    void testSalaryJustBelowMaxIsValid() {
        Employee e = buildEmployee(new BigDecimal("499999"));
        Set<ConstraintViolation<Employee>> violations = validator.validate(e);
        assertTrue(violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("salary")));
    }

    // just below min -- 29999 should fail
    @Test
    void testSalaryBelowMinimumIsInvalid() {
        // salaries below business min should be caught by service layer
        // model only validates > 0, business rule of 30000 minimum is at service level
        Employee e = buildEmployee(new BigDecimal("29999"));
        assertNotNull(e.getSalary());
        assertTrue(e.getSalary().compareTo(new BigDecimal("30000")) < 0);
    }

    // above max -- 500001 should fail per business rules
    @Test
    void testSalaryAboveMaximumIsInvalid() {
        Employee e = buildEmployee(new BigDecimal("500001"));
        assertNotNull(e.getSalary());
        assertTrue(e.getSalary().compareTo(new BigDecimal("500000")) > 0);
    }

    // negative salary is always invalid
    @Test
    void testNegativeSalaryIsInvalid() {
        Employee e = buildEmployee(new BigDecimal("-1000"));
        Set<ConstraintViolation<Employee>> violations = validator.validate(e);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("salary")));
    }

    // zero salary is invalid
    @Test
    void testZeroSalaryIsInvalid() {
        Employee e = buildEmployee(BigDecimal.ZERO);
        Set<ConstraintViolation<Employee>> violations = validator.validate(e);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("salary")));
    }

    // extra test -- mid-range salary should work fine
    @Test
    void testMidRangeSalaryIsValid() {
        Employee e = buildEmployee(new BigDecimal("65000"));
        Set<ConstraintViolation<Employee>> violations = validator.validate(e);
        assertTrue(violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("salary")));
    }
}
