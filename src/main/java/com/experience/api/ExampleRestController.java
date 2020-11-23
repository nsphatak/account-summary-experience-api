package com.experience.api;

import org.apache.commons.lang3.RandomUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.experience.api.aspect.CircuitBreakerCommand;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/experience/api")
public class ExampleRestController {

	@GetMapping("/account/summary")
	@CircuitBreakerCommand(fallbackMethod = "getAccountSummaryFallback")
	public Mono<String> getAccountSummary() {

		if (!RandomUtils.nextBoolean()) {
			return Mono.error(new RuntimeException("Error while calling Service"));
		}

		return Mono.just("Into Actual Call");
	}

	public Mono<String> getAccountSummaryFallback() {
		System.out.println("Thread Name : " + Thread.currentThread().getName());
		return Mono.just("Into Fallback Call......");
	}
}
