package com.redjanvier.emailservice;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Web-layer tests for the happy paths, history/read endpoints and every
 * validation error. The mail sender is mocked so no SMTP server is required.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmailServiceApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private JavaMailSender mailSender;

	@Test
	void contextLoads() {
	}

	@Test
	void send_persistsAndReturnsCreated() throws Exception {
		doNothing().when(mailSender).send(any(SimpleMailMessage.class));

		mockMvc.perform(post("/api/v1/notifications/email")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"to\":\"user@example.com\",\"subject\":\"Hi\",\"body\":\"Hello there\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", notNullValue()))
				.andExpect(jsonPath("$.status", is("SENT")))
				.andExpect(jsonPath("$.recipient", is("user@example.com")))
				.andExpect(jsonPath("$.error").doesNotExist());

		verify(mailSender).send(any(SimpleMailMessage.class));
	}

	@Test
	void history_returnsSavedRecords() throws Exception {
		doNothing().when(mailSender).send(any(SimpleMailMessage.class));

		mockMvc.perform(post("/api/v1/notifications/email")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"to\":\"a@example.com\",\"subject\":\"First\",\"body\":\"x\"}"))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/v1/notifications/email"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()", greaterThan(0)))
				.andExpect(jsonPath("$[0].subject", is("First")));
	}

	@Test
	void getById_returnsRecordWhenFound() throws Exception {
		doNothing().when(mailSender).send(any(SimpleMailMessage.class));

		String body = mockMvc.perform(post("/api/v1/notifications/email")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"to\":\"find@example.com\",\"subject\":\"Find\",\"body\":\"x\"}"))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		Integer id = com.jayway.jsonpath.JsonPath.read(body, "$.id");

		mockMvc.perform(get("/api/v1/notifications/email/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.subject", is("Find")));
	}

	@Test
	void getById_returns404WhenMissing() throws Exception {
		mockMvc.perform(get("/api/v1/notifications/email/{id}", 999999))
				.andExpect(status().isNotFound());
	}

	@Test
	void send_invalidEmail_returns400() throws Exception {
		mockMvc.perform(post("/api/v1/notifications/email")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"to\":\"not-an-email\",\"subject\":\"Hi\",\"body\":\"x\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error", is("Validation failed")))
				.andExpect(jsonPath("$.fields.to", notNullValue()));
	}

	@Test
	void send_missingSubject_returns400() throws Exception {
		mockMvc.perform(post("/api/v1/notifications/email")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"to\":\"user@example.com\",\"body\":\"x\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fields.subject", notNullValue()));
	}

	@Test
	void send_missingBody_returns400() throws Exception {
		mockMvc.perform(post("/api/v1/notifications/email")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"to\":\"user@example.com\",\"subject\":\"Hi\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fields.body", notNullValue()));
	}
}
