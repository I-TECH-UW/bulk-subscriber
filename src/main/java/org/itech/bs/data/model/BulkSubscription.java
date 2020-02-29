package org.itech.bs.data.model;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Transient;

import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Subscription;
import org.itech.fhircore.model.base.PersistenceEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class BulkSubscription extends PersistenceEntity<Long> {

	private String subscriptionType;

	// TODO allow multiple subscriber in the future maybe?
	private URI subscriberUri;

	private URI subscribeToUri;

	@ElementCollection
	@MapKeyColumn(name = "resource_type")
	@Column(name = "subscription_json", length = 65535)
	@CollectionTable(name = "subscriptions", joinColumns = @JoinColumn(name = "bulk_subscription_id"))
	@JsonIgnore
	private Map<ResourceType, Subscription> subscriptions;
	@Transient
	private Map<ResourceType, Boolean> subscriptionSuccesses;


	public void putSubscription(ResourceType type, Subscription subscription) {
		if (subscriptions == null) {
			subscriptions = new HashMap<>();
		}
		subscriptions.put(type, subscription);
	}


	public void putSubscriptionSuccess(ResourceType type, boolean success) {
		if (subscriptionSuccesses == null) {
			subscriptionSuccesses = new HashMap<>();
		}
		subscriptionSuccesses.put(type, success);

	}

}
