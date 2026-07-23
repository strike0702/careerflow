package com.careerflow.applicationservice.events.mapping;

import com.careerflow.applicationservice.application.model.Application;
import com.careerflow.applicationservice.application.model.ApplicationStatus;
import com.careerflow.applicationservice.offer.model.Offer;
import com.careerflow.events.DomainEventEnvelope;
import com.careerflow.events.EventMetadata;
import com.careerflow.events.EventTypes;
import com.careerflow.events.payload.ApplicationCreatedPayload;
import com.careerflow.events.payload.ApplicationStatusChangedPayload;
import com.careerflow.events.payload.OfferEventPayload;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class ApplicationEventMapper {

    public DomainEventEnvelope<ApplicationCreatedPayload> applicationCreated(
        Application application,
        String userId,
        String requestId
    ) {
        ApplicationCreatedPayload payload = new ApplicationCreatedPayload(
            application.getId(),
            application.getCompanyName(),
            application.getJobTitle(),
            application.getStatus().name(),
            application.getLocation(),
            application.getSource().name()
        );
        return envelope(
            EventTypes.APPLICATION_CREATED,
            1,
            application.getId(),
            userId,
            requestId,
            payload
        );
    }

    public DomainEventEnvelope<ApplicationStatusChangedPayload> applicationStatusChanged(
        Application application,
        ApplicationStatus previousStatus,
        String userId,
        String requestId
    ) {
        ApplicationStatusChangedPayload payload = new ApplicationStatusChangedPayload(
            application.getId(),
            previousStatus.name(),
            application.getStatus().name(),
            application.getCompanyName(),
            application.getJobTitle()
        );
        return envelope(
            EventTypes.APPLICATION_STATUS_CHANGED,
            1,
            application.getId(),
            userId,
            requestId,
            payload
        );
    }

    public DomainEventEnvelope<OfferEventPayload> offerAdded(
        Application application,
        Offer offer,
        String userId,
        String requestId
    ) {
        return offerEnvelope(EventTypes.OFFER_ADDED, application, offer, userId, requestId);
    }

    public DomainEventEnvelope<OfferEventPayload> offerUpdated(
        Application application,
        Offer offer,
        String userId,
        String requestId
    ) {
        return offerEnvelope(EventTypes.OFFER_UPDATED, application, offer, userId, requestId);
    }

    private DomainEventEnvelope<OfferEventPayload> offerEnvelope(
        String eventType,
        Application application,
        Offer offer,
        String userId,
        String requestId
    ) {
        OfferEventPayload payload = new OfferEventPayload(
            application.getId(),
            offer.getId(),
            offer.getBaseSalary() != null ? offer.getBaseSalary().toPlainString() : null,
            offer.getCurrency(),
            offer.getOfferStatus() != null ? offer.getOfferStatus().name() : null,
            offer.getJoiningDate() != null ? offer.getJoiningDate().toString() : null,
            application.getCompanyName(),
            application.getJobTitle()
        );
        return envelope(eventType, 1, application.getId(), userId, requestId, payload);
    }

    private <T> DomainEventEnvelope<T> envelope(
        String eventType,
        int eventVersion,
        UUID aggregateId,
        String userId,
        String requestId,
        T payload
    ) {
        return new DomainEventEnvelope<>(
            DomainEventEnvelope.SPEC_VERSION,
            UUID.randomUUID(),
            eventType,
            eventVersion,
            Instant.now(),
            DomainEventEnvelope.PRODUCER_APPLICATION_SERVICE,
            DomainEventEnvelope.AGGREGATE_APPLICATION,
            aggregateId,
            new EventMetadata(requestId, userId),
            payload
        );
    }
}
