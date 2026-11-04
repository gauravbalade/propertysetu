package com.gaurav.property.service;

import com.gaurav.property.enums.ApplicationStatus;

/**
 * Shared server-side rules for the PropertySetu application lifecycle.
 * Controllers and the frontend are not trusted to authorize transitions.
 */
public final class ApplicationWorkflowPolicy {

    private ApplicationWorkflowPolicy() {
    }

    public static boolean canSubmit(ApplicationStatus status) {
        return status == ApplicationStatus.DRAFT || status == ApplicationStatus.NEEDS_CORRECTION;
    }

    public static ApplicationStatus submissionTarget(ApplicationStatus status) {
        if (status == ApplicationStatus.DRAFT) {
            return ApplicationStatus.SUBMITTED;
        }
        if (status == ApplicationStatus.NEEDS_CORRECTION) {
            return ApplicationStatus.RESUBMITTED;
        }
        throw new IllegalArgumentException("Only a draft or correction-requested application can be submitted.");
    }

    public static boolean canAssignOfficer(ApplicationStatus status) {
        return status == ApplicationStatus.PAID
                || status == ApplicationStatus.UNDER_VERIFICATION
                || status == ApplicationStatus.NEEDS_CORRECTION
                || status == ApplicationStatus.RESUBMITTED;
    }

    public static boolean canUploadDocuments(ApplicationStatus status) {
        return status == ApplicationStatus.DRAFT || status == ApplicationStatus.NEEDS_CORRECTION;
    }

    public static boolean canReviewDocuments(ApplicationStatus status) {
        return status == ApplicationStatus.PAID
                || status == ApplicationStatus.UNDER_VERIFICATION
                || status == ApplicationStatus.RESUBMITTED;
    }

    public static boolean canVerifyApplication(ApplicationStatus status) {
        return canReviewDocuments(status);
    }

    public static boolean canEditDraft(ApplicationStatus status) {
        return status == ApplicationStatus.DRAFT;
    }
}
