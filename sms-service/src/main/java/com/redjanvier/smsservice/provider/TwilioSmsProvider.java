package com.redjanvier.smsservice.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import jakarta.annotation.PostConstruct;

/**
 * Real SMS delivery via Twilio. Activated with {@code sms.provider=twilio} and
 * the {@code TWILIO_ACCOUNT_SID}, {@code TWILIO_AUTH_TOKEN}, and
 * {@code TWILIO_FROM_NUMBER} environment variables.
 */
@Component
@ConditionalOnProperty(name = "sms.provider", havingValue = "twilio")
public class TwilioSmsProvider implements SmsProvider {

	private static final Logger log = LoggerFactory.getLogger(TwilioSmsProvider.class);

	private final String accountSid;
	private final String authToken;
	private final String fromNumber;

	public TwilioSmsProvider(
			@Value("${twilio.account-sid:}") String accountSid,
			@Value("${twilio.auth-token:}") String authToken,
			@Value("${twilio.from-number:}") String fromNumber) {
		this.accountSid = accountSid;
		this.authToken = authToken;
		this.fromNumber = fromNumber;
	}

	@PostConstruct
	void init() {
		if (accountSid.isBlank() || authToken.isBlank() || fromNumber.isBlank()) {
			throw new IllegalStateException(
					"sms.provider=twilio requires twilio.account-sid, twilio.auth-token and twilio.from-number");
		}
		Twilio.init(accountSid, authToken);
		log.info("Twilio SMS provider initialized (from={})", fromNumber);
	}

	@Override
	public SmsResult send(String to, String message) {
		try {
			Message created = Message
					.creator(new PhoneNumber(to), new PhoneNumber(fromNumber), message)
					.create();
			log.info("Twilio accepted SMS sid={}", created.getSid());
			return SmsResult.ok(name());
		} catch (Exception e) {
			log.error("Twilio SMS send failed: {}", e.getMessage());
			return SmsResult.failed(name(), e.getMessage());
		}
	}

	@Override
	public String name() {
		return "twilio";
	}
}
