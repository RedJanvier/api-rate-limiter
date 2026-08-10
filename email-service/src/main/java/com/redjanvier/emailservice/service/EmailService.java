package com.redjanvier.emailservice.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.redjanvier.emailservice.entity.EmailNotification;
import com.redjanvier.emailservice.entity.NotificationStatus;
import com.redjanvier.emailservice.repository.EmailNotificationRepository;

@Service
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	private final JavaMailSender mailSender;
	private final EmailNotificationRepository repository;
	private final String fromAddress;

	public EmailService(JavaMailSender mailSender,
			EmailNotificationRepository repository,
			@Value("${email.from:no-reply@rate-limiter.local}") String fromAddress) {
		this.mailSender = mailSender;
		this.repository = repository;
		this.fromAddress = fromAddress;
	}

	/**
	 * Sends an email through the configured SMTP server (Mailpit locally) and
	 * persists the outcome as a notification record.
	 */
	public EmailNotification send(String to, String subject, String body) {
		NotificationStatus status;
		String error = null;
		try {
			SimpleMailMessage mail = new SimpleMailMessage();
			mail.setFrom(fromAddress);
			mail.setTo(to);
			mail.setSubject(subject);
			mail.setText(body);
			mailSender.send(mail);
			status = NotificationStatus.SENT;
			log.info("Email sent to '{}' subject='{}'", to, subject);
		} catch (Exception e) {
			status = NotificationStatus.FAILED;
			error = e.getMessage();
			log.error("Email send to '{}' failed: {}", to, error);
		}
		return repository.save(new EmailNotification(to, subject, body, status, error));
	}

	public List<EmailNotification> history() {
		return repository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
	}

	public EmailNotification findById(Long id) {
		return repository.findById(id).orElse(null);
	}
}
