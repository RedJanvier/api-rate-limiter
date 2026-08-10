package com.redjanvier.smsservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.redjanvier.smsservice.dto.SendSmsRequest;
import com.redjanvier.smsservice.entity.NotificationStatus;
import com.redjanvier.smsservice.entity.SmsNotification;
import com.redjanvier.smsservice.service.SmsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/notifications/sms")
public class SmsController {

	private final SmsService smsService;

	public SmsController(SmsService smsService) {
		this.smsService = smsService;
	}

	@PostMapping
	public ResponseEntity<SmsNotification> send(@Valid @RequestBody SendSmsRequest request) {
		SmsNotification record = smsService.send(request.getTo(), request.getMessage());
		HttpStatus status = record.getStatus() == NotificationStatus.SENT
				? HttpStatus.CREATED
				: HttpStatus.BAD_GATEWAY;
		return ResponseEntity.status(status).body(record);
	}

	@GetMapping
	public List<SmsNotification> history() {
		return smsService.history();
	}

	@GetMapping("/{id}")
	public ResponseEntity<SmsNotification> byId(@PathVariable Long id) {
		SmsNotification record = smsService.findById(id);
		return record == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(record);
	}
}
