package com.hrms.repository;

import com.hrms.model.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeId(Long employeeId);

    List<LeaveRequest> findByStatus(String status);

    List<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, String status);

    // used for dashboard count of approved leaves this month
    List<LeaveRequest> findByStatusAndStartDateBetween(String status, LocalDate from, LocalDate to);

    long countByStatus(String status);
}
