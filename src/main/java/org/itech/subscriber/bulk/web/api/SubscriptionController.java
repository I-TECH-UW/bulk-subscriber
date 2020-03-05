package org.itech.subscriber.bulk.web.api;

import java.io.UnsupportedEncodingException;

import org.itech.fhircore.model.Server;
import org.itech.subscriber.bulk.data.model.BulkSubscription;
import org.itech.subscriber.bulk.service.BulkSubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Server.SERVER_PATH + "/{serverId}/subscription/{subscriptionType}")
public class SubscriptionController {

	private BulkSubscriptionService subscriptionService;

	public SubscriptionController(BulkSubscriptionService subscriptionService) {
		this.subscriptionService = subscriptionService;
	}

	@GetMapping
	public ResponseEntity<Iterable<BulkSubscription>> getAllSubscriptions() throws UnsupportedEncodingException {
		return new ResponseEntity<>(subscriptionService.getDAO().findAll(), HttpStatus.CREATED);
	}

	@GetMapping("/{bulkId}")
	public ResponseEntity<BulkSubscription> getSubscription(@PathVariable("bulkId") Long bulkId)
			throws UnsupportedEncodingException {
		return new ResponseEntity<>(subscriptionService.getDAO().findById(bulkId).get(), HttpStatus.CREATED);
	}


	@PostMapping
	public ResponseEntity<BulkSubscription> createSubscription(@PathVariable("serverId") Long serverId,
			@PathVariable("subscriptionType") String subscriptionType) throws UnsupportedEncodingException {
		return new ResponseEntity<>(subscriptionService.createBulkSubscriptions(serverId, subscriptionType),
				HttpStatus.CREATED);
	}

}
