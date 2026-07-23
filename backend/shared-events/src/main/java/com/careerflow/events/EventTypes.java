package com.careerflow.events;

public final class EventTypes {

    public static final String APPLICATION_CREATED = "ApplicationCreated";
    public static final String APPLICATION_STATUS_CHANGED = "ApplicationStatusChanged";
    public static final String OFFER_ADDED = "OfferAdded";
    public static final String OFFER_UPDATED = "OfferUpdated";
    public static final String RESUME_UPLOADED = "ResumeUploaded";
    public static final String RESUME_DELETED = "ResumeDeleted";
    public static final String INTERVIEW_SCHEDULED = "InterviewScheduled";
    public static final String INTERVIEW_COMPLETED = "InterviewCompleted";

    private EventTypes() {
    }
}
