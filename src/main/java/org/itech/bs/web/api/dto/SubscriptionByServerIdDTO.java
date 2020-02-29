package org.itech.bs.web.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SubscriptionByServerIdDTO extends SubscriptionDTO {

	private Long subscribingServerId;

	private Long subscribeToServerId;

}
