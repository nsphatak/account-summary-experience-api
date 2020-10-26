package com.experience.api;

import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/experience/api")
public class ExampleRestController {

	@SuppressWarnings("rawtypes")
	@Autowired
	private CircuitBreakerFactory circuitBreakerFactory;

	@GetMapping("/account/summary")
	public String getAccountSummary() {

		CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");

		return circuitBreaker.run(() -> {
			if (RandomUtils.nextBoolean()) {
				throw new RuntimeException("Exception in Account Summary");
			}
			return "AccountSummry";
		}, throwable -> getAccountSummaryFallback());

	}

	public String getAccountSummaryFallback() {
		return "Into Account Summary Fallback !!!";
	}
}
