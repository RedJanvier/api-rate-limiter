package com.redjanvier.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import reactor.core.publisher.Mono;

/**
 * Key resolvers for the gateway's {@code RequestRateLimiter} filter.
 *
 * <p>The rate limiter buckets requests by the key returned here. Two strategies
 * are provided so both README requirements are covered:
 *
 * <ul>
 *   <li>{@link #systemKeyResolver()} — one shared bucket for the whole system.
 *       This enforces the "limit amount of requests per time window across the
 *       whole system" requirement (the {@code 10 req / 3s} system-wide limit).</li>
 *   <li>{@link #clientKeyResolver()} — one bucket per client, keyed by the
 *       {@code X-Client-Id} header (falling back to the caller's IP). This
 *       enforces the "limit too many requests from a client" requirement.</li>
 * </ul>
 *
 * <p>The active strategy is chosen in {@code application.yml} via
 * {@code key-resolver: "#{@systemKeyResolver}"} (or {@code #{@clientKeyResolver}}).
 * {@code systemKeyResolver} is {@link Primary} so it is used if a route omits an
 * explicit key resolver.
 */
@Configuration
public class RateLimiterConfig {

	static final String SYSTEM_BUCKET = "system";
	static final String CLIENT_HEADER = "X-Client-Id";
	static final String UNKNOWN_CLIENT = "unknown";

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
						: UNKNOWN_CLIENT;
			}
			return Mono.just(clientId);
		};
	}

}
