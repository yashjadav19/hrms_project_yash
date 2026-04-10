package com.hrms.service;

import com.hrms.model.Employee;
import com.hrms.model.LeaveBalance;
import com.hrms.model.LeaveRequest;
import com.hrms.repository.LeaveBalanceRepository;
import com.hrms.repository.LeaveRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class LeaveService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestRepository.findAll();
    }

    public List<LeaveRequest> getLeaveRequestsByEmployee(Long employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId);
    }

    public List<LeaveRequest> getPendingRequests() {
        return leaveRequestRepository.findByStatus("PENDING");
    }

    public Optional<LeaveRequest> getLeaveRequestById(Long id) {
        return leaveRequestRepository.findById(id);
    }

    @Transactional
    public LeaveRequest submitLeaveRequest(LeaveRequest leaveRequest) {
        // check that the employee has enough balance
        int year = leaveRequest.getStartDate().getYear();
        long days = leaveRequest.getDays();

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndYear(leaveRequest.getEmployee().getId(), year)
                .orElseThrow(() -> new IllegalStateException("No leave balance found for this year"));

        String type = leaveRequest.getLeaveType();

        if ("VACATION".equals(type) || "PERSONAL".equals(type)) {
            if (balance.getVacationDaysRemaining() < days) {
                throw new IllegalArgumentException("Not enough vacation days remaining");
            }
        } else if ("SICK".equals(type)) {
            if (balance.getSickDaysRemaining() < days) {
                throw new IllegalArgumentException("Not enough sick days remaining");
            }
        } else if ("EMERGENCY".equals(type)) {
            // emergency leave uses vacation balance
            if (balance.getVacationDaysRemaining() < days) {
                throw new IllegalArgumentException("Not enough vacation days remaining for emergency leave");
            }
        }

        leaveRequest.setStatus("PENDING");
        return leaveRequestRepository.save(leaveRequest);
    }

    @Transactional
    public LeaveRequest approveLeave(Long id, String adminComment) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException("Can only approve pending requests");
        }

        // deduct from balance when approved
        int year = request.getStartDate().getYear();
        long days = request.getDays();
        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndYear(request.getEmployee().getId(), year)
                .orElseThrow(() -> new IllegalStateException("No leave balance found"));

        String type = request.getLeaveType();
        if ("SICK".equals(type)) {
            balance.setSickDaysRemaining((int)(balance.getSickDaysRemaining() - days));
        } else {
            balance.setVacationDaysRemaining((int)(balance.getVacationDaysRemaining() - days));
        }
        leaveBalanceRepository.save(balance);

        request.setStatus("APPROVED");
        request.setAdminComment(adminComment);
        return leaveRequestRepository.save(request);
    }

    @Transactional
    public LeaveRequest rejectLeave(Long id, String adminComment) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException("Can only reject pending requests");
        }

        request.setStatus("REJECTED");
        request.setAdminComment(adminComment);
        return leaveRequestRepository.save(request);
    }

    public Optional<LeaveBalance> getLeaveBalance(Long employeeId, int year) {
        return leaveBalanceRepository.findByEmployeeIdAndYear(employeeId, year);
    }

    public LeaveBalance getOrCreateBalance(Employee employee, int year) {
        return leaveBalanceRepository.findByEmployeeIdAndYear(employee.getId(), year)
                .orElseGet(() -> {
                    LeaveBalance b = new LeaveBalance(employee, year);
                    return leaveBalanceRepository.save(b);
                });
    }

    public long getPendingCount() {
        return leaveRequestRepository.countByStatus("PENDING");
    }

    public long getApprovedThisMonth() {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        return leaveRequestRepository.findByStatusAndStartDateBetween("APPROVED", start, end).size();
    }
}
