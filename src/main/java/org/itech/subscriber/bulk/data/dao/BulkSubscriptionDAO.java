package org.itech.subscriber.bulk.data.dao;

import java.util.List;

import org.hl7.fhir.r4.model.ResourceType;
import org.itech.subscriber.bulk.data.model.BulkSubscription;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BulkSubscriptionDAO extends CrudRepository<BulkSubscription, Long> {

	@Query("SELECT bulk FROM BulkSubscription bulk JOIN bulk.subscriptions s WHERE KEY(s) = :resourceType")
	List<BulkSubscription> findSubscriptionsWithResourceType(@Param("resourceType") ResourceType resourceType);

	@Query("SELECT bulk FROM BulkSubscription bulk JOIN bulk.subscriptions s WHERE KEY(s) = :resourceType AND bulk.remoteServer.id = :remoteServerId")
	List<BulkSubscription> findSubscriptionsWithResourceTypeAndSubscribeToUri(ResourceType resourceType,
			@Param("remoteServerId") Long remoteServerId);

	@Query("SELECT bulk FROM BulkSubscription bulk JOIN bulk.subscriptions s WHERE bulk.remoteServer.id = :remoteServerId")
	List<BulkSubscription> findSubscriptionsSubscribeToUri(@Param("remoteServerId") Long remoteServerId);

}
