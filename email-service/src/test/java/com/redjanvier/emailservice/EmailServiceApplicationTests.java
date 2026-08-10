package com.redjanvier.emailservice;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;
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

@SpringBootTest
@AutoConfigureMockMvc
class EmailServiceApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	// Mock the mail sender so tests don't need a live SMTP server.
	@MockBean
	private JavaMailSender mailSender;

	@Test
	void contextLoads() {
	}

	@Test
	void sendPersistsAndReturnsCreated() throws Exception {
		doNothing().when(mailSender).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));

		mockMvc.perform(post("/api/v1/notifications/email")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"to\":\"user@example.com\",\"subject\":\"Hi\",\"body\":\"Hello there\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status", is("SENT")))
				.andExpect(jsonPath("$.recipient", is("user@example.com")));

		mockMvc.perform(get("/api/v1/notifications/email"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].subject", is("Hi")));
	}

	@Test
	void invalidEmailReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/v1/notifications/email")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"to\":\"not-an-email\",\"subject\":\"Hi\",\"body\":\"x\"}"))
				.andExpect(status().isBadRequest());
	}
}
