package com.redjanvier.smsservice.provider;

/**
 * Strategy for delivering an SMS. Implementations are selected at startup via
 * the {@code sms.provider} property (see {@link MockSmsProvider} /
 * {@link TwilioSmsProvider}), so exactly one bean is active.
 */
public interface SmsProvider {

	SmsResult send(String to, String message);

	String name();
}
