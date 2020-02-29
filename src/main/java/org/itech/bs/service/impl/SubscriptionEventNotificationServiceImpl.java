package org.itech.bs.service.impl;

import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;

import org.hl7.fhir.r4.model.ResourceType;
import org.itech.bs.data.dao.BulkSubscriptionDAO;
import org.itech.bs.data.model.BulkSubscription;
import org.itech.bs.service.SubscriptionEventNotificationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SubscriptionEventNotificationServiceImpl implements SubscriptionEventNotificationService {

	private FhirContext fhirContext;
	private BulkSubscriptionDAO bulkSubscriptionDAO;

	public SubscriptionEventNotificationServiceImpl(FhirContext fhirContext,
			BulkSubscriptionDAO bulkSubscriptionDAO) {
		this.fhirContext = fhirContext;
		this.bulkSubscriptionDAO = bulkSubscriptionDAO;
	}

	@Override
	@Async
	public void notifyFromSource(String jsonResource, URI resourcePath, URI subscribeToUri) {
		ResourceType resourceType = ResourceType.valueOf(getResourceType(jsonResource));
		notifyForResourceTypeFromSource(resourceType, jsonResource, subscribeToUri);
	}

	@SuppressWarnings("unused")
	private String getResourceType(URI resourcePath) {
		return Pattern.compile("\\/([a-zA-Z]+)\\/.*").matcher(resourcePath.toString()).group(1);
	}

	private String getResourceType(String jsonResource) {
		return fhirContext.newJsonParser().parseResource(jsonResource).fhirType();
	}

	@Override
	@Async
	public void notifyForResourceTypeFromSource(ResourceType resourceType, String jsonResource, URI subscribeToUri) {
		List<BulkSubscription> bulks = bulkSubscriptionDAO
				.findSubscriptionsWithResourceTypeAndSubscribeToUri(resourceType, subscribeToUri);
		for (BulkSubscription bulk : bulks) {
			notifySubscriberOfResourceType(resourceType, bulk, jsonResource);
		}
	}

	private void notifySubscriberOfResourceType(ResourceType resourceType, BulkSubscription bulk,
			String jsonResource) {
		URI baseSubscriberUri = bulk.getSubscriberUri();
		log.debug("notifying subscriber " + baseSubscriberUri + " of " + resourceType);
		IGenericClient client = fhirContext.newRestfulGenericClient(baseSubscriberUri.toString());

		try {
			boolean success = true;
			if (newServerResource(jsonResource, client)) {
				MethodOutcome outcome = client.create().resource(jsonResource).execute();
				success = outcome.getCreated();
			} else {
				MethodOutcome outcome = client.update().resource(jsonResource).execute();
				success = null != outcome.getResource();
			}
			if (!success) {
				log.error("could not communicate " + jsonResource + " to " + baseSubscriberUri);
			}
			// check if we can find tighter possible exception
		} catch (RuntimeException e) {
			log.error("could not communicate " + jsonResource + " to " + baseSubscriberUri);
		}

	}

	// TODO make this more robust. Maybe query baseSubscriberUri to see if it has
	// the resource already
	private boolean newServerResource(String jsonResource, IGenericClient client) {
		return fhirContext.newJsonParser().parseResource(jsonResource).getMeta().getVersionId().equals("1");
	}

}
