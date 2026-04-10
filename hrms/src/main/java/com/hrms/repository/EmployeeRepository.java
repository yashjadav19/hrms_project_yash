package com.hrms.repository;

import com.hrms.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Employee> findByFullNameContainingIgnoreCase(String name);

    List<Employee> findByDepartment(String department);

    // get the max emp id number so we can generate the next one
    @Query("SELECT MAX(e.employeeId) FROM Employee e")
    String findMaxEmployeeId();
}
