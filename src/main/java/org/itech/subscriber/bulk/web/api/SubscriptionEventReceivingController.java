package org.itech.subscriber.bulk.web.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.ClientProtocolException;
import org.hl7.fhir.r4.model.ResourceType;
import org.itech.subscriber.bulk.service.SubscriptionEventNotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class SubscriptionEventReceivingController {

	public static final String SUBSCRIPTION_EVENT_PATH = "/subscription/event";

	public static final String REMOTE_SERVER_ID_PARAM = "remoteServerId";
	public static final String SOURCE_RESOURCE_PATH_PARAM = "sourceResourcePath";

	private SubscriptionEventNotificationService eventNotificationService;

	public SubscriptionEventReceivingController(SubscriptionEventNotificationService eventNotificationService) {
		this.eventNotificationService = eventNotificationService;
	}

	@RequestMapping(SUBSCRIPTION_EVENT_PATH + "/**")
	public ResponseEntity<String> receiveEventForType(HttpServletRequest request,
			@RequestParam(REMOTE_SERVER_ID_PARAM) Long remoteServerId,
			@RequestParam(SOURCE_RESOURCE_PATH_PARAM) URI resourcePath)
			throws ClientProtocolException, IOException, URISyntaxException {
		// fhir server is appending the resource type to the query string, not the path,
		// so we are retrieving it from the query string in this method
		log.debug("ping received from " + request.getRemoteHost() + ":" + request.getRemotePort());
		log.debug("ping received from server reporting as id " + remoteServerId);
		if (resourcePath != null) {
			log.debug("reporting resource path " + resourcePath);
			eventNotificationService.notifyForResourceFromSource(resourcePath, remoteServerId);
		} else {
			log.debug("no resource path found");
			eventNotificationService.notifyFromSource(remoteServerId);
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(SUBSCRIPTION_EVENT_PATH + "/{resourceType}/**")
	public ResponseEntity<String> receiveEventForTypeForResource(
			@PathVariable("resourceType") ResourceType resourceType,
			@RequestParam(REMOTE_SERVER_ID_PARAM) Long remoteServerId)
			throws ClientProtocolException, IOException, URISyntaxException {
		log.debug("ping received for " + resourceType + " from server repporting as " + remoteServerId);
		eventNotificationService.notifyForResourceTypeFromSource(resourceType, remoteServerId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}