package com.hrms.service;

import com.hrms.model.Employee;
import com.hrms.model.LeaveBalance;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.LeaveBalanceRepository;
import com.hrms.repository.LeaveRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    public Optional<Employee> getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    public List<Employee> searchByName(String name) {
        return employeeRepository.findByFullNameContainingIgnoreCase(name);
    }

    public List<Employee> filterByDepartment(String department) {
        return employeeRepository.findByDepartment(department);
    }

    public Employee saveEmployee(Employee employee) {
        // auto generate employee id if this is a new record
        if (employee.getEmployeeId() == null || employee.getEmployeeId().isEmpty()) {
            employee.setEmployeeId(generateNextEmployeeId());
        }

        // encode password before saving
        if (employee.getPassword() != null && !employee.getPassword().startsWith("$2a$")) {
            employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        }

        Employee saved = employeeRepository.save(employee);

        // create a leave balance record for the current year
        int currentYear = LocalDate.now().getYear();
        if (leaveBalanceRepository.findByEmployeeIdAndYear(saved.getId(), currentYear).isEmpty()) {
            LeaveBalance balance = new LeaveBalance(saved, currentYear);
            leaveBalanceRepository.save(balance);
        }

        return saved;
    }

    public Employee updateEmployee(Employee employee) {
        // only re-encode password if it changed
        if (employee.getPassword() != null && !employee.getPassword().startsWith("$2a$")) {
            employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        }
        return employeeRepository.save(employee);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        // remove dependent records first to avoid FK violations
        leaveRequestRepository.findByEmployeeId(id).forEach(lr ->
            leaveRequestRepository.deleteById(lr.getId()));
        leaveBalanceRepository.findAll().stream()
            .filter(lb -> lb.getEmployee().getId().equals(id))
            .forEach(lb -> leaveBalanceRepository.deleteById(lb.getId()));
        employeeRepository.deleteById(id);
    }

    public boolean emailExists(String email, Long excludeId) {
        Optional<Employee> existing = employeeRepository.findByEmail(email);
        if (existing.isEmpty()) return false;
        return !existing.get().getId().equals(excludeId);
    }

    private String generateNextEmployeeId() {
        String maxId = employeeRepository.findMaxEmployeeId();
        if (maxId == null) {
            return "EMP001";
        }
        // extract the number portion and increment
        int num = Integer.parseInt(maxId.replace("EMP", ""));
        return String.format("EMP%03d", num + 1);
    }

    public long getTotalCount() {
        return employeeRepository.count();
    }
}
