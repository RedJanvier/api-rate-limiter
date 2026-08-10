package com.redjanvier.smsservice.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.redjanvier.smsservice.entity.NotificationStatus;
import com.redjanvier.smsservice.entity.SmsNotification;
import com.redjanvier.smsservice.provider.SmsProvider;
import com.redjanvier.smsservice.provider.SmsResult;
import com.redjanvier.smsservice.repository.SmsNotificationRepository;

@Service
public class SmsService {

	private final SmsProvider provider;
	private final SmsNotificationRepository repository;

	public SmsService(SmsProvider provider, SmsNotificationRepository repository) {
		this.provider = provider;
		this.repository = repository;
	}

	/**
	 * Sends an SMS through the active provider and persists the outcome
	 * (whether it succeeded or failed) as a notification record.
	 */
	public SmsNotification send(String to, String message) {
		SmsResult result = provider.send(to, message);
		NotificationStatus status = result.success() ? NotificationStatus.SENT : NotificationStatus.FAILED;
		SmsNotification record = new SmsNotification(to, message, status, result.provider(), result.error());
		return repository.save(record);
	}

	public List<SmsNotification> history() {
		return repository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
	}

	public SmsNotification findById(Long id) {
		return repository.findById(id).orElse(null);
	}
}
