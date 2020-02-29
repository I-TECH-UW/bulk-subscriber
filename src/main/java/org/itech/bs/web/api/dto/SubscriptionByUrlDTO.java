package org.itech.bs.web.api.dto;

import java.net.URI;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SubscriptionByUrlDTO extends SubscriptionDTO {

	private URI subscribingServerUrl;

	private URI subscribeToServerUrl;

}
