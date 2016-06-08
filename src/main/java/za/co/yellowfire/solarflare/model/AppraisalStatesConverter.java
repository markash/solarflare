package za.co.yellowfire.solarflare.model;

import org.jooq.impl.EnumConverter;

public class AppraisalStatesConverter extends EnumConverter<String, AppraisalStates> {
	private static final long serialVersionUID = -7853990002103434343L;

	public AppraisalStatesConverter() {
		super(String.class, AppraisalStates.class);
	}
}
