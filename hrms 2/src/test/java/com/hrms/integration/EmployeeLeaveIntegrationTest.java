package com.hrms.integration;

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
class EmployeeLeaveIntegrationTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private LeaveService leaveService;

    private Employee createEmployee(String name, String email) {
        Employee e = new Employee();
        e.setFullName(name);
        e.setEmail(email);
        e.setPassword("inttest");
        e.setRole("EMPLOYEE");
        e.setDepartment("Integration");
        e.setSalary(new BigDecimal("60000"));
        e.setHireDate(LocalDate.now().minusMonths(6));
        return employeeService.saveEmployee(e);
    }

    @Test
    @Transactional
    void testEmployeeAndLeaveServiceIntegration() {
        // create employee, then use leave service to check balance
        Employee emp = createEmployee("Integration User", "integration@hrms.com");
        assertNotNull(emp.getId());

        int year = LocalDate.now().getYear();
        Optional<LeaveBalance> balance = leaveService.getLeaveBalance(emp.getId(), year);
        assertTrue(balance.isPresent());
        assertEquals(15, balance.get().getVacationDaysRemaining());
    }

    @Test
    @Transactional
    void testLeaveSubmissionUpdatesEmployeeContext() {
        Employee emp = createEmployee("Leave Int User", "leaveint@hrms.com");

        LeaveRequest lr = new LeaveRequest();
        lr.setEmployee(emp);
        lr.setLeaveType("PERSONAL");
        lr.setStartDate(LocalDate.now().plusDays(2));
        lr.setEndDate(LocalDate.now().plusDays(2));
        lr.setReason("Personal day");

        leaveService.submitLeaveRequest(lr);

        List<LeaveRequest> requests = leaveService.getLeaveRequestsByEmployee(emp.getId());
        assertFalse(requests.isEmpty());
        assertEquals("PENDING", requests.get(0).getStatus());
        assertEquals(emp.getId(), requests.get(0).getEmployee().getId());
    }

    @Test
    @Transactional
    void testApproveUpdatesBothLeaveAndBalance() {
        Employee emp = createEmployee("Approve Int", "approveint@hrms.com");

        int year = LocalDate.now().getYear();
        Optional<LeaveBalance> balanceBefore = leaveService.getLeaveBalance(emp.getId(), year);
        int initVacation = balanceBefore.get().getVacationDaysRemaining();

        LeaveRequest lr = new LeaveRequest();
        lr.setEmployee(emp);
        lr.setLeaveType("VACATION");
        lr.setStartDate(LocalDate.now().plusDays(1));
        lr.setEndDate(LocalDate.now().plusDays(3));
        lr.setReason("Integration approval test");

        LeaveRequest submitted = leaveService.submitLeaveRequest(lr);
        assertEquals("PENDING", submitted.getStatus());

        LeaveRequest approved = leaveService.approveLeave(submitted.getId(), "OK");
        assertEquals("APPROVED", approved.getStatus());

        Optional<LeaveBalance> balanceAfter = leaveService.getLeaveBalance(emp.getId(), year);
        assertTrue(balanceAfter.get().getVacationDaysRemaining() < initVacation);
    }

    @Test
    @Transactional
    void testRejectDoesNotDeductBalance() {
        Employee emp = createEmployee("Reject Int", "rejectint@hrms.com");

        int year = LocalDate.now().getYear();
        Optional<LeaveBalance> balanceBefore = leaveService.getLeaveBalance(emp.getId(), year);
        int initVacation = balanceBefore.get().getVacationDaysRemaining();

        LeaveRequest lr = new LeaveRequest();
        lr.setEmployee(emp);
        lr.setLeaveType("VACATION");
        lr.setStartDate(LocalDate.now().plusDays(5));
        lr.setEndDate(LocalDate.now().plusDays(6));
        lr.setReason("Should be rejected");

        LeaveRequest submitted = leaveService.submitLeaveRequest(lr);
        leaveService.rejectLeave(submitted.getId(), "Not approved");

        Optional<LeaveBalance> balanceAfter = leaveService.getLeaveBalance(emp.getId(), year);
        assertEquals(initVacation, balanceAfter.get().getVacationDaysRemaining());
    }

    @Test
    void testTotalEmployeeCountIntegration() {
        long countBefore = employeeService.getTotalCount();
        createEmployee("Count Test 1", "count1@hrms.com");
        createEmployee("Count Test 2", "count2@hrms.com");
        long countAfter = employeeService.getTotalCount();
        assertEquals(countBefore + 2, countAfter);
    }

    @Test
    void testPendingLeaveCountIntegration() {
        Employee emp = createEmployee("Pending Count", "pendingcount@hrms.com");
        long before = leaveService.getPendingCount();

        LeaveRequest lr = new LeaveRequest();
        lr.setEmployee(emp);
        lr.setLeaveType("SICK");
        lr.setStartDate(LocalDate.now().plusDays(1));
        lr.setEndDate(LocalDate.now().plusDays(1));
        lr.setReason("Count test");
        leaveService.submitLeaveRequest(lr);

        long after = leaveService.getPendingCount();
        assertEquals(before + 1, after);
    }
}
