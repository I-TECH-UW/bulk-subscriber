package org.itech.subscriber.bulk.service;

import java.io.UnsupportedEncodingException;

import org.itech.fhircore.model.Server;
import org.itech.fhircore.service.CrudService;
import org.itech.subscriber.bulk.data.model.BulkSubscription;

public interface BulkSubscriptionService extends CrudService<BulkSubscription, Long> {

	BulkSubscription createBulkSubscriptions(Long subscribeToServerId, String subscriptionType)
			throws UnsupportedEncodingException;

	BulkSubscription createBulkSubscriptions(Server remoteServer, String subscriptionType)
			throws UnsupportedEncodingException;

}
