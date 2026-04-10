package com.hrms.usecase;

import com.hrms.model.Employee;
import com.hrms.model.LeaveBalance;
import com.hrms.model.LeaveRequest;
import com.hrms.service.EmployeeService;
import com.hrms.service.LeaveService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class HrmsUseCaseTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private LeaveService leaveService;

    // Use Case 1: Employee applies for vacation leave
    @Test
    @Transactional
    void useCase1_EmployeeAppliesForVacationLeave() {
        // setup: create an employee
        Employee emp = new Employee();
        emp.setFullName("Alice Johnson");
        emp.setEmail("alice@hrms.com");
        emp.setPassword("pass123");
        emp.setRole("EMPLOYEE");
        emp.setDepartment("Engineering");
        emp.setSalary(new BigDecimal("60000"));
        emp.setHireDate(LocalDate.of(2022, 6, 1));
        emp = employeeService.saveEmployee(emp);

        // step 1: check leave balance first
        int year = LocalDate.now().getYear();
        Optional<LeaveBalance> balance = leaveService.getLeaveBalance(emp.getId(), year);
        assertTrue(balance.isPresent());
        assertTrue(balance.get().getVacationDaysRemaining() > 0);

        // step 2: submit the vacation request
        LeaveRequest request = new LeaveRequest();
        request.setEmployee(emp);
        request.setLeaveType("VACATION");
        request.setStartDate(LocalDate.now().plusDays(14));
        request.setEndDate(LocalDate.now().plusDays(18));
        request.setReason("Annual family vacation");

        LeaveRequest submitted = leaveService.submitLeaveRequest(request);

        // verify the request was created
        assertNotNull(submitted.getId());
        assertEquals("PENDING", submitted.getStatus());
        assertEquals("VACATION", submitted.getLeaveType());
        assertEquals(emp.getId(), submitted.getEmployee().getId());

        // verify it shows up in employee's request list
        List<LeaveRequest> myRequests = leaveService.getLeaveRequestsByEmployee(emp.getId());
        assertFalse(myRequests.isEmpty());
    }

    // Use Case 2: Admin adds a new employee to the system
    @Test
    void useCase2_AdminAddsNewEmployee() {
        long initialCount = employeeService.getTotalCount();

        // admin fills in the new employee form
        Employee newEmp = new Employee();
        newEmp.setFullName("Bob Williams");
        newEmp.setEmail("bob.williams@hrms.com");
        newEmp.setPassword("temppass123");
        newEmp.setRole("EMPLOYEE");
        newEmp.setDepartment("Finance");
        newEmp.setPosition("Financial Analyst");
        newEmp.setSalary(new BigDecimal("58000"));
        newEmp.setPhone("555-0200");
        newEmp.setHireDate(LocalDate.now());

        Employee saved = employeeService.saveEmployee(newEmp);

        // verify employee was saved
        assertNotNull(saved.getId());
        assertNotNull(saved.getEmployeeId());
        assertTrue(saved.getEmployeeId().startsWith("EMP"));

        // verify count increased
        long newCount = employeeService.getTotalCount();
        assertEquals(initialCount + 1, newCount);

        // verify leave balance was auto-created
        int year = LocalDate.now().getYear();
        Optional<LeaveBalance> balance = leaveService.getLeaveBalance(saved.getId(), year);
        assertTrue(balance.isPresent());
        assertEquals(15, balance.get().getVacationDaysRemaining());

        // verify can retrieve by email
        Optional<Employee> retrieved = employeeService.getEmployeeByEmail("bob.williams@hrms.com");
        assertTrue(retrieved.isPresent());
        assertEquals("Bob Williams", retrieved.get().getFullName());
    }

    // Use Case 3: Manager approves a pending leave request
    @Test
    @Transactional
    void useCase3_ManagerApprovesPendingLeaveRequest() {
        // setup employee
        Employee emp = new Employee();
        emp.setFullName("Carol Davis");
        emp.setEmail("carol@hrms.com");
        emp.setPassword("pass");
        emp.setRole("EMPLOYEE");
        emp.setDepartment("Marketing");
        emp.setSalary(new BigDecimal("52000"));
        emp.setHireDate(LocalDate.of(2021, 3, 15));
        emp = employeeService.saveEmployee(emp);

        // employee submits request
        LeaveRequest request = new LeaveRequest();
        request.setEmployee(emp);
        request.setLeaveType("PERSONAL");
        request.setStartDate(LocalDate.now().plusDays(7));
        request.setEndDate(LocalDate.now().plusDays(7));
        request.setReason("Personal appointment");
        LeaveRequest pending = leaveService.submitLeaveRequest(request);

        // manager reviews and sees the request in pending list
        List<LeaveRequest> pendingRequests = leaveService.getPendingRequests();
        assertTrue(pendingRequests.stream().anyMatch(r -> r.getId().equals(pending.getId())));

        // manager approves the request
        String comment = "Approved. Good luck with your appointment.";
        LeaveRequest approved = leaveService.approveLeave(pending.getId(), comment);

        // verify the approval
        assertEquals("APPROVED", approved.getStatus());
        assertEquals(comment, approved.getAdminComment());

        // verify it no longer shows as pending
        List<LeaveRequest> stillPending = leaveService.getPendingRequests();
        assertTrue(stillPending.stream().noneMatch(r -> r.getId().equals(approved.getId())));
    }

    // Use Case 4: Employee checks leave balance before applying
    @Test
    void useCase4_EmployeeChecksLeaveBalanceBeforeApplying() {
        // setup employee
        Employee emp = new Employee();
        emp.setFullName("Dave Evans");
        emp.setEmail("dave@hrms.com");
        emp.setPassword("pass");
        emp.setRole("EMPLOYEE");
        emp.setDepartment("Operations");
        emp.setSalary(new BigDecimal("48000"));
        emp.setHireDate(LocalDate.of(2020, 9, 1));
        emp = employeeService.saveEmployee(emp);

        // employee views their balance
        int year = LocalDate.now().getYear();
        Optional<LeaveBalance> balance = leaveService.getLeaveBalance(emp.getId(), year);

        assertTrue(balance.isPresent());
        assertNotNull(balance.get().getVacationDaysRemaining());
        assertNotNull(balance.get().getSickDaysRemaining());

        // employee checks if they have enough vacation days for a 5-day trip
        int requiredDays = 5;
        boolean canTakeVacation = balance.get().getVacationDaysRemaining() >= requiredDays;
        assertTrue(canTakeVacation);

        // verify the balance values are realistic
        assertTrue(balance.get().getVacationDaysRemaining() >= 0);
        assertTrue(balance.get().getSickDaysRemaining() >= 0);
        assertTrue(balance.get().getVacationDaysRemaining() <= 15);
        assertTrue(balance.get().getSickDaysRemaining() <= 10);
    }
}
