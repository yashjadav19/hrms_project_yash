package com.hrms.model;

import jakarta.persistence.*;

@Entity
@Table(name = "leave_balance",
       uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "leave_year"}))
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "leave_year")
    private Integer year;

    @Column(name = "vacation_days_remaining")
    private Integer vacationDaysRemaining;

    @Column(name = "sick_days_remaining")
    private Integer sickDaysRemaining;

    public LeaveBalance() {}

    public LeaveBalance(Employee employee, int year) {
        this.employee = employee;
        this.year = year;
        this.vacationDaysRemaining = 15;
        this.sickDaysRemaining = 10;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getVacationDaysRemaining() { return vacationDaysRemaining; }
    public void setVacationDaysRemaining(Integer vacationDaysRemaining) { this.vacationDaysRemaining = vacationDaysRemaining; }

    public Integer getSickDaysRemaining() { return sickDaysRemaining; }
    public void setSickDaysRemaining(Integer sickDaysRemaining) { this.sickDaysRemaining = sickDaysRemaining; }
}
