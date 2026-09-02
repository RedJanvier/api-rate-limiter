package com.redjanvier.emailservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.redjanvier.emailservice.entity.EmailNotification;
import com.redjanvier.emailservice.entity.NotificationStatus;
import com.redjanvier.emailservice.repository.EmailNotificationRepository;

/**
 * Pure unit tests for the send -> persist mapping (no Spring context).
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceUnitTest {

	@Mock
	private JavaMailSender mailSender;

	@Mock
	private EmailNotificationRepository repository;

	private EmailService emailService;

	@BeforeEach
	void setUp() {
		emailService = new EmailService(mailSender, repository, "no-reply@test.local");
		when(repository.save(any(EmailNotification.class))).thenAnswer(inv -> inv.getArgument(0));
	}

	@Test
	void send_successful_isPersistedAsSent() {
		EmailNotification result = emailService.send("user@example.com", "Subject", "Body");

		assertThat(result.getStatus()).isEqualTo(NotificationStatus.SENT);
		assertThat(result.getError()).isNull();
		verify(mailSender).send(any(SimpleMailMessage.class));
	}

	@Test
	void send_whenSmtpThrows_isPersistedAsFailed() {
		doThrow(new MailSendException("down")).when(mailSender).send(any(SimpleMailMessage.class));

		EmailNotification result = emailService.send("user@example.com", "Subject", "Body");

		assertThat(result.getStatus()).isEqualTo(NotificationStatus.FAILED);
		assertThat(result.getError()).isEqualTo("down");
	}
}
