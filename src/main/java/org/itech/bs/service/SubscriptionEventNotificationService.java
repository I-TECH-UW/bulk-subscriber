package org.itech.bs.service;

import java.net.URI;

import org.hl7.fhir.r4.model.ResourceType;

public interface SubscriptionEventNotificationService {

	void notifyFromSource(String jsonResource, URI resourcePath, URI subscribeToUri);

	void notifyForResourceTypeFromSource(ResourceType resourceType, String jsonResource, URI subscribeToUri);

}
