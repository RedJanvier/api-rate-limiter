package com.redjanvier.smsservice.provider;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MockSmsProviderTest {

	private final MockSmsProvider provider = new MockSmsProvider();

	@Test
	void send_alwaysSucceedsWithMockProviderName() {
		SmsResult result = provider.send("+250788000000", "hello");

		assertThat(result.success()).isTrue();
		assertThat(result.provider()).isEqualTo("mock");
		assertThat(result.error()).isNull();
	}

	@Test
	void name_isMock() {
		assertThat(provider.name()).isEqualTo("mock");
	}
}
