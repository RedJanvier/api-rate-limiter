package com.redjanvier.apigateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import redis.embedded.RedisServer;

/**
 * End-to-end test of the gateway's two headline responsibilities:
 * <ol>
 *   <li>routing a request to the correct downstream service, and</li>
 *   <li>enforcing the Redis-backed rate limit (HTTP 429 once the bucket drains).</li>
 * </ol>
 *
 * <p>Uses a real, in-process Redis (embedded-redis, no Docker) and a stubbed
 * downstream (MockWebServer), so it exercises the actual {@code RequestRateLimiter}
 * Lua script and runs anywhere — locally and in CI.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class RateLimitingIntegrationTest {

	static RedisServer redisServer;
	static MockWebServer downstream;

	@DynamicPropertySource
	static void properties(DynamicPropertyRegistry registry) throws IOException {
		int redisPort = findFreePort();
		redisServer = new RedisServer(redisPort);
		redisServer.start();

		downstream = new MockWebServer();
		downstream.setDispatcher(new Dispatcher() {
			@Override
			public MockResponse dispatch(RecordedRequest request) {
				return new MockResponse().setResponseCode(200).setBody("ok");
			}
		});
		downstream.start();

		String base = "http://" + downstream.getHostName() + ":" + downstream.getPort();
		registry.add("spring.data.redis.host", () -> "localhost");
		registry.add("spring.data.redis.port", () -> redisPort);
		registry.add("SMS_SERVICE_URI", () -> base);
		registry.add("EMAIL_SERVICE_URI", () -> base);
	}

	@AfterAll
	static void tearDown() throws IOException {
		if (downstream != null) {
			downstream.shutdown();
		}
		if (redisServer != null) {
			redisServer.stop();
		}
	}

	private static int findFreePort() throws IOException {
		try (ServerSocket socket = new ServerSocket(0)) {
			return socket.getLocalPort();
		}
	}

	@Autowired
	private WebTestClient client;

	@Test
	void routesToDownstreamThenRateLimitsTheBurst() {
		// 1) Routing: the first request is proxied to the stub downstream.
		client.post().uri("/api/v1/notifications/sms")
				.exchange()
				.expectStatus().isOk()
				.expectBody(String.class).isEqualTo("ok");

		// 2) Rate limiting: a rapid burst must be partly rejected with 429.
		int ok = 1;
		int limited = 0;
		for (int i = 0; i < 40; i++) {
			int status = client.post().uri("/api/v1/notifications/sms")
					.exchange()
					.returnResult(String.class)
					.getStatus()
					.value();
			if (status == 429) {
				limited++;
			} else if (status == 200) {
				ok++;
			}
		}

		assertThat(limited)
				.as("the rate limiter must reject part of a 41-request burst")
				.isGreaterThan(0);
		assertThat(ok)
				.as("some requests should still pass through")
				.isGreaterThanOrEqualTo(1)
				.isLessThan(41);
	}
}
