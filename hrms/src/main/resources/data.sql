MERGE INTO employee (employee_id, full_name, email, department, position, hire_date, salary, phone, role, password)
KEY (email)
VALUES
('EMP001', 'Admin User', 'admin@hrms.com', 'Management', 'HR Administrator', '2022-01-15', 75000.00, '555-0100', 'ADMIN', '$2a$10$N.zmdr9zkoa05638FeT9euqHhQ7G6I1WuV7OmzNaGR.1RMvLtgDde'),
('EMP002', 'John Smith', 'john.smith@hrms.com', 'Engineering', 'Software Engineer', '2023-03-10', 65000.00, '555-0101', 'EMPLOYEE', '$2a$10$8K1p/a0dL1LXMIgoEDFrwOfMQkLmjHBdMCl2yz3HZoHhP.6Ov6/oK'),
('EMP003', 'Jane Doe', 'jane.doe@hrms.com', 'Marketing', 'Marketing Specialist', '2023-06-20', 58000.00, '555-0102', 'EMPLOYEE', '$2a$10$8K1p/a0dL1LXMIgoEDFrwOfMQkLmjHBdMCl2yz3HZoHhP.6Ov6/oK');

MERGE INTO leave_balance (employee_id, leave_year, vacation_days_remaining, sick_days_remaining)
KEY (employee_id, leave_year)
SELECT e.id, 2025, 15, 10 FROM employee e WHERE e.email = 'john.smith@hrms.com'
UNION ALL
SELECT e.id, 2025, 12, 8 FROM employee e WHERE e.email = 'jane.doe@hrms.com';
