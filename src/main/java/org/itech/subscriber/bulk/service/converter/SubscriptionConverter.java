package org.itech.subscriber.bulk.service.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.hl7.fhir.r4.model.Subscription;

import ca.uhn.fhir.context.FhirContext;

@Converter(autoApply = true)
public class SubscriptionConverter implements AttributeConverter<Subscription, String> {

	FhirContext fhirContext;

	public SubscriptionConverter(FhirContext fhirContext) {
		this.fhirContext = fhirContext;
	}

	@Override
	public String convertToDatabaseColumn(Subscription attribute) {
		return fhirContext.newJsonParser().encodeResourceToString(attribute);
	}

	@Override
	public Subscription convertToEntityAttribute(String dbData) {
		return fhirContext.newJsonParser().parseResource(Subscription.class, dbData);
	}

}
