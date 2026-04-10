# HRMS Testing Documentation

## Overview of Test Strategy

The HRMS testing suite covers 8 distinct software testing techniques to ensure correctness across all layers of the application. Tests are written with JUnit 5 and Mockito, and use a real H2 in-memory database to validate actual behavior rather than mocked interactions where possible.

Run all tests:
```bash
mvn test
```

---

## 1. Path Testing (`EmployeePathTest.java`)

Path testing verifies that specific execution paths through the code work correctly end to end.

| Test Case | Path | Expected Result |
|-----------|------|-----------------|
| testAddEmployeePath | Create employee → Check persisted | Employee saved with auto-generated ID |
| testEditEmployeePath | Create → Edit → Verify | Updated fields reflected in DB |
| testDeleteEmployeePath | Create → Delete → Query | Employee no longer findable |
| testFullEmployeeLifecyclePath | Add → Edit → Delete | All three operations succeed |
| testSubmitAndApproveLeaveRequestPath | Submit → Approve | Status changes to APPROVED |
| testSubmitAndRejectLeaveRequestPath | Submit → Reject | Status changes to REJECTED |

---

## 2. Data Flow Testing (`LeaveDataFlowTest.java`)

Data flow testing traces how data moves through the system — from input to storage to output.

| Test Case | Data Flow | Expected Result |
|-----------|-----------|-----------------|
| testEmployeeCreationFlowCreatesLeaveBalance | Create employee → Check DB | Leave balance auto-created |
| testLeaveRequestCreationDataFlow | Submit request → Fetch from DB | All fields correctly persisted |
| testBalanceDeductionAfterApproval | Submit → Approve → Check balance | Balance decremented by correct days |
| testInputValidationFlow | Request exceeding balance | Exception thrown before save |
| testDataRetrievalByEmployee | Query by employee ID | Returns only that employee's requests |

---

## 3. Integration Testing (`EmployeeLeaveIntegrationTest.java`)

Integration tests verify that multiple components work correctly together — especially across service boundaries.

| Test Case | Components | Expected Result |
|-----------|------------|-----------------|
| testEmployeeAndLeaveServiceIntegration | EmployeeService + LeaveService | Balance created when employee is created |
| testLeaveSubmissionUpdatesEmployeeContext | Controller → Service → DB | Request linked to correct employee |
| testApproveUpdatesBothLeaveAndBalance | LeaveService → LeaveBalanceRepository | Both request status and balance updated |
| testRejectDoesNotDeductBalance | LeaveService reject path | Balance unchanged on rejection |
| testTotalEmployeeCountIntegration | EmployeeService count | Count increases after creates |
| testPendingLeaveCountIntegration | LeaveService pending count | Pending count increases after submission |

---

## 4. Boundary Value Testing (`SalaryBoundaryTest.java`)

Boundary value testing checks inputs at the exact edges of valid ranges.

| Test Case | Input Value | Expected Result |
|-----------|-------------|-----------------|
| testSalaryAtMinimumIsValid | 30,000 | Valid |
| testSalaryAtMaximumIsValid | 500,000 | Valid |
| testSalaryJustAboveMinIsValid | 30,001 | Valid |
| testSalaryJustBelowMaxIsValid | 499,999 | Valid |
| testSalaryBelowMinimumIsInvalid | 29,999 | Business rule violation |
| testSalaryAboveMaximumIsInvalid | 500,001 | Business rule violation |
| testNegativeSalaryIsInvalid | -1,000 | Bean validation violation |
| testZeroSalaryIsInvalid | 0 | Bean validation violation |

---

## 5. Equivalence Class Testing (`EmployeeEquivalenceTest.java`)

Equivalence partitioning groups inputs into classes where all members are expected to behave the same.

**Valid Classes:**

| Class | Condition | Example |
|-------|-----------|---------|
| Full-time | salary >= 40,000 | 55,000 |
| Part-time | salary 20,000–39,999 | 30,000 |
| Contract | salary >= 50,000 | 75,000 |

**Invalid Classes:**

| Class | Condition | Example |
|-------|-----------|---------|
| Negative salary | salary < 0 | -500 |
| Empty name | fullName is blank | "" |
| Blank name | whitespace only | "   " |
| Bad email (no @) | email missing @ | "invalidemail.com" |
| Bad email (no domain) | domain missing | "user@" |
| Null email | email is empty | "" |

---

## 6. Decision Table Testing (`LeaveApprovalDecisionTableTest.java`)

Decision tables enumerate all combinations of conditions and map them to expected actions.

**Conditions:**
- C1: `hasBalance` — employee has remaining leave days
- C2: `noticePeriodValid` — at least 2 days advance notice
- C3: `reportingManagerApproval` — manager has given approval

| Rule | C1 | C2 | C3 | Action |
|------|----|----|----|-|
| 1 | T | T | T | Approve |
| 2 | T | T | F | RequestMoreInfo |
| 3 | T | F | T | RequestMoreInfo |
| 4 | T | F | F | Reject |
| 5 | F | T | T | Reject |
| 6 | F | T | F | Reject |
| 7 | F | F | T | Reject |
| 8 | F | F | F | Reject |

---

## 7. State Transition Testing (`LeaveStateTransitionTest.java`)

State transition testing verifies that the system correctly moves between allowed states and refuses invalid transitions.

**Valid Transitions:**

| From State | To State | Test Case |
|------------|----------|-----------|
| (new) | PENDING | testSubmittedStateAfterCreation |
| PENDING | APPROVED | testPendingToApprovedTransition |
| PENDING | REJECTED | testPendingToRejectedTransition |

**Invalid Transitions (must throw exception):**

| From State | To State | Test Case |
|------------|----------|-----------|
| APPROVED | PENDING/APPROVED | testApprovedToPendingIsInvalidTransition |
| REJECTED | APPROVED | testRejectedToApprovedIsInvalidTransition |

---

## 8. Use Case Testing (`HrmsUseCaseTest.java`)

Use case tests simulate full end-to-end user scenarios from the perspective of a real user.

| Use Case | Actor | Steps | Expected Outcome |
|----------|-------|-------|------------------|
| UC1: Apply for vacation | Employee | Check balance → Submit request | Request created with PENDING status |
| UC2: Add new employee | Admin | Fill form → Save | Employee stored, balance auto-created, ID generated |
| UC3: Approve leave request | Admin/Manager | View pending → Approve | Status becomes APPROVED, comment stored |
| UC4: Check leave balance | Employee | View dashboard | Balance shown, can determine if enough days remain |

---

## Sample Test Output

When running `mvn test`, you should see output similar to:

```
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0 -- EmployeePathTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0 -- LeaveDataFlowTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0 -- EmployeeLeaveIntegrationTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0 -- SalaryBoundaryTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0 -- EmployeeEquivalenceTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0 -- LeaveApprovalDecisionTableTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0 -- LeaveStateTransitionTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 -- HrmsUseCaseTest
[INFO] BUILD SUCCESS
```
