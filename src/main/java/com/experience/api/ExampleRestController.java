package com.experience.api;

import org.apache.commons.lang3.RandomUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@RestController
@RequestMapping("/experience/api")
public class ExampleRestController {

	@GetMapping("/account/summary")
	@HystrixCommand(fallbackMethod = "getAccountSummaryFallback", commandKey = "accountsummary", groupKey = "accountsummary")
	public String getAccountSummary() {

		if (RandomUtils.nextBoolean()) {
			throw new RuntimeException("Exception in Account Summary");
		}
		return "AccountSummry";

	}

	public String getAccountSummaryFallback() {
		return "Into Account Summary Fallback !!!";
	}
}
