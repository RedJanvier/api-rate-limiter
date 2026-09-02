package com.redjanvier.smsservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.redjanvier.smsservice.entity.NotificationStatus;
import com.redjanvier.smsservice.entity.SmsNotification;
import com.redjanvier.smsservice.provider.SmsProvider;
import com.redjanvier.smsservice.provider.SmsResult;
import com.redjanvier.smsservice.repository.SmsNotificationRepository;

/**
 * Pure unit tests for the provider-result -> entity mapping (no Spring context).
 */
@ExtendWith(MockitoExtension.class)
class SmsServiceUnitTest {

	@Mock
	private SmsProvider provider;

	@Mock
	private SmsNotificationRepository repository;

	@InjectMocks
	private SmsService smsService;

	@Test
	void send_successfulProvider_isPersistedAsSent() {
		when(provider.send("+250788000000", "hi")).thenReturn(SmsResult.ok("mock"));
		when(repository.save(any(SmsNotification.class))).thenAnswer(inv -> inv.getArgument(0));

		SmsNotification result = smsService.send("+250788000000", "hi");

		assertThat(result.getStatus()).isEqualTo(NotificationStatus.SENT);
		assertThat(result.getProvider()).isEqualTo("mock");
		assertThat(result.getError()).isNull();
		verify(repository).save(any(SmsNotification.class));
	}

	@Test
	void send_failingProvider_isPersistedAsFailedWithError() {
		when(provider.send("+250788000000", "hi")).thenReturn(SmsResult.failed("twilio", "boom"));
		when(repository.save(any(SmsNotification.class))).thenAnswer(inv -> inv.getArgument(0));

		SmsNotification result = smsService.send("+250788000000", "hi");

		assertThat(result.getStatus()).isEqualTo(NotificationStatus.FAILED);
		assertThat(result.getProvider()).isEqualTo("twilio");
		assertThat(result.getError()).isEqualTo("boom");
	}
}
