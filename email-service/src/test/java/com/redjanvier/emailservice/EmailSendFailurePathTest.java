package com.redjanvier.emailservice;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * When the SMTP send throws, the record must still be persisted with status
 * FAILED and the endpoint must answer 502 Bad Gateway.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmailSendFailurePathTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private JavaMailSender mailSender;

	@Test
	void smtpFailure_persistsFailedAndReturns502() throws Exception {
		doThrow(new MailSendException("smtp unreachable"))
				.when(mailSender).send(any(SimpleMailMessage.class));

		mockMvc.perform(post("/api/v1/notifications/email")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"to\":\"user@example.com\",\"subject\":\"Hi\",\"body\":\"will fail\"}"))
				.andExpect(status().isBadGateway())
				.andExpect(jsonPath("$.status", is("FAILED")))
				.andExpect(jsonPath("$.error", is("smtp unreachable")));
	}
}
