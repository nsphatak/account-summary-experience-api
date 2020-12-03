package com.experience.api.aspect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.function.Supplier;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.stereotype.Component;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.vavr.control.Try;
import reactor.core.publisher.Mono;

@Aspect
@Component
public class CircuitBreakerAspect<T> {

	@SuppressWarnings("rawtypes")
	@Autowired
	private ReactiveCircuitBreakerFactory circuitBreakerFactory;

	private CircuitBreakerConfig circuitBreakerConfig;
	
	private CircuitBreaker circuitBreaker;

	@Pointcut("@annotation(com.experience.api.aspect.CircuitBreakerCommand)")
	public void circuitBreakerCommandAnnotationPointCut() {

	}

	public CircuitBreakerAspect() {
		circuitBreakerConfig = CircuitBreakerConfig.custom().failureRateThreshold(10)
				.waitDurationInOpenState(Duration.ofMillis(10000)).permittedNumberOfCallsInHalfOpenState(1)
				.slidingWindowSize(10).build();
		CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig);
		circuitBreaker = circuitBreakerRegistry.circuitBreaker("externalService");
	}

	@SuppressWarnings("unchecked")
	@Around("circuitBreakerCommandAnnotationPointCut()")
	public Mono<T> methodsAnnotatedWithCircuitBrekerCommand(final ProceedingJoinPoint joinPoint) throws Throwable {

		// Create a CircuitBreakerRegistry with a custom global configuration
	

		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();

		CircuitBreakerCommand circuitBreakerClass = method.getAnnotation(CircuitBreakerCommand.class);

		// Mono<T> responseObj = (Mono<T>) joinPoint.proceed();

		String fallbackMethodName = circuitBreakerClass.fallbackMethod();
		Method fallbackMethod = joinPoint.getTarget().getClass().getMethod(fallbackMethodName);
		System.out.println(
				"Metrics :" + circuitBreaker.getMetrics().getFailureRate() + " : " + circuitBreaker.getState());
		
		
			Supplier<Mono<String>> decoratedSupplier = CircuitBreaker
					.decorateSupplier(circuitBreaker,() -> {
						
						Mono<String> a = null;
						try {
							a = (Mono<String>) joinPoint.proceed();
						} catch (Throwable e1) {
							// TODO Auto-generated catch block
							throw new RuntimeException();
						}
						return a;
					});
			
			Mono<String> result = (Mono<String>) Try.ofSupplier(decoratedSupplier).recover(throwable -> {
				try {
					return (Mono<String>) fallbackMethod.invoke(joinPoint.getTarget());
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					// TODO Auto-generated catch block
					System.out.println("into ERROR: " + e.getMessage());
					// e.printStackTrace();
				}
				return null;
			}).get();
		return null;
	}

}
