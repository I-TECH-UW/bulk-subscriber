package org.itech.bs.web.api;

import java.io.UnsupportedEncodingException;

import org.itech.bs.data.model.BulkSubscription;
import org.itech.bs.service.BulkSubscriptionService;
import org.itech.bs.web.api.dto.SubscriptionByServerIdDTO;
import org.itech.bs.web.api.dto.SubscriptionByUrlDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subscription")
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
	public ResponseEntity<BulkSubscription> createSubscription(@RequestBody SubscriptionByUrlDTO subscribeDTO)
			throws UnsupportedEncodingException {
		return new ResponseEntity<>(
				subscriptionService.createBulkSubscriptions(subscribeDTO.getSubscribingServerUrl(),
						subscribeDTO.getSubscribeToServerUrl(), subscribeDTO.getSubscriptionType()),
				HttpStatus.CREATED);
	}

	@PostMapping("/server")
	public ResponseEntity<BulkSubscription> createSubscription(@RequestBody SubscriptionByServerIdDTO subscribeDTO)
			throws UnsupportedEncodingException {
		return new ResponseEntity<>(
				subscriptionService.createBulkSubscriptions(subscribeDTO.getSubscribingServerId(),
						subscribeDTO.getSubscribeToServerId(), subscribeDTO.getSubscriptionType()),
				HttpStatus.CREATED);
	}

}
