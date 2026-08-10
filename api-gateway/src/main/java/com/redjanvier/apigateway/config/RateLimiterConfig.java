package com.redjanvier.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

	private static final String SYSTEM_BUCKET = "system";
	private static final String CLIENT_HEADER = "X-Client-Id";

	@Bean
	@Primary
	public KeyResolver systemKeyResolver() {
		return exchange -> Mono.just(SYSTEM_BUCKET);
	}

	@Bean
	public KeyResolver clientKeyResolver() {
		return exchange -> {
			String clientId = exchange.getRequest().getHeaders().getFirst(CLIENT_HEADER);
			if (clientId == null || clientId.isBlank()) {
				clientId = exchange.getRequest().getRemoteAddress() != null
						? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
						: "unknown";
			}
			return Mono.just(clientId);
		};
	}

}
