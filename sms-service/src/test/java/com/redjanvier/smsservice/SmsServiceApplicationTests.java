package com.redjanvier.smsservice;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Web-layer tests for the happy paths, history/read endpoints and every
 * validation error. Each test rolls back so the in-memory DB stays clean.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SmsServiceApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void send_persistsAndReturnsCreated() throws Exception {
		mockMvc.perform(post("/api/v1/notifications/sms")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"to\":\"+250788000000\",\"message\":\"hello\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", notNullValue()))
				.andExpect(jsonPath("$.status", is("SENT")))
				.andExpect(jsonPath("$.provider", is("mock")))
				.andExpect(jsonPath("$.recipient", is("+250788000000")))
				.andExpect(jsonPath("$.error").doesNotExist());
	}

	@Test
	void history_returnsSavedRecords() throws Exception {
		mockMvc.perform(post("/api/v1/notifications/sms")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"to\":\"+250788000001\",\"message\":\"first\"}"))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/v1/notifications/sms"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()", greaterThan(0)))
				.andExpect(jsonPath("$[0].message", is("first")));
	}

	@Test
	void getById_returnsRecordWhenFound() throws Exception {
		String body = mockMvc.perform(post("/api/v1/notifications/sms")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"to\":\"+250788000002\",\"message\":\"findme\"}"))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		Integer id = com.jayway.jsonpath.JsonPath.read(body, "$.id");

		mockMvc.perform(get("/api/v1/notifications/sms/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message", is("findme")));
	}

	@Test
	void getById_returns404WhenMissing() throws Exception {
		mockMvc.perform(get("/api/v1/notifications/sms/{id}", 999999))
				.andExpect(status().isNotFound());
	}

	@Test
	void send_missingRecipient_returns400() throws Exception {
		mockMvc.perform(post("/api/v1/notifications/sms")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"message\":\"no recipient\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error", is("Validation failed")))
				.andExpect(jsonPath("$.fields.to", notNullValue()));
	}

	@Test
	void send_missingMessage_returns400() throws Exception {
		mockMvc.perform(post("/api/v1/notifications/sms")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"to\":\"+250788000000\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fields.message", notNullValue()));
	}

	@Test
	void send_blankFields_returns400() throws Exception {
		mockMvc.perform(post("/api/v1/notifications/sms")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"to\":\"\",\"message\":\"\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fields.to", notNullValue()))
				.andExpect(jsonPath("$.fields.message", notNullValue()));
	}

	@Test
	void send_messageTooLong_returns400() throws Exception {
		String longMessage = "x".repeat(1601);
		mockMvc.perform(post("/api/v1/notifications/sms")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"to\":\"+250788000000\",\"message\":\"" + longMessage + "\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fields.message", notNullValue()));
	}
}
