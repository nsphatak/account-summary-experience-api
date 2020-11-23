package com.experience.api.aspect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Aspect
@Component
public class CircuitBreakerAspect<T> {

	@SuppressWarnings("rawtypes")
	@Autowired
	private ReactiveCircuitBreakerFactory circuitBreakerFactory;

	@Pointcut("@annotation(com.experience.api.aspect.CircuitBreakerCommand)")
	public void circuitBreakerCommandAnnotationPointCut() {
	}

	@SuppressWarnings("unchecked")
	@Around("circuitBreakerCommandAnnotationPointCut()")
	public Mono<T> methodsAnnotatedWithCircuitBrekerCommand(final ProceedingJoinPoint joinPoint) throws Throwable {

		ReactiveCircuitBreaker circuitBreaker = circuitBreakerFactory.create("TEST");

		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();

		CircuitBreakerCommand circuitBreakerClass = method.getAnnotation(CircuitBreakerCommand.class);

		// Mono<T> responseObj = (Mono<T>) joinPoint.proceed();

		return circuitBreaker.run((Mono<T>) joinPoint.proceed(), throwable -> {

			String fallbackMethodName = circuitBreakerClass.fallbackMethod();
			Object result = null;
			try {
				Method fallbackMethod = joinPoint.getTarget().getClass().getMethod(fallbackMethodName);

				result = fallbackMethod.invoke(joinPoint.getTarget());
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return (Mono<T>) Mono.just(result);
		}

		);

	}

}
