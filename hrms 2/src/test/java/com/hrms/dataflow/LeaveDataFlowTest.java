package com.hrms.dataflow;

import com.hrms.model.Employee;
import com.hrms.model.LeaveBalance;
import com.hrms.model.LeaveRequest;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.LeaveBalanceRepository;
import com.hrms.repository.LeaveRequestRepository;
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
class LeaveDataFlowTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    private Employee makeEmployee(String name, String email) {
        Employee e = new Employee();
        e.setFullName(name);
        e.setEmail(email);
        e.setPassword("pass");
        e.setRole("EMPLOYEE");
        e.setDepartment("HR");
        e.setSalary(new BigDecimal("45000"));
        e.setHireDate(LocalDate.now().minusYears(1));
        return employeeService.saveEmployee(e);
    }

    @Test
    void testEmployeeCreationFlowCreatesLeaveBalance() {
        Employee emp = makeEmployee("Balance Flow", "balanceflow@hrms.com");

        // creating an employee should automatically create a leave balance
        Optional<LeaveBalance> balance = leaveBalanceRepository
                .findByEmployeeIdAndYear(emp.getId(), LocalDate.now().getYear());
        assertTrue(balance.isPresent());
        assertEquals(15, balance.get().getVacationDaysRemaining());
        assertEquals(10, balance.get().getSickDaysRemaining());
    }

    @Test
    @Transactional
    void testLeaveRequestCreationDataFlow() {
        Employee emp = makeEmployee("Request Flow", "requestflow@hrms.com");

        LeaveRequest lr = new LeaveRequest();
        lr.setEmployee(emp);
        lr.setLeaveType("VACATION");
        lr.setStartDate(LocalDate.now().plusDays(10));
        lr.setEndDate(LocalDate.now().plusDays(12));
        lr.setReason("Testing the flow");

        LeaveRequest saved = leaveService.submitLeaveRequest(lr);

        assertNotNull(saved.getId());
        assertEquals("PENDING", saved.getStatus());
        assertNotNull(saved.getSubmittedDate());

        // retrieve from repository and verify data persisted correctly
        Optional<LeaveRequest> fetched = leaveRequestRepository.findById(saved.getId());
        assertTrue(fetched.isPresent());
        assertEquals("VACATION", fetched.get().getLeaveType());
        assertEquals(emp.getId(), fetched.get().getEmployee().getId());
    }

    @Test
    @Transactional
    void testBalanceDeductionAfterApproval() {
        Employee emp = makeEmployee("Deduct Flow", "deductflow@hrms.com");
        int year = LocalDate.now().getYear();

        // check initial balance
        Optional<LeaveBalance> before = leaveBalanceRepository.findByEmployeeIdAndYear(emp.getId(), year);
        assertTrue(before.isPresent());
        int initialVacation = before.get().getVacationDaysRemaining();

        LeaveRequest lr = new LeaveRequest();
        lr.setEmployee(emp);
        lr.setLeaveType("VACATION");
        lr.setStartDate(LocalDate.now().plusDays(3));
        lr.setEndDate(LocalDate.now().plusDays(5));
        lr.setReason("Testing deduction");

        LeaveRequest submitted = leaveService.submitLeaveRequest(lr);
        long days = submitted.getDays();

        leaveService.approveLeave(submitted.getId(), "Approved");

        Optional<LeaveBalance> after = leaveBalanceRepository.findByEmployeeIdAndYear(emp.getId(), year);
        assertTrue(after.isPresent());
        assertEquals((int)(initialVacation - days), (int) after.get().getVacationDaysRemaining());
    }

    @Test
    void testInputValidationFlow() {
        Employee emp = makeEmployee("Validation Flow", "validationflow@hrms.com");

        LeaveRequest lr = new LeaveRequest();
        lr.setEmployee(emp);
        lr.setLeaveType("VACATION");
        // requesting more days than available
        lr.setStartDate(LocalDate.now().plusDays(1));
        lr.setEndDate(LocalDate.now().plusDays(30));
        lr.setReason("Too many days");

        assertThrows(IllegalArgumentException.class, () -> leaveService.submitLeaveRequest(lr));
    }

    @Test
    void testDataRetrievalByEmployee() {
        Employee emp = makeEmployee("Retrieval Flow", "retrievalflow@hrms.com");
        List<LeaveRequest> requests = leaveService.getLeaveRequestsByEmployee(emp.getId());
        assertNotNull(requests);
        // newly created employee should have 0 requests
        assertEquals(0, requests.size());
    }
}
