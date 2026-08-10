package com.redjanvier.smsservice.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Default provider. "Sends" an SMS by logging it — no external account needed,
 * so the service is fully functional out of the box. Active unless
 * {@code sms.provider=twilio}.
 */
@Component
@ConditionalOnProperty(name = "sms.provider", havingValue = "mock", matchIfMissing = true)
public class MockSmsProvider implements SmsProvider {

	private static final Logger log = LoggerFactory.getLogger(MockSmsProvider.class);

	@Override
	public SmsResult send(String to, String message) {
		log.info("[MOCK SMS] -> to='{}' message='{}'", to, message);
		return SmsResult.ok(name());
	}

	@Override
	public String name() {
		return "mock";
	}
}
