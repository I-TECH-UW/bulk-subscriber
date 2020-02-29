package org.itech.bs.data.dao;

import java.net.URI;
import java.util.List;

import org.hl7.fhir.r4.model.ResourceType;
import org.itech.bs.data.model.BulkSubscription;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BulkSubscriptionDAO extends CrudRepository<BulkSubscription, Long> {

	@Query("SELECT bulk FROM BulkSubscription bulk JOIN bulk.subscriptions s WHERE KEY(s) = :resourceType")
	List<BulkSubscription> findSubscriptionsWithResourceType(@Param("resourceType") ResourceType resourceType);

	@Query("SELECT bulk FROM BulkSubscription bulk JOIN bulk.subscriptions s WHERE KEY(s) = :resourceType AND bulk.subscribeToUri = :subscribeToUri")
	List<BulkSubscription> findSubscriptionsWithResourceTypeAndSubscribeToUri(ResourceType resourceType,
			@Param("subscribeToUri") URI subscribeToUri);

}
