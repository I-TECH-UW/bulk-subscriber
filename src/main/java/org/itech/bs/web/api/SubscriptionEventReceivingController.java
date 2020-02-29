package org.itech.bs.web.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.ClientProtocolException;
import org.hl7.fhir.r4.model.ResourceType;
import org.itech.bs.service.SubscriptionEventNotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class SubscriptionEventReceivingController {

	public static final String SUBSCRIPTION_EVENT_PATH = "/subscription/event";

	public static final String SOURCE_URI_PARAM = "sourceURI";
	public static final String SOURCE_RESOURCE_PATH_PARAM = "sourceResourcePath";

	private SubscriptionEventNotificationService eventNotificationService;

	public SubscriptionEventReceivingController(SubscriptionEventNotificationService eventNotificationService) {
		this.eventNotificationService = eventNotificationService;
	}


	@RequestMapping(SUBSCRIPTION_EVENT_PATH + "/**")
	public ResponseEntity<String> receiveEventForType(HttpServletRequest request, @RequestBody String requestBody,
			@RequestParam(SOURCE_URI_PARAM) URI sourceUri, @RequestParam(SOURCE_RESOURCE_PATH_PARAM) URI resourcePath)
			throws ClientProtocolException, IOException, URISyntaxException {
		log.debug("ping received from " + request.getRemoteHost() + ":" + request.getRemotePort());
		log.debug("reporting ping received from " + sourceUri);
		log.debug("reporting resource path " + resourcePath);
		// fhir server is appending to our provided query string, not the path, so we
		// must correct it here

		eventNotificationService.notifyFromSource(requestBody, resourcePath, sourceUri);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(SUBSCRIPTION_EVENT_PATH + "/{resourceType}/**")
	public ResponseEntity<String> receiveEventForTypeForResource(
			@PathVariable("resourceType") ResourceType resourceType,
			@RequestBody String requestBody, @RequestParam(SOURCE_URI_PARAM) URI sourceUri)
			throws ClientProtocolException, IOException, URISyntaxException {
		log.debug("ping received for " + resourceType + " from " + sourceUri);
		eventNotificationService.notifyForResourceTypeFromSource(resourceType, requestBody, sourceUri);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}