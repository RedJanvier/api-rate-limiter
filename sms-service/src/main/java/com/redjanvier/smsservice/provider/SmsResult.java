package com.redjanvier.smsservice.provider;

/**
 * Outcome of a provider send attempt.
 *
 * @param success   whether the message was accepted by the provider
 * @param provider  the provider that handled the send (e.g. "mock", "twilio")
 * @param error     failure detail when {@code success} is false, otherwise null
 */
public record SmsResult(boolean success, String provider, String error) {

	public static SmsResult ok(String provider) {
		return new SmsResult(true, provider, null);
	}

	public static SmsResult failed(String provider, String error) {
		return new SmsResult(false, provider, error);
	}
}
