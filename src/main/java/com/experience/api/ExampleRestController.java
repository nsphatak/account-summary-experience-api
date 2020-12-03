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
	public Mono<String> getAccountSummary(@PathVariable int id) {
		
		System.out.println("INTO ACTUAL CALL ....");
		if(true) {
			throw new RuntimeException();
		}
   		return Mono.just("from Service call....");
	}

	public Mono<String> getAccountSummaryFallback() {
		System.out.println("Into Fallback : ");
		return Mono.just("Into FallBack");
	}
}
