MERGE INTO employee (employee_id, full_name, email, department, position, hire_date, salary, phone, role, password)
KEY (email)
VALUES
('EMP001', 'Admin User', 'admin@hrms.com', 'Management', 'HR Administrator', '2022-01-15', 75000.00, '555-0100', 'ADMIN', 'admin123'),
('EMP002', 'John Smith', 'john.smith@hrms.com', 'Engineering', 'Software Engineer', '2023-03-10', 65000.00, '555-0101', 'EMPLOYEE', 'employee123'),
('EMP003', 'Jane Doe', 'jane.doe@hrms.com', 'Marketing', 'Marketing Specialist', '2023-06-20', 58000.00, '555-0102', 'EMPLOYEE', 'employee123');

MERGE INTO leave_balance (employee_id, leave_year, vacation_days_remaining, sick_days_remaining)
KEY (employee_id, leave_year)
SELECT e.id, 2025, 15, 10 FROM employee e WHERE e.email = 'john.smith@hrms.com'
UNION ALL
SELECT e.id, 2025, 12, 8 FROM employee e WHERE e.email = 'jane.doe@hrms.com';
