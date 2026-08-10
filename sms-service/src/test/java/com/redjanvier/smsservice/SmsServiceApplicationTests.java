package com.redjanvier.smsservice;

import static org.hamcrest.Matchers.is;
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

@SpringBootTest
@AutoConfigureMockMvc
class SmsServiceApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void sendPersistsAndReturnsCreated() throws Exception {
		mockMvc.perform(post("/api/v1/notifications/sms")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"to\":\"+250788000000\",\"message\":\"hello\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status", is("SENT")))
				.andExpect(jsonPath("$.provider", is("mock")))
				.andExpect(jsonPath("$.recipient", is("+250788000000")));

		mockMvc.perform(get("/api/v1/notifications/sms"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].message", is("hello")));
	}

	@Test
	void missingFieldsReturnBadRequest() throws Exception {
		mockMvc.perform(post("/api/v1/notifications/sms")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"to\":\"\"}"))
				.andExpect(status().isBadRequest());
	}
}
