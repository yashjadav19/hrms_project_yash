package com.hrms.controller;

import com.hrms.model.Employee;
import com.hrms.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    public String listEmployees(@RequestParam(required = false) String search,
                                @RequestParam(required = false) String department,
                                Model model) {
        List<Employee> employees;

        if (search != null && !search.isEmpty()) {
            employees = employeeService.searchByName(search);
            model.addAttribute("search", search);
        } else if (department != null && !department.isEmpty()) {
            employees = employeeService.filterByDepartment(department);
            model.addAttribute("department", department);
        } else {
            employees = employeeService.getAllEmployees();
        }

        model.addAttribute("employees", employees);
        return "employees/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("employee", new Employee());
        return "employees/add";
    }

    @PostMapping("/add")
    public String addEmployee(@Valid @ModelAttribute Employee employee,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (result.hasErrors()) {
            return "employees/add";
        }

        if (employee.getRole() == null || employee.getRole().isEmpty()) {
            employee.setRole("EMPLOYEE");
        }

        // check for duplicate email
        if (employeeService.emailExists(employee.getEmail(), -1L)) {
            result.rejectValue("email", "error.employee", "Email already registered");
            return "employees/add";
        }

        employeeService.saveEmployee(employee);
        redirectAttributes.addFlashAttribute("successMsg", "Employee added successfully.");
        return "redirect:/employees";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Employee> emp = employeeService.getEmployeeById(id);
        if (emp.isEmpty()) {
            return "redirect:/employees";
        }
        Employee e = emp.get();
        // don't show the hashed password in the form
        e.setPassword("");
        model.addAttribute("employee", e);
        return "employees/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateEmployee(@PathVariable Long id,
                                 @Valid @ModelAttribute Employee employee,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (result.hasErrors()) {
            return "employees/edit";
        }

        // check for duplicate email but excluding this record
        if (employeeService.emailExists(employee.getEmail(), id)) {
            result.rejectValue("email", "error.employee", "Email already in use by another employee");
            return "employees/edit";
        }

        Optional<Employee> existing = employeeService.getEmployeeById(id);
        if (existing.isEmpty()) {
            return "redirect:/employees";
        }

        Employee existingEmp = existing.get();
        existingEmp.setFullName(employee.getFullName());
        existingEmp.setEmail(employee.getEmail());
        existingEmp.setDepartment(employee.getDepartment());
        existingEmp.setPosition(employee.getPosition());
        existingEmp.setHireDate(employee.getHireDate());
        existingEmp.setSalary(employee.getSalary());
        existingEmp.setPhone(employee.getPhone());
        existingEmp.setRole(employee.getRole());

        // only update password if they entered one
        if (employee.getPassword() != null && !employee.getPassword().isEmpty()) {
            existingEmp.setPassword(employee.getPassword());
        }

        employeeService.updateEmployee(existingEmp);
        redirectAttributes.addFlashAttribute("successMsg", "Employee updated successfully.");
        return "redirect:/employees";
    }

    @PostMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        employeeService.deleteEmployee(id);
        redirectAttributes.addFlashAttribute("successMsg", "Employee deleted.");
        return "redirect:/employees";
    }
}
