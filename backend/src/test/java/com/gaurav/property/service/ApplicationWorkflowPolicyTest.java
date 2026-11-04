package com.gaurav.property.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.gaurav.property.enums.ApplicationStatus;

class ApplicationWorkflowPolicyTest {

    @Test
    void draftSubmissionMovesToSubmitted() {
        assertTrue(ApplicationWorkflowPolicy.canSubmit(ApplicationStatus.DRAFT));
        assertEquals(ApplicationStatus.SUBMITTED,
                ApplicationWorkflowPolicy.submissionTarget(ApplicationStatus.DRAFT));
    }

    @Test
    void correctionSubmissionMovesToResubmittedWithoutPaymentStatusReset() {
        assertTrue(ApplicationWorkflowPolicy.canSubmit(ApplicationStatus.NEEDS_CORRECTION));
        assertEquals(ApplicationStatus.RESUBMITTED,
                ApplicationWorkflowPolicy.submissionTarget(ApplicationStatus.NEEDS_CORRECTION));
    }

    @Test
    void paidAndReviewStatusesCanBeAssignedToAnOfficer() {
        assertTrue(ApplicationWorkflowPolicy.canAssignOfficer(ApplicationStatus.PAID));
        assertTrue(ApplicationWorkflowPolicy.canAssignOfficer(ApplicationStatus.UNDER_VERIFICATION));
        assertTrue(ApplicationWorkflowPolicy.canAssignOfficer(ApplicationStatus.NEEDS_CORRECTION));
        assertTrue(ApplicationWorkflowPolicy.canAssignOfficer(ApplicationStatus.RESUBMITTED));
        assertFalse(ApplicationWorkflowPolicy.canAssignOfficer(ApplicationStatus.DRAFT));
        assertFalse(ApplicationWorkflowPolicy.canAssignOfficer(ApplicationStatus.SUBMITTED));
    }

    @Test
    void documentUploadIsLimitedToDraftAndCorrectionStates() {
        assertTrue(ApplicationWorkflowPolicy.canUploadDocuments(ApplicationStatus.DRAFT));
        assertTrue(ApplicationWorkflowPolicy.canUploadDocuments(ApplicationStatus.NEEDS_CORRECTION));
        assertFalse(ApplicationWorkflowPolicy.canUploadDocuments(ApplicationStatus.PAID));
        assertFalse(ApplicationWorkflowPolicy.canUploadDocuments(ApplicationStatus.COMPLETED));
    }

    @Test
    void reviewAndVerificationRequirePaidOrResubmittedCase() {
        assertTrue(ApplicationWorkflowPolicy.canReviewDocuments(ApplicationStatus.PAID));
        assertTrue(ApplicationWorkflowPolicy.canVerifyApplication(ApplicationStatus.RESUBMITTED));
        assertFalse(ApplicationWorkflowPolicy.canVerifyApplication(ApplicationStatus.NEEDS_CORRECTION));
        assertFalse(ApplicationWorkflowPolicy.canReviewDocuments(ApplicationStatus.SUBMITTED));
    }

    @Test
    void completedApplicationCannotBeSubmittedAgain() {
        assertFalse(ApplicationWorkflowPolicy.canSubmit(ApplicationStatus.COMPLETED));
        assertThrows(IllegalArgumentException.class,
                () -> ApplicationWorkflowPolicy.submissionTarget(ApplicationStatus.COMPLETED));
    }
}
