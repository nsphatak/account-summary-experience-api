package com.experience.api;

import org.apache.commons.lang3.RandomUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.experience.api.aspect.CircuitBreakerCommand;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/experience/api")
public class ExampleRestController {

	@GetMapping("/account/summary/{id}")
	@CircuitBreakerCommand(fallbackMethod = "getAccountSummaryFallback")
	public Mono<Account> getAccountSummary(@PathVariable int id) {

		if (!RandomUtils.nextBoolean()) {
			return Mono.error(new RuntimeException("Error while calling Service"));
		}

		System.out.println("INTO ACTUAL CALL ....");
  		return Mono.empty();
	}

	public Mono<Account> getAccountSummaryFallback() {
		System.out.println("Into Fallback : " + Thread.currentThread().getName());
		return Mono.empty();
	}
}
