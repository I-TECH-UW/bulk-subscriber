package org.itech.bs.service;

import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.itech.bs.data.model.BulkSubscription;
import org.itech.fhircore.service.CrudService;

public interface BulkSubscriptionService extends CrudService<BulkSubscription, Long> {

	BulkSubscription createBulkSubscriptions(Long subscribingServerId, Long subscribeToServerId,
			String subscriptionType) throws UnsupportedEncodingException;

	BulkSubscription createBulkSubscriptions(URI subscribingServerUrl, URI subscribeToServerUrl,
			String subscriptionType)
			throws UnsupportedEncodingException;

}
