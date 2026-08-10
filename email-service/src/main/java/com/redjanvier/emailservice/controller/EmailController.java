package com.redjanvier.emailservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.redjanvier.emailservice.dto.SendEmailRequest;
import com.redjanvier.emailservice.entity.EmailNotification;
import com.redjanvier.emailservice.entity.NotificationStatus;
import com.redjanvier.emailservice.service.EmailService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/notifications/email")
public class EmailController {

	private final EmailService emailService;

	public EmailController(EmailService emailService) {
		this.emailService = emailService;
	}

	@PostMapping
	public ResponseEntity<EmailNotification> send(@Valid @RequestBody SendEmailRequest request) {
		EmailNotification record = emailService.send(request.getTo(), request.getSubject(), request.getBody());
		HttpStatus status = record.getStatus() == NotificationStatus.SENT
				? HttpStatus.CREATED
				: HttpStatus.BAD_GATEWAY;
		return ResponseEntity.status(status).body(record);
	}

	@GetMapping
	public List<EmailNotification> history() {
		return emailService.history();
	}

	@GetMapping("/{id}")
	public ResponseEntity<EmailNotification> byId(@PathVariable Long id) {
		EmailNotification record = emailService.findById(id);
		return record == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(record);
	}
}
