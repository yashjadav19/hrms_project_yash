package com.hrms.path;

import com.hrms.model.Employee;
import com.hrms.model.LeaveRequest;
import com.hrms.service.EmployeeService;
import com.hrms.service.LeaveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class EmployeePathTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private LeaveService leaveService;

    private Employee createTestEmployee(String name, String email) {
        Employee e = new Employee();
        e.setFullName(name);
        e.setEmail(email);
        e.setPassword("testpass");
        e.setRole("EMPLOYEE");
        e.setDepartment("Engineering");
        e.setPosition("Developer");
        e.setSalary(new BigDecimal("55000"));
        e.setHireDate(LocalDate.of(2023, 1, 1));
        return employeeService.saveEmployee(e);
    }

    @Test
    void testAddEmployeePath() {
        Employee emp = createTestEmployee("Path Test User", "pathtest@hrms.com");
        assertNotNull(emp.getId());
        assertNotNull(emp.getEmployeeId());
        assertTrue(emp.getEmployeeId().startsWith("EMP"));
    }

    @Test
    void testEditEmployeePath() {
        Employee emp = createTestEmployee("Original Name", "editpath@hrms.com");
        Long id = emp.getId();

        emp.setFullName("Updated Name");
        emp.setDepartment("Marketing");
        employeeService.updateEmployee(emp);

        Optional<Employee> updated = employeeService.getEmployeeById(id);
        assertTrue(updated.isPresent());
        assertEquals("Updated Name", updated.get().getFullName());
        assertEquals("Marketing", updated.get().getDepartment());
    }

    @Test
    void testDeleteEmployeePath() {
        Employee emp = createTestEmployee("To Delete", "deletepath@hrms.com");
        Long id = emp.getId();

        employeeService.deleteEmployee(id);

        Optional<Employee> deleted = employeeService.getEmployeeById(id);
        assertFalse(deleted.isPresent());
    }

    @Test
    void testFullEmployeeLifecyclePath() {
        // add
        Employee emp = createTestEmployee("Lifecycle User", "lifecycle@hrms.com");
        assertNotNull(emp.getId());

        // edit
        emp.setPosition("Senior Developer");
        emp.setSalary(new BigDecimal("70000"));
        employeeService.updateEmployee(emp);

        Optional<Employee> updated = employeeService.getEmployeeById(emp.getId());
        assertTrue(updated.isPresent());
        assertEquals("Senior Developer", updated.get().getPosition());

        // delete
        employeeService.deleteEmployee(emp.getId());
        assertFalse(employeeService.getEmployeeById(emp.getId()).isPresent());
    }

    @Test
    @Transactional
    void testSubmitAndApproveLeaveRequestPath() {
        Employee emp = createTestEmployee("Leave Path User", "leavepath@hrms.com");

        LeaveRequest lr = new LeaveRequest();
        lr.setEmployee(emp);
        lr.setLeaveType("VACATION");
        lr.setStartDate(LocalDate.now().plusDays(5));
        lr.setEndDate(LocalDate.now().plusDays(7));
        lr.setReason("Family vacation");

        LeaveRequest submitted = leaveService.submitLeaveRequest(lr);
        assertNotNull(submitted.getId());
        assertEquals("PENDING", submitted.getStatus());

        LeaveRequest approved = leaveService.approveLeave(submitted.getId(), "Looks good");
        assertEquals("APPROVED", approved.getStatus());
        assertEquals("Looks good", approved.getAdminComment());
    }

    @Test
    @Transactional
    void testSubmitAndRejectLeaveRequestPath() {
        Employee emp = createTestEmployee("Reject Path User", "rejectpath@hrms.com");

        LeaveRequest lr = new LeaveRequest();
        lr.setEmployee(emp);
        lr.setLeaveType("SICK");
        lr.setStartDate(LocalDate.now().plusDays(1));
        lr.setEndDate(LocalDate.now().plusDays(2));
        lr.setReason("Not feeling well");

        LeaveRequest submitted = leaveService.submitLeaveRequest(lr);
        assertEquals("PENDING", submitted.getStatus());

        LeaveRequest rejected = leaveService.rejectLeave(submitted.getId(), "Insufficient documentation");
        assertEquals("REJECTED", rejected.getStatus());
    }
}
