package com.hrms.controller;

import com.hrms.model.Employee;
import com.hrms.model.LeaveBalance;
import com.hrms.model.LeaveRequest;
import com.hrms.service.AuthService;
import com.hrms.service.EmployeeService;
import com.hrms.service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
public class DashboardController {

    @Autowired
    private AuthService authService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private LeaveService leaveService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Optional<Employee> currentEmployee = authService.getCurrentEmployee();
        if (currentEmployee.isEmpty()) {
            return "redirect:/login";
        }

        Employee emp = currentEmployee.get();
        model.addAttribute("employee", emp);

        if ("ADMIN".equals(emp.getRole())) {
            // admin sees overall stats
            model.addAttribute("totalEmployees", employeeService.getTotalCount());
            model.addAttribute("pendingLeaves", leaveService.getPendingCount());
            model.addAttribute("approvedThisMonth", leaveService.getApprovedThisMonth());
            model.addAttribute("recentRequests", leaveService.getPendingRequests());
        } else {
            // regular employee sees their own info
            int year = LocalDate.now().getYear();
            Optional<LeaveBalance> balance = leaveService.getLeaveBalance(emp.getId(), year);
            model.addAttribute("balance", balance.orElse(null));
            List<LeaveRequest> myRequests = leaveService.getLeaveRequestsByEmployee(emp.getId());
            model.addAttribute("myRequests", myRequests);
        }

        return "dashboard";
    }
}
