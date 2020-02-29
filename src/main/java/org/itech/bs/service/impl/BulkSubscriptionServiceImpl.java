package org.itech.bs.service.impl;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.hibernate.ObjectNotFoundException;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryResponseComponent;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Narrative.NarrativeStatus;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Subscription.SubscriptionChannelComponent;
import org.hl7.fhir.r4.model.Subscription.SubscriptionChannelType;
import org.hl7.fhir.r4.model.Subscription.SubscriptionStatus;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.itech.bs.data.dao.BulkSubscriptionDAO;
import org.itech.bs.data.model.BulkSubscription;
import org.itech.bs.service.BulkSubscriptionService;
import org.itech.bs.web.api.SubscriptionEventReceivingController;
import org.itech.fhircore.model.Server;
import org.itech.fhircore.service.FhirResourceGroupService;
import org.itech.fhircore.service.ServerService;
import org.itech.fhircore.service.impl.CrudServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.PreferReturnEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BulkSubscriptionServiceImpl extends CrudServiceImpl<BulkSubscription, Long>
		implements BulkSubscriptionService {

	private ServerService serverService;
	private BulkSubscriptionDAO bulkSubscriptionDAO;
	private FhirContext fhirContext;
	private FhirResourceGroupService fhirResources;

	private Map<String, List<ResourceType>> subscriptionTypeToResourceType;

	public BulkSubscriptionServiceImpl(ServerService serverService,
			BulkSubscriptionDAO bulkSubscriptionDAO, FhirContext fhirContext,
			FhirResourceGroupService fhirResources) {
		super(bulkSubscriptionDAO);
		this.serverService = serverService;
		this.bulkSubscriptionDAO = bulkSubscriptionDAO;
		this.fhirContext = fhirContext;
		this.fhirResources = fhirResources;
	}

	@PostConstruct
	private void getFhirResources() {
		subscriptionTypeToResourceType = fhirResources.getAllFhirGroupsToResourceTypes();
	}

	@Override
	@Transactional
	public BulkSubscription createBulkSubscriptions(Long subscribingServerId, Long subscribeToServerId,
			String subscriptionType) throws UnsupportedEncodingException {
		Server subscribingServer = serverService.getDAO().findById(subscribingServerId)
				.orElseThrow(() -> new ObjectNotFoundException(subscribingServerId, Server.class.getName()));
		Server subscribeToServer = serverService.getDAO().findById(subscribeToServerId)
				.orElseThrow(() -> new ObjectNotFoundException(subscribeToServerId, Server.class.getName()));
		return createBulkSubscriptions(subscribingServer.getId(), subscribeToServer.getId(), subscriptionType);
	}

	@Override
	public BulkSubscription createBulkSubscriptions(URI subscribingServerUrl, URI subscribeToServerUrl,
			String subscriptionType) throws UnsupportedEncodingException {
		BulkSubscription bulk = new BulkSubscription();
		bulk.setSubscriberUri(subscribingServerUrl);
		bulk.setSubscribeToUri(subscribeToServerUrl);
		bulk.setSubscriptionType(subscriptionType);

		Bundle subscriptionsBundle = createSubscriptionsForType(bulk,
				subscriptionTypeToResourceType.get(subscriptionType));
		Optional<Bundle> receivedSubscriptionsBundleOpt = sendSubscriptionsToRemote(subscriptionsBundle,
				bulk.getSubscribeToUri());

		if (receivedSubscriptionsBundleOpt.isPresent()) {
			log.debug("received " + receivedSubscriptionsBundleOpt.get());
			for (int i = 0; i < subscriptionsBundle.getEntry().size(); ++i) {
				BundleEntryComponent receivedBundleEntry = receivedSubscriptionsBundleOpt.get().getEntry().get(i);
				BundleEntryComponent bundleEntry = subscriptionsBundle.getEntry().get(i);

				if (bundleEntry.getResource().getResourceType().equals(ResourceType.Subscription)) {
					Subscription subscription = (Subscription) bundleEntry.getResource();
					ResourceType resourceType = extractResourceTypeFromCriteria(subscription.getCriteria());
					bulk.putSubscription(resourceType, subscription);
					bulk.putSubscriptionSuccess(resourceType,
							subscriptionCreatedSuccess(receivedBundleEntry.getResponse()));
				} else {
					log.warn("expected a resource type of \"subscription\" but got "
							+ bundleEntry.getResource().getResourceType());
				}
			}
		}

		// outdated way of sending subscriptions one by one
//		for (ResourceType resourceType : subscriptionTypeToResourceTypes.get(subscriptionType)) {
//			Subscription subscription = createSubscriptionForType(bulk, resourceType);
//			try {
//				subscription = sendSubscriptionToRemote(subscription, bulk.getSubscribeToUri());
//				log.debug("subscription status received from " + subscribeToUri + ": "
//						+ subscription.getStatus().toCode());
//				// TODO consider querying server after a small delay to check that the status
//				// has changed from 'requested' to 'active'
//				if (!subscriptionSuccess(subscription)) {
//					log.error("could not subscribe " + subscriberUri + " to " + subscribeToUri
//							+ " for resource of type " + resourceType
//							+ ": subscription status something other than active (" + subscription.getStatus() + ")");
//				}
//			} catch (DataFormatException e) {
//				subscription.setStatus(SubscriptionStatus.ERROR);
//				log.error("could not subscribe " + subscriberUri + " to " + subscribeToUri + " for resource of type "
//						+ resourceType + ": error occured");
//			}
//			bulk.putSubscription(resourceType, subscription);
//			bulk.putSubscriptionSuccess(resourceType, subscriptionSuccess(subscription));
//		}

		log.debug("bulk before saving: " + bulk.toString());
		return bulkSubscriptionDAO.save(bulk);
	}

	private Bundle createSubscriptionsForType(BulkSubscription bulk, List<ResourceType> resourceTypes)
			throws UnsupportedEncodingException {
		Bundle subscriptionBundle = new Bundle();
		for (ResourceType resourceType : resourceTypes) {
			Subscription subscription = createSubscriptionForType(bulk, resourceType);
			BundleEntryComponent bundleEntry = new BundleEntryComponent();
			bundleEntry.setResource(subscription);
			bundleEntry.setRequest(new BundleEntryRequestComponent().setMethod(HTTPVerb.POST)
					.setUrl(ResourceType.Subscription.name()));

			subscriptionBundle.addEntry(bundleEntry);
		}
		return subscriptionBundle;
	}

	private Subscription createSubscriptionForType(BulkSubscription bulk, ResourceType resourceType)
			throws UnsupportedEncodingException {
		Subscription subscription = new Subscription();
		subscription.setText(new Narrative().setStatus(NarrativeStatus.GENERATED).setDiv(new XhtmlNode(NodeType.Text)));
		subscription.setStatus(SubscriptionStatus.REQUESTED);
		subscription
				.setContact(Arrays.asList(new ContactPoint().setSystem(ContactPointSystem.PHONE).setValue("ext 4123")));
		subscription.setReason("bulk synchronization");

		SubscriptionChannelComponent channel = new SubscriptionChannelComponent();
		// TODO remove hardcoded endpoint (this server)
		channel.setType(SubscriptionChannelType.RESTHOOK).setEndpoint(
				"http://as-1:8080/subscription/event?" + SubscriptionEventReceivingController.SOURCE_URI_PARAM + "="
						+ URLEncoder.encode(bulk.getSubscribeToUri().toString(), "UTF-8") + "&"
						// when the server responds it appends it's path to the end of the endpoint
						// string, so this parameter will be filled in by the responding server
						+ SubscriptionEventReceivingController.SOURCE_RESOURCE_PATH_PARAM + "=");
		channel.setPayload("application/json");
		subscription.setChannel(channel);

		subscription.setCriteria(resourceType.name() + "?");
		return subscription;

	}

	private Optional<Bundle> sendSubscriptionsToRemote(Bundle subscriptionsBundle, URI subscribeToUri) {
		log.debug("sending " + subscriptionsBundle + " to " + subscribeToUri);

		try {
			IGenericClient fhirClient = fhirContext
					.newRestfulGenericClient(new URI(subscribeToUri.getScheme(), null, subscribeToUri.getHost(),
							subscribeToUri.getPort(), subscribeToUri.getPath(), null, null).toString());
			try {
				Bundle returnedBundle = fhirClient.transaction().withBundle(subscriptionsBundle).encodedJson()
						.execute();
				return Optional.of(returnedBundle);
			} catch (UnprocessableEntityException | DataFormatException e) {
				log.error("error while communicating subscription bundle to " + subscribeToUri + " for "
						+ subscriptionsBundle, e);
			}
		} catch (URISyntaxException e) {
			log.error("error while creating uri for " + subscribeToUri, e);
		}
		return Optional.empty();
	}

	@SuppressWarnings("unused")
	// used for sending one by one. Replaced by sending by bundle
	private Subscription sendSubscriptionToRemote(Subscription subscription, URI subscribeToUri) {
		log.debug("sending " + subscription + " to " + subscribeToUri);
		try {
			IGenericClient fhirClient = fhirContext
					.newRestfulGenericClient(new URI(subscribeToUri.getScheme(), null, subscribeToUri.getHost(),
							subscribeToUri.getPort(), subscribeToUri.getPath(), null, null).toString());
			try {
				MethodOutcome outcome = fhirClient.create().resource(subscription)
						.prefer(PreferReturnEnum.REPRESENTATION).encodedJson().execute();
				if (outcome.getCreated()) {
					return (Subscription) outcome.getResource();
				} else {
					log.error("received " + outcome + " while communicating with " + subscribeToUri
							+ " for subscription request ");
				}
			} catch (UnprocessableEntityException | DataFormatException e) {
				log.error(
						"error while communicating subscription request to " + subscribeToUri + " for " + subscription,
						e);
			}
		} catch (URISyntaxException e) {
			log.error("error while creating uri for " + subscribeToUri, e);
		}
		return subscription;
	}

	private ResourceType extractResourceTypeFromCriteria(String criteria) {
		return ResourceType.valueOf(criteria.substring(0, criteria.indexOf("?")));
	}

	@SuppressWarnings("unused")
	private boolean subscriptionSuccess(Subscription subscription) {
		return subscription.getStatus().equals(SubscriptionStatus.ACTIVE)
				|| subscription.getStatus().equals(SubscriptionStatus.REQUESTED);
	}

	private boolean subscriptionCreatedSuccess(BundleEntryResponseComponent response) {
		return response.getStatus().contains("201");
	}

}
