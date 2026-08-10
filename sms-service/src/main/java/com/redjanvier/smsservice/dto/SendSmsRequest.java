package com.redjanvier.smsservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Incoming payload for POST /api/v1/notifications/sms.
 */
public class SendSmsRequest {

	@NotBlank(message = "'to' (recipient phone number) is required")
	private String to;

	@NotBlank(message = "'message' is required")
	@Size(max = 1600, message = "'message' must be at most 1600 characters")
	private String message;

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
