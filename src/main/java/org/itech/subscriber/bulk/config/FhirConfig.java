package org.itech.subscriber.bulk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;

@Configuration
public class FhirConfig {

	@Bean
	public FhirContext fhirContext() {
		return new FhirContext(FhirVersionEnum.R4);
	}

}
