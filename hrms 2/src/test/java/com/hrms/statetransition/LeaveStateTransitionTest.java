package com.hrms.statetransition;

import com.hrms.model.Employee;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * State transition tests for leave request states.
 *
 * Valid transitions:
 *   DRAFT -> SUBMITTED (in our system, direct submission)
 *   SUBMITTED/PENDING -> APPROVED
 *   SUBMITTED/PENDING -> REJECTED
 *   APPROVED -> COMPLETED (conceptual)
 *
 * Invalid transitions:
 *   APPROVED -> PENDING
 *   REJECTED -> APPROVED
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class LeaveStateTransitionTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private LeaveService leaveService;

    private Employee makeEmployee(String email) {
        Employee e = new Employee();
        e.setFullName("State Test");
        e.setEmail(email);
        e.setPassword("pass");
        e.setRole("EMPLOYEE");
        e.setDepartment("QA");
        e.setSalary(new BigDecimal("50000"));
        e.setHireDate(LocalDate.now().minusYears(1));
        return employeeService.saveEmployee(e);
    }

    private LeaveRequest makeRequest(Employee emp) {
        LeaveRequest lr = new LeaveRequest();
        lr.setEmployee(emp);
        lr.setLeaveType("VACATION");
        lr.setStartDate(LocalDate.now().plusDays(5));
        lr.setEndDate(LocalDate.now().plusDays(7));
        lr.setReason("State transition test");
        return lr;
    }

    // DRAFT -> SUBMITTED: submitting a request changes status to PENDING
    @Test
    @Transactional
    void testSubmittedStateAfterCreation() {
        Employee emp = makeEmployee("state1@hrms.com");
        LeaveRequest lr = makeRequest(emp);
        LeaveRequest submitted = leaveService.submitLeaveRequest(lr);
        assertEquals("PENDING", submitted.getStatus());
    }

    // PENDING -> APPROVED: valid transition
    @Test
    @Transactional
    void testPendingToApprovedTransition() {
        Employee emp = makeEmployee("state2@hrms.com");
        LeaveRequest lr = makeRequest(emp);
        LeaveRequest submitted = leaveService.submitLeaveRequest(lr);
        assertEquals("PENDING", submitted.getStatus());

        LeaveRequest approved = leaveService.approveLeave(submitted.getId(), "Approved");
        assertEquals("APPROVED", approved.getStatus());
    }

    // PENDING -> REJECTED: valid transition
    @Test
    @Transactional
    void testPendingToRejectedTransition() {
        Employee emp = makeEmployee("state3@hrms.com");
        LeaveRequest lr = makeRequest(emp);
        LeaveRequest submitted = leaveService.submitLeaveRequest(lr);
        assertEquals("PENDING", submitted.getStatus());

        LeaveRequest rejected = leaveService.rejectLeave(submitted.getId(), "Not approved");
        assertEquals("REJECTED", rejected.getStatus());
    }

    // APPROVED -> PENDING (invalid): should throw exception
    @Test
    @Transactional
    void testApprovedToPendingIsInvalidTransition() {
        Employee emp = makeEmployee("state4@hrms.com");
        LeaveRequest lr = makeRequest(emp);
        LeaveRequest submitted = leaveService.submitLeaveRequest(lr);
        leaveService.approveLeave(submitted.getId(), "Approved");

        // trying to approve again should throw -- already approved
        assertThrows(Exception.class, () -> leaveService.approveLeave(submitted.getId(), "Again"));
    }

    // REJECTED -> APPROVED (invalid): should throw exception
    @Test
    @Transactional
    void testRejectedToApprovedIsInvalidTransition() {
        Employee emp = makeEmployee("state5@hrms.com");
        LeaveRequest lr = makeRequest(emp);
        LeaveRequest submitted = leaveService.submitLeaveRequest(lr);
        leaveService.rejectLeave(submitted.getId(), "Rejected");

        // can't approve a rejected request
        assertThrows(Exception.class, () -> leaveService.approveLeave(submitted.getId(), "Trying to approve"));
    }

    // test that admin comment is preserved in each state
    @Test
    @Transactional
    void testAdminCommentStoredOnApproval() {
        Employee emp = makeEmployee("state6@hrms.com");
        LeaveRequest lr = makeRequest(emp);
        LeaveRequest submitted = leaveService.submitLeaveRequest(lr);

        String comment = "Approved for this quarter";
        LeaveRequest approved = leaveService.approveLeave(submitted.getId(), comment);
        assertEquals(comment, approved.getAdminComment());
    }

    // test initial state is always PENDING when submitted
    @Test
    @Transactional
    void testInitialStateIsAlwaysPending() {
        Employee emp = makeEmployee("state7@hrms.com");
        // submit two separate requests -- both should start at PENDING
        LeaveRequest lr1 = makeRequest(emp);
        LeaveRequest sub1 = leaveService.submitLeaveRequest(lr1);

        LeaveRequest lr2 = new LeaveRequest();
        lr2.setEmployee(emp);
        lr2.setLeaveType("SICK");
        lr2.setStartDate(LocalDate.now().plusDays(10));
        lr2.setEndDate(LocalDate.now().plusDays(10));
        lr2.setReason("Sick day");
        LeaveRequest sub2 = leaveService.submitLeaveRequest(lr2);

        assertEquals("PENDING", sub1.getStatus());
        assertEquals("PENDING", sub2.getStatus());
    }
}
