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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/leaves")
public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private AuthService authService;

    @Autowired
    private EmployeeService employeeService;

    // admin sees all requests
    @GetMapping("/requests")
    public String allRequests(Model model) {
        List<LeaveRequest> requests = leaveService.getAllLeaveRequests();
        model.addAttribute("requests", requests);
        return "leaves/requests";
    }

    // employee sees their own leaves
    @GetMapping("/my-leaves")
    public String myLeaves(Model model) {
        Optional<Employee> current = authService.getCurrentEmployee();
        if (current.isEmpty()) return "redirect:/login";

        List<LeaveRequest> myRequests = leaveService.getLeaveRequestsByEmployee(current.get().getId());
        model.addAttribute("requests", myRequests);
        return "leaves/my-leaves";
    }

    @GetMapping("/submit")
    public String showSubmitForm(Model model) {
        Optional<Employee> current = authService.getCurrentEmployee();
        if (current.isEmpty()) return "redirect:/login";

        Employee emp = current.get();
        int year = LocalDate.now().getYear();
        LeaveBalance balance = leaveService.getOrCreateBalance(emp, year);

        model.addAttribute("leaveRequest", new LeaveRequest());
        model.addAttribute("balance", balance);
        return "leaves/submit";
    }

    @PostMapping("/submit")
    public String submitLeave(@ModelAttribute LeaveRequest leaveRequest,
                              RedirectAttributes redirectAttributes) {
        Optional<Employee> current = authService.getCurrentEmployee();
        if (current.isEmpty()) return "redirect:/login";

        leaveRequest.setEmployee(current.get());

        try {
            leaveService.submitLeaveRequest(leaveRequest);
            redirectAttributes.addFlashAttribute("successMsg", "Leave request submitted successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/leaves/my-leaves";
    }

    @PostMapping("/approve/{id}")
    public String approveLeave(@PathVariable Long id,
                               @RequestParam(required = false) String adminComment,
                               RedirectAttributes redirectAttributes) {
        try {
            leaveService.approveLeave(id, adminComment);
            redirectAttributes.addFlashAttribute("successMsg", "Leave request approved.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/leaves/requests";
    }

    @PostMapping("/reject/{id}")
    public String rejectLeave(@PathVariable Long id,
                              @RequestParam(required = false) String adminComment,
                              RedirectAttributes redirectAttributes) {
        try {
            leaveService.rejectLeave(id, adminComment);
            redirectAttributes.addFlashAttribute("successMsg", "Leave request rejected.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/leaves/requests";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        Optional<Employee> current = authService.getCurrentEmployee();
        if (current.isEmpty()) return "redirect:/login";

        Employee emp = current.get();
        int year = LocalDate.now().getYear();
        Optional<LeaveBalance> balance = leaveService.getLeaveBalance(emp.getId(), year);

        model.addAttribute("employee", emp);
        model.addAttribute("balance", balance.orElse(null));
        return "profile";
    }
}
