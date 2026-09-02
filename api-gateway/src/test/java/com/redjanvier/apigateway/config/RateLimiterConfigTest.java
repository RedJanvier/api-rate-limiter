package com.redjanvier.apigateway.config;

import java.net.InetSocketAddress;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import reactor.test.StepVerifier;

/**
 * Unit tests for the rate-limiter key resolvers — no Spring context or Redis needed.
 */
class RateLimiterConfigTest {

	private final RateLimiterConfig config = new RateLimiterConfig();

	@Test
	void systemKeyResolver_alwaysReturnsTheSharedBucket() {
		KeyResolver resolver = config.systemKeyResolver();
		MockServerWebExchange exchange = MockServerWebExchange.from(
				MockServerHttpRequest.post("/api/v1/notifications/sms"));

		StepVerifier.create(resolver.resolve(exchange))
				.expectNext("system")
				.verifyComplete();
	}

	@Test
	void clientKeyResolver_usesClientIdHeaderWhenPresent() {
		KeyResolver resolver = config.clientKeyResolver();
		MockServerWebExchange exchange = MockServerWebExchange.from(
				MockServerHttpRequest.post("/x").header("X-Client-Id", "client-42"));

		StepVerifier.create(resolver.resolve(exchange))
				.expectNext("client-42")
				.verifyComplete();
	}

	@Test
	void clientKeyResolver_fallsBackToRemoteIpWhenHeaderMissing() {
		KeyResolver resolver = config.clientKeyResolver();
		MockServerWebExchange exchange = MockServerWebExchange.from(
				MockServerHttpRequest.post("/x").remoteAddress(new InetSocketAddress("10.1.2.3", 9999)));

		StepVerifier.create(resolver.resolve(exchange))
				.expectNext("10.1.2.3")
				.verifyComplete();
	}

	@Test
	void clientKeyResolver_fallsBackToRemoteIpWhenHeaderBlank() {
		KeyResolver resolver = config.clientKeyResolver();
		MockServerWebExchange exchange = MockServerWebExchange.from(
				MockServerHttpRequest.post("/x")
						.header("X-Client-Id", "   ")
						.remoteAddress(new InetSocketAddress("10.9.8.7", 1234)));

		StepVerifier.create(resolver.resolve(exchange))
				.expectNext("10.9.8.7")
				.verifyComplete();
	}

	@Test
	void clientKeyResolver_returnsUnknownWhenNoHeaderAndNoRemoteAddress() {
		KeyResolver resolver = config.clientKeyResolver();
		MockServerWebExchange exchange = MockServerWebExchange.from(
				MockServerHttpRequest.post("/x"));

		StepVerifier.create(resolver.resolve(exchange))
				.expectNext("unknown")
				.verifyComplete();
	}
}
