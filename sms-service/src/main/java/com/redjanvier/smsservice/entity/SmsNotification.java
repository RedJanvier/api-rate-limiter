package com.redjanvier.smsservice.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sms_notifications")
public class SmsNotification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String recipient;

	@Column(nullable = false, columnDefinition = "text")
	private String message;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private NotificationStatus status;

	@Column(nullable = false, length = 32)
	private String provider;

	@Column(columnDefinition = "text")
	private String error;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	protected SmsNotification() {
		// for JPA
	}

	public SmsNotification(String recipient, String message, NotificationStatus status, String provider, String error) {
		this.recipient = recipient;
		this.message = message;
		this.status = status;
		this.provider = provider;
		this.error = error;
	}

	public Long getId() {
		return id;
	}

	public String getRecipient() {
		return recipient;
	}

	public String getMessage() {
		return message;
	}

	public NotificationStatus getStatus() {
		return status;
	}

	public String getProvider() {
		return provider;
	}

	public String getError() {
		return error;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
