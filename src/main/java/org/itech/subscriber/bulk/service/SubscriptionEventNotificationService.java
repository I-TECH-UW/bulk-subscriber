package org.itech.subscriber.bulk.service;

import java.net.URI;

import org.hl7.fhir.r4.model.ResourceType;

public interface SubscriptionEventNotificationService {

	void notifyForResourceFromSource(URI resourcePath, Long remoteServerId);

	void notifyForResourceTypeFromSource(ResourceType resourceType, Long remoteServerId);

	void notifyFromSource(Long remoteServerId);

}
