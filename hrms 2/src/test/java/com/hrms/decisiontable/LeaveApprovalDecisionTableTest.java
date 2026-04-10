package com.hrms.decisiontable;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Decision table tests for leave approval rules.
 *
 * Conditions:
 *   C1 = hasBalance (employee has remaining leave balance)
 *   C2 = noticePeriodValid (at least 2 days notice)
 *   C3 = reportingManagerApproval (manager has approved)
 *
 * Actions:
 *   A1 = Approve
 *   A2 = Reject
 *   A3 = RequestMoreInfo
 *
 * Decision table (8 combinations):
 * | C1 | C2 | C3 | Action |
 * |  T |  T |  T | Approve|
 * |  T |  T |  F | RequestMoreInfo |
 * |  T |  F |  T | RequestMoreInfo |
 * |  T |  F |  F | Reject |
 * |  F |  T |  T | Reject |
 * |  F |  T |  F | Reject |
 * |  F |  F |  T | Reject |
 * |  F |  F |  F | Reject |
 */
class LeaveApprovalDecisionTableTest {

    // helper method that implements the approval logic
    private String getDecision(boolean hasBalance, boolean noticePeriodValid, boolean managerApproval) {
        if (!hasBalance) {
            return "Reject";
        }
        if (hasBalance && noticePeriodValid && managerApproval) {
            return "Approve";
        }
        if (hasBalance && noticePeriodValid && !managerApproval) {
            return "RequestMoreInfo";
        }
        if (hasBalance && !noticePeriodValid && managerApproval) {
            return "RequestMoreInfo";
        }
        // hasBalance=true, noticePeriod=false, manager=false
        return "Reject";
    }

    // Rule 1: all conditions true -> Approve
    @Test
    void rule1_AllTrue_ShouldApprove() {
        String result = getDecision(true, true, true);
        assertEquals("Approve", result);
    }

    // Rule 2: balance OK, notice OK, no manager approval -> RequestMoreInfo
    @Test
    void rule2_BalanceOkNoticeOkNoManager_ShouldRequestMoreInfo() {
        String result = getDecision(true, true, false);
        assertEquals("RequestMoreInfo", result);
    }

    // Rule 3: balance OK, no notice, manager approval -> RequestMoreInfo
    @Test
    void rule3_BalanceOkNoNoticeManagerOk_ShouldRequestMoreInfo() {
        String result = getDecision(true, false, true);
        assertEquals("RequestMoreInfo", result);
    }

    // Rule 4: balance OK, no notice, no manager -> Reject
    @Test
    void rule4_BalanceOkNoNoticeNoManager_ShouldReject() {
        String result = getDecision(true, false, false);
        assertEquals("Reject", result);
    }

    // Rule 5: no balance, notice OK, manager OK -> Reject
    @Test
    void rule5_NoBalanceNoticOkManagerOk_ShouldReject() {
        String result = getDecision(false, true, true);
        assertEquals("Reject", result);
    }

    // Rule 6: no balance, notice OK, no manager -> Reject
    @Test
    void rule6_NoBalanceNoticeOkNoManager_ShouldReject() {
        String result = getDecision(false, true, false);
        assertEquals("Reject", result);
    }

    // Rule 7: no balance, no notice, manager OK -> Reject
    @Test
    void rule7_NoBalanceNoNoticeManagerOk_ShouldReject() {
        String result = getDecision(false, false, true);
        assertEquals("Reject", result);
    }

    // Rule 8: all conditions false -> Reject
    @Test
    void rule8_AllFalse_ShouldReject() {
        String result = getDecision(false, false, false);
        assertEquals("Reject", result);
    }
}
