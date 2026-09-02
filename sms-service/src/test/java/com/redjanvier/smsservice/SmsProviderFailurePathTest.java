package com.redjanvier.smsservice;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.redjanvier.smsservice.provider.SmsProvider;
import com.redjanvier.smsservice.provider.SmsResult;

/**
 * When the provider fails to deliver, the record must still be persisted with
 * status FAILED and the endpoint must answer 502 Bad Gateway.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SmsProviderFailurePathTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private SmsProvider smsProvider;

	@Test
	void providerFailure_persistsFailedAndReturns502() throws Exception {
		when(smsProvider.send(anyString(), anyString()))
				.thenReturn(SmsResult.failed("mock", "carrier unavailable"));

		mockMvc.perform(post("/api/v1/notifications/sms")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"to\":\"+250788000000\",\"message\":\"will fail\"}"))
				.andExpect(status().isBadGateway())
				.andExpect(jsonPath("$.status", is("FAILED")))
				.andExpect(jsonPath("$.error", is("carrier unavailable")));
	}
}
